
# Family Shop — Backend (Spring Boot + Postgres + STOMP)

Backend для приложения семейного учёта покупок. Поддерживает REST API, WebSocket (SockJS + STOMP) для лайв‑обновлений и интеграцию с Postgres.

## ⚙️ Стек
- Java **21**, Spring Boot **3.x**
- Gradle (**8.x**) / Maven (возможна сборка)
- Postgres **16**
- WebSocket: SockJS + STOMP (`/ws`, топики `/topic/family/{id}`)

---

## 📁 Архитектура (основные домены)
- **Family** — семья/группа пользователей.
- **User** — пользователь (в MVP: регистрация по email + пароль + familyId).
- **ShoppingList** — активные позиции покупки (isBought=false).
- **FrequentItem** — частые покупки (накапливаем frequency).
- **PurchaseHistory** — история покупок.

События домена публикуются в STOMP‑топик семьи:
```
/topic/family/{familyId}
```
Фронт ожидает payload вида:
```json
{ "type": "SHOPPING_ADDED", "familyId": 100, "payload": { ... } }
```

Типы событий: `SHOPPING_ADDED`, `SHOPPING_UPDATED`, `SHOPPING_BOUGHT`, `SHOPPING_DELETED`, `FREQUENT_UPDATED`, `HISTORY_ADDED`.

---

## 🔐 Безопасность (MVP)
- **CORS** разрешён для dev‑фронта (`http://localhost:5173`) и локальных origin.
- **CSRF** отключён для `/api/**` и `/ws/**`, чтобы POST/PUT/DELETE не падали 403 в SPA.
- Все эндпоинты `/api/**` открыты в MVP. Для продакшена рекомендуется JWT.

Файлы конфигурации (пример):
```java
// CorsConfig.java
@Configuration
public class CorsConfig {
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost", "http://127.0.0.1"));
    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/**", cfg);
    return src;
  }
}
```
```java
// SecurityConfig.java
@Configuration
public class SecurityConfig {
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource cors) throws Exception {
    http.cors(c -> c.configurationSource(cors))
       .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/ws/**"))
       .authorizeHttpRequests(auth -> auth
         .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
         .requestMatchers(HttpMethod.POST, "/api/families").permitAll()
         .requestMatchers(HttpMethod.GET, "/api/families/**").permitAll()
         .requestMatchers("/api/users/register").permitAll()
         .requestMatchers("/api/**", "/ws/**").permitAll()
         .anyRequest().permitAll()
       )
       .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    return http.build();
  }
}
```

---

## 🔌 WebSocket (STOMP)
Конфигурация:
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:5173","http://localhost","http://127.0.0.1")
            .withSockJS();
  }
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }
}
```
Публикация события (утилита):
```java
@Component
@RequiredArgsConstructor
public class FamilyEventPublisher {
  private final SimpMessagingTemplate template;
  public void send(Long familyId, String type, Object payload) {
    template.convertAndSend("/topic/family/" + familyId, new FamilyEvent(type, familyId, payload));
  }
}
```

---

## 🌱 Конфигурация окружения
**application.yml**
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update   # для prod рекомендуется Flyway
    properties:
      hibernate:
        format_sql: true
server:
  port: 8080
```

**Переменные окружения** (локально/в Docker):
```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/familyshop
SPRING_DATASOURCE_USERNAME=family
SPRING_DATASOURCE_PASSWORD=familypass
```

---

## ▶️ Запуск локально (Gradle)
```bash
# Требуется Java 21
./gradlew bootRun
# или
./gradlew clean bootJar && java -jar build/libs/*.jar
```

## 🐳 Docker
**Dockerfile (Gradle Wrapper):**
```dockerfile
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies || true
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
```
**docker-compose.yml** (фрагмент):
```yaml
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: familyshop
      POSTGRES_USER: family
      POSTGRES_PASSWORD: familypass
    ports: ["5432:5432"]
    volumes: ["pgdata:/var/lib/postgresql/data"]

  backend:
    build: ./backend
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/familyshop
      SPRING_DATASOURCE_USERNAME: family
      SPRING_DATASOURCE_PASSWORD: familypass
    depends_on: [db]
    expose: ["8080"]
volumes: { pgdata: {} }
```

---

## 🧪 Быстрая проверка API (cURL)
Создать семью:
```bash
curl -i -X POST http://localhost:8080/api/families \
  -H "Content-Type: application/json" \
  -d '{"name":"Наша семья"}'
```
Регистрация пользователя:
```bash
curl -i -X POST "http://localhost:8080/api/users/register?email=a@a.com&password=pass123&familyId=1"
```
Активный список:
```bash
curl -s http://localhost:8080/api/shopping-list/family/1 | jq
```
Добавить позицию:
```bash
curl -i -X POST http://localhost:8080/api/shopping-list \
  -H "Content-Type: application/json" \
  -d '{"family":{"id":1},"itemName":"Молоко","quantity":"2"}'
```
Отметить купленным:
```bash
curl -i -X PUT http://localhost:8080/api/shopping-list/1/bought
```

---

## 🔁 События, которые публикует бэкенд
Вызывайте `eventPublisher.send(familyId, "<TYPE>", payload)` в сервисах:
- `SHOPPING_ADDED` — после создания позиции
- `SHOPPING_BOUGHT` — после отметки купленным
- `SHOPPING_DELETED` — после удаления
- `FREQUENT_UPDATED` — после инкремента частоты
- `HISTORY_ADDED` — после записи в историю

Фронт подписан на: `/topic/family/{id}` и обновляет UI.

---

## 🧰 Траблшутинг
- **CORS ошибка (browser)** → проверь `CorsConfig` и `allowedOrigins`.
- **403 на POST/PUT/DELETE** → CSRF: проверь, что в `SecurityConfig` игнорируется `/api/**` и `/ws/**`.
- **DB connection** → проверь `SPRING_DATASOURCE_URL` и доступность Postgres.
- **WebSocket не коннектится** → проверь URL `/ws` и `setAllowedOrigins(...)`.
- **Схема БД** → `ddl-auto: update` удобно для dev, в prod используйте **Flyway**.

---

## 🗺️ Дальше в прод
- JWT‑аутентификация (логин/refresh), закрыть `/api/**`.
- Flyway‑миграции вместо `ddl-auto`.
- Actuator `/actuator/health` и метрики.
- Nginx/Traefik перед бэком (SSL, gzip).
- Тесты: unit + интеграционные (Testcontainers для Postgres).
