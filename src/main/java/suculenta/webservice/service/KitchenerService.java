package suculenta.webservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.repository.KitchenerRepository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service

@RequiredArgsConstructor
public class KitchenerService implements CrudService<Kitchener, UUID>, WebSocketService {
    private final KitchenerRepository repository;
    ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public ConcurrentHashMap<String, WebSocketSession> sessions() {
        return sessions;
    }

    @Override
    public KitchenerRepository repository() {
        return repository;
    }
}
