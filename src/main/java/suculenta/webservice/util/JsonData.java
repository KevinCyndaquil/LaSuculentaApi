package suculenta.webservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public enum JsonData {
    WAITER(Objects.requireNonNull(JsonData.class.getClassLoader().getResource(
        "templates/json/waiter.json"))),
    KITCHENER(Objects.requireNonNull(JsonData.class.getClassLoader().getResource(
        "templates/json/kitchener.json"))),
    INGREDIENT(Objects.requireNonNull(JsonData.class.getClassLoader().getResource(
        "templates/json/ingredient.json"))),
    DISH(Objects.requireNonNull(JsonData.class.getClassLoader().getResource(
        "templates/json/dish.json")));

    final URL url;
    final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> map() {
        try {
            return mapper.readValue(url, new TypeReference<>() {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> List<T> content(Class<T> _class) {
        return map().stream()
            .map(m -> mapper.convertValue(m, _class))
            .toList();
    }
}
