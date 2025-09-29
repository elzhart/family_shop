package com.familyshop.ws;

import com.familyshop.security.JwtService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Component
public class WsAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwt;

    public WsAuthHandshakeInterceptor(JwtService jwt) {
        this.jwt = jwt;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            org.springframework.web.socket.WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        try {
            var headers = request.getHeaders();
            String auth = headers.getFirst(HttpHeaders.AUTHORIZATION);
            String token = null;
            if (auth != null && auth.startsWith("Bearer ")) token = auth.substring(7);
            if (token == null && request instanceof ServletServerHttpRequest sreq) {
                token = sreq.getServletRequest().getParameter("token");
            }
            if (token != null) {
                var claims = jwt.parse(token).getBody();
                attributes.put("userEmail", claims.getSubject());
                attributes.put("uid", claims.get("uid"));
                attributes.put("familyId", claims.get("familyId"));
                return true;
            }
        } catch (Exception ignored) {
        }
        // Можно разрешить анонимов, если нужно:
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest req, ServerHttpResponse res,
            org.springframework.web.socket.WebSocketHandler wsHandler, Exception ex) {
    }
}