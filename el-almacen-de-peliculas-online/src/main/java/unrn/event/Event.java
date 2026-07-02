package unrn.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Envoltorio generico para eventos intercambiados por RabbitMQ.
 *
 * Combina tipo de evento, clave y payload para construir mensajes publicables o
 * consumibles por la vertical. La routing key se deriva del tipo concreto de dato y
 * de la operacion, por eso funciona como contrato tecnico comun.
 */
public class Event<K, T> {

    /**
     * Operaciones basicas que puede expresar el envoltorio generico de eventos.
     *
     * CREATE representa alta o actualizacion de datos entrantes y DELETE reserva el
     * caso de baja para integraciones que lo necesiten.
     */
    public enum Type {
        CREATE,
        DELETE
    }

    private Type eventType;
    private K key;
    private T data;

    /**
     * Inicializa una instancia de Event con los datos necesarios.
     */
    public Event() {
        this.eventType = null;
        this.key = null;
        this.data = null;
    }

    /**
     * Inicializa una instancia de Event con los datos necesarios.
     */
    public Event(Type eventType, K key, T data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
    }

    /**
     * Devuelve la operacion que representa el evento generico.
     */
    public Type getEventType() {
        return eventType;
    }

    /**
     * Asigna la operacion del evento durante la deserializacion o armado del mensaje.
     */
    public void setEventType(Type eventType) {
        this.eventType = eventType;
    }

    /**
     * Devuelve la clave de negocio usada para identificar o rutear el evento.
     */
    public K getKey() {
        return key;
    }

    /**
     * Asigna la clave de negocio del evento durante la deserializacion o armado del mensaje.
     */
    public void setKey(K key) {
        this.key = key;
    }

    /**
     * Devuelve el payload de negocio transportado por el evento.
     */
    public T getData() {
        return data;
    }

    /**
     * Asigna el payload de negocio del evento durante la deserializacion o armado del mensaje.
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * Calcula la routing key a partir del payload y el tipo de evento.
     */
    @JsonIgnore
    public String getRoutingkey() {
        return this.getData().getClass().getSimpleName() + "." + this.getEventType();
    }
}
