// java
package unrn.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Listener RabbitMQ para eventos externos que impactan datos de peliculas.
 *
 * Consume eventos genericos de pelicula y eventos de rating, aplica reintentos y
 * actualiza el catalogo cuando llega nueva informacion desde otras verticales. Es
 * una pieza de integracion, no un endpoint HTTP.
 */
@Service
public class MessageListener {

    private static final Logger log = LoggerFactory.getLogger(MessageListener.class);
    static final String ERROR_PELICULA_NO_ENCONTRADA = "ERROR_PELICULA_NO_ENCONTRADA";

    private final PeliculaRepository peliculaRepository;

    /**
     * Inicializa una instancia de MessageListener con los datos necesarios.
     */
    public MessageListener(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    /**
     * Procesa eventos de pelicula recibidos por RabbitMQ.
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "${rabbitmq.event.movie.queue.name}", durable = "true"), exchange = @Exchange(value = "${rabbitmq.event.exchange.name}", type = "topic"), key = "${rabbitmq.event.movie.routing.key}"))
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
                log.info("PelÃ­cula {} actualizada con nuevo rating {}", id, newRating);
                break;

            case DELETE:
                // Si en el futuro se requiere eliminaciÃ³n, implementarla aquÃ­.
                break;

            default:
                // Ignorar otros tipos si los hubiera.
        }
    }

    /**
     * Registra la recuperacion luego de agotar reintentos de procesamiento.
     */
    @Recover
    public void recover(Exception e, Event<String, Pelicula> event) {
        log.info("Recover: no se pudo procesar el evento despuÃ©s de reintentos: {}", event.getData());
    }

    /**
     * Escucha eventos de actualizaciÃ³n de rating desde la vertical Rating.
     * Actualiza el ratingPromedio y totalRatings en la pelÃ­cula del catÃ¡logo.
     */
    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "rating.catalogo.queue", durable = "true"), exchange = @Exchange(value = "${rabbitmq.event.exchange.name}", type = "topic"), key = "RatingActualizadoEvent.#"))
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public void handleRatingEvent(Event<String, RatingActualizadoEvent> event) {
        log.info("Evento de rating recibido: {}", event.getData());

        Long peliculaId = event.getData().id();
        double ratingPromedio = event.getData().rating();
        int totalRatings = (int) event.getData().totalRatings();

        Pelicula pelicula = peliculaRepository.porId(peliculaId);
        if (pelicula == null) {
            throw new RuntimeException(ERROR_PELICULA_NO_ENCONTRADA);
        }

        pelicula.actualizarRatingPromedio(ratingPromedio, totalRatings);
        peliculaRepository.actualizar(peliculaId, pelicula);

        log.info("PelÃ­cula {} actualizada: ratingPromedio={}, totalRatings={}",
                peliculaId, ratingPromedio, totalRatings);
    }

    /**
     * Registra la recuperacion luego de fallar el procesamiento de rating.
     */
    @Recover
    public void recoverRating(Exception e, Event<String, RatingActualizadoEvent> event) {
        log.error("Recover: no se pudo procesar el evento de rating despuÃ©s de reintentos: {}",
                event.getData(), e);
    }
}
