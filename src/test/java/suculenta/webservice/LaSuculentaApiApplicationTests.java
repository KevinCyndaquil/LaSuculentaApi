package suculenta.webservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.ActiveProfiles;
import suculenta.webservice.model.Waiter;
import suculenta.webservice.repository.WaiterRepository;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@SpringBootTest
@ActiveProfiles("test")
class LaSuculentaApiApplicationTests {
    @Autowired
    private ObjectMapper mapper;

    @Test
    void contextLoads() {

    }

    @Bean
    public CommandLineRunner demo(WaiterRepository waiterRepository) throws URISyntaxException, IOException {
        List<Waiter> waiters = mapper.readValue(
            Waiter.class.getClassLoader().getResource("templates/json/waiter.json"),
            new TypeReference<>() {}
		);
		List<Waiter> kitcheners = mapper.readValue(
			Waiter.class.getClassLoader().getResource("templates/json/kitcheners.json"),
			new TypeReference<>() {}
		);
		List<Waiter> ingredients = mapper.readValue(
			Waiter.class.getClassLoader().getResource("templates/json/ingredient.json"),
			new TypeReference<>() {}
		);

        return args -> {

        };
    }

}
