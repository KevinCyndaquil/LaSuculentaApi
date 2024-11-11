package suculenta.webservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.repository.KitchenerRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service

@RequiredArgsConstructor
public class KitchenerService implements CrudService<Kitchener, UUID>, WebSocketService {
    Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final KitchenerRepository repository;

    @Override
    public Map<String, WebSocketSession> sessions() {
        return sessions;
    }

    @Override
    public KitchenerRepository repository() {
        return repository;
    }
}
