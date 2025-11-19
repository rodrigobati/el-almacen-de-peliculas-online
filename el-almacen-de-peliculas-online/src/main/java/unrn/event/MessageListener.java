// java
package unrn.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import unrn.infra.persistence.PeliculaRepository;
import unrn.model.Pelicula;

@Service
@Slf4j
public class MessageListener {

    static final String ERROR_PELICULA_NO_ENCONTRADA = "ERROR_PELICULA_NO_ENCONTRADA";

    private final PeliculaRepository peliculaRepository;

    public MessageListener(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(
                    value = "${rabbitmq.event.movie.queue.name}",
                    durable = "true"
            ),
            exchange = @Exchange(value = "${rabbitmq.event.exchange.name}", type = "topic"),
            key = "${rabbitmq.event.movie.routing.key}"
    ))
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void handleMovieEvent(Event<String, Pelicula> event) {
        switch (event.getEventType()) {
            case CREATE:
                Long id = event.getData().id();
                int newRating = event.getData().rating();

                Pelicula pelicula = peliculaRepository.porId(id);
                if (pelicula == null) {
                    throw new RuntimeException(ERROR_PELICULA_NO_ENCONTRADA);
                }

                pelicula.actualizarRating(newRating);
                peliculaRepository.actualizar(id, pelicula);
                log.info("Película {} actualizada con nuevo rating {}", id, newRating);
                break;

            case DELETE:
                // Si en el futuro se requiere eliminación, implementarla aquí.
                break;

            default:
                // Ignorar otros tipos si los hubiera.
        }
    }

    @Recover
    public void recover(Exception e, Event<String, Pelicula> event) {
        log.info("Recover: no se pudo procesar el evento después de reintentos: {}", event.getData());
    }
}
