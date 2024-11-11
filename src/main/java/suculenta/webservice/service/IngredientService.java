package suculenta.webservice.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import suculenta.webservice.dto.CountedIngredient;
import suculenta.webservice.dto.PredictedIngredient;
import suculenta.webservice.model.Ingredient;
import suculenta.webservice.repository.IngredientRepository;

import java.net.URI;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service

@RequiredArgsConstructor
public class IngredientService implements CrudService<Ingredient, UUID> {
    private final IngredientRepository repository;
    private final RestTemplate restTemplate;

    @Override
    public JpaRepository<Ingredient, UUID> repository() {
        return repository;
    }

    public List<PredictedIngredient> predict(@NonNull Date from) {
        String apiURL = "http://localhost:8000/predict";

        URI uri = UriComponentsBuilder.fromHttpUrl(apiURL)
            .build()
            .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        Map<String, Object> body = Map.of("fecha", from.toString());

        ResponseEntity<List<PredictedIngredient>> response = restTemplate.exchange(
            uri,
            HttpMethod.POST,
            new HttpEntity<>(body, headers),
            new ParameterizedTypeReference<>() {}
        );
        var predictions = Objects.requireNonNull(response.getBody());
        return repository.countLastMonthIngredientUsed().stream()
            .map(ingredient -> {
                var countedIngredient = new CountedIngredient(ingredient);
                var prediction = predictions.stream()
                    .filter(i -> i.name().equals(countedIngredient.name()))
                    .findFirst()
                    .orElse(PredictedIngredient.zero());

                return prediction.complete(countedIngredient);
            })
            .toList();
    }
}
