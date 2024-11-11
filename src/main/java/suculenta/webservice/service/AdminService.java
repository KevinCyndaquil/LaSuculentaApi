package suculenta.webservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AdminService implements WebSocketService{
    ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public ConcurrentHashMap<String, WebSocketSession> sessions() {
        return sessions;
    }
}
