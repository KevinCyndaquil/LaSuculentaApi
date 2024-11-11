package suculenta.webservice.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import suculenta.webservice.service.AdminService;
import suculenta.webservice.service.KitchenerService;
import suculenta.webservice.service.WaiterService;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final WaiterService waiterService;
    private final KitchenerService kitchenerService;
    private final AdminService adminService;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String role = (String) session.getAttributes().get("role");
        System.out.println("up session attributes " + userId + " " + role);

        switch (role) {
            case "waiter" -> waiterService.register(userId, session);
            case "kitchener" -> kitchenerService.register(userId, session);
            case "admin" -> adminService.register(userId, session);
            default -> System.out.println("Invalid role " + role);
        }

        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String role = (String) session.getAttributes().get("role");
        System.out.println("down session attributes " + userId + " " + role);

        switch (role) {
            case "waiter" -> waiterService.removeSession(userId);
            case "kitchener" -> kitchenerService.removeSession(userId);
            case "admin" -> adminService.removeSession(userId);
            default -> System.out.println("Invalid role" + role);
        }

        super.afterConnectionClosed(session, status);
    }
}
