package cl.sda1085.subastas.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ProductoClient {

    private final WebClient webClient;

    //Inyectar la URL
    public ProductoClient(WebClient.Builder webClientBuilder,
                          @Value("${productos-service.url}") String urlProductos){
        this.webClient = webClientBuilder.baseUrl(urlProductos).build();
    }

    //Método que permite comunicar con 'producto'
    public Map<String, Object> obtenerProductoPorId(Long id){
        return this.webClient.get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),  //Si 'productos' devuelve 404, se lanza una excepción clara
                        response -> Mono.error(new RuntimeException("El producto con ID " + id + " no existe en el catálogo de productos.")))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();  //Bloquear la ejecución para esperar el resultado
    }
}
