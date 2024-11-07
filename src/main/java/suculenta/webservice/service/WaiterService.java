package suculenta.webservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import suculenta.webservice.model.Waiter;
import suculenta.webservice.repository.WaiterRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WaiterService implements CrudService<Waiter, UUID>, WebSocketService {
    private final WaiterRepository waiterRepository;

    @Override
    public WaiterRepository repository() {
        return waiterRepository;
    }
}
