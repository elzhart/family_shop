-- 1. Таблица семей
CREATE TABLE families (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Пользователи
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       family_id BIGINT REFERENCES families(id) ON DELETE CASCADE,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Список покупок
CREATE TABLE shopping_list (
                               id BIGSERIAL PRIMARY KEY,
                               family_id BIGINT REFERENCES families(id) ON DELETE CASCADE,
                               item_name VARCHAR(255) NOT NULL,
                               quantity VARCHAR(50),
                               is_bought BOOLEAN DEFAULT false,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. История покупок
CREATE TABLE purchases_history (
                                   id BIGSERIAL PRIMARY KEY,
                                   family_id BIGINT REFERENCES families(id) ON DELETE CASCADE,
                                   item_name VARCHAR(255) NOT NULL,
                                   quantity VARCHAR(50),
                                   bought_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Часто покупаемые товары
CREATE TABLE frequent_items (
                                id BIGSERIAL PRIMARY KEY,
                                family_id BIGINT REFERENCES families(id) ON DELETE CASCADE,
                                item_name VARCHAR(255) NOT NULL,
                                frequency INT DEFAULT 0
);

-- Индексы для ускорения запросов
CREATE INDEX idx_users_family ON users(family_id);
CREATE INDEX idx_shopping_list_family ON shopping_list(family_id);
CREATE INDEX idx_purchases_history_family ON purchases_history(family_id);
CREATE INDEX idx_frequent_items_family ON frequent_items(family_id);