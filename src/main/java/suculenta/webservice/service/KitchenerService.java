package suculenta.webservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import suculenta.webservice.model.Kitchener;
import suculenta.webservice.repository.KitchenerRepository;

import java.util.UUID;

@Service

@RequiredArgsConstructor
public class KitchenerService implements CrudService<Kitchener, UUID>, WebSocketService {
    private final KitchenerRepository repository;

    @Override
    public KitchenerRepository repository() {
        return repository;
    }
}
