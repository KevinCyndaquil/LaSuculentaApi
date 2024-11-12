package suculenta.webservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.model.Waiter;
import suculenta.webservice.repository.WaiterRepository;

import java.sql.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WaiterService implements CrudService<Waiter, UUID>, WebSocketService {
    Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final WaiterRepository repository;

    @Override
    public Map<String, WebSocketSession> sessions() {
        return sessions;
    }

    @Override
    public WaiterRepository repository() {
        return repository;
    }

    public Page<Waiter> selectBest(Date since, Date until, @NonNull Pageable pageable) {
        var result = repository.best(since, until, pageable.getOffset(), pageable.getPageSize());
        return new PageImpl<>(result, pageable, repository.count());
    }
}
