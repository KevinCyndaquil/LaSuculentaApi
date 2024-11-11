package suculenta.webservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import suculenta.webservice.model.Waiter;
import suculenta.webservice.repository.WaiterRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WaiterService implements CrudService<Waiter, UUID>, WebSocketService {
    Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final WaiterRepository waiterRepository;

    @Override
    public Map<String, WebSocketSession> sessions() {
        return sessions;
    }

    @Override
    public WaiterRepository repository() {
        return waiterRepository;
    }
}
