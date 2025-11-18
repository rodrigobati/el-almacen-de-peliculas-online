package unrn.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

@Getter
public class Event<K, T> {

    public enum Type {
        CREATE,
        DELETE
    }

    private final Type eventType;
    private final K key;
    private final T data;

    public Event() {
        this.eventType = null;
        this.key = null;
        this.data = null;
    }

    public Event(Type eventType, K key, T data) {
        this.eventType = eventType;
        this.key = key;
        this.data = data;
    }

    @JsonIgnore
    public String getRoutingkey() {return  this.getData().getClass().getSimpleName() + "." + this.getEventType();}

}