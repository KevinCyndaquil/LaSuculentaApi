package suculenta.webservice.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket     //Habilita el soporte basico
@EnableWebSocketMessageBroker //habilita la gestion de mensaje entre cliente y servidor  
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer, WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue"); //los mensaes con queue son enviados por este broker 
        registry.setApplicationDestinationPrefixes("/app"); //prefijo con el que cliente debe de enviar los mensajes al servidor 
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/websocket") //conex client
            .setAllowedOrigins("*") //cualquier org
            .withSockJS(); // no acep puro websk
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/websocket")
            .addInterceptors(new WebSocketHandshakeInterceptor())
            .setAllowedOrigins("*");
    }
}
