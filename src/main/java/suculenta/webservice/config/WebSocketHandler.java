package suculenta.webservice.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import suculenta.webservice.service.KitchenerService;
import suculenta.webservice.service.WaiterService;

@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final WaiterService waiterService;
    private final KitchenerService kitchenerService;

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String role = (String) session.getAttributes().get("role");

        if (role.equals("waiter"))
            waiterService.register(userId, session);
        if (role.equals("kitchener"))
            kitchenerService.register(userId, session);

        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        String role = (String) session.getAttributes().get("role");

        if (role.equals("waiter"))
            waiterService.removeSession(userId).close();
        if (role.equals("kitchener"))
            kitchenerService.removeSession(userId).close();

        super.afterConnectionClosed(session, status);
    }
}
