package suculenta.webservice.config;

import lombok.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response,
        @NonNull WebSocketHandler wsHandler,
        @NonNull Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest httpRequest) {
            String userId = httpRequest.getServletRequest().getParameter("userId");
            String role = httpRequest.getServletRequest().getParameter("role");

            attributes.put("userId", userId);
            attributes.put("role", role);
        }

        return true;
    }

    @Override
    public void afterHandshake(
        @NonNull ServerHttpRequest request,
        @NonNull ServerHttpResponse response,
        @NonNull WebSocketHandler wsHandler,
        Exception exception) {

    }
}
