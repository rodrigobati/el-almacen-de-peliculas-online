package unrn.event;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Event<K, T> {

    public enum Type {
        CREATE,
        DELETE
    }

    private Type eventType;
    private K key;
    private T data;

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

    public Type getEventType() {
        return eventType;
    }

    public void setEventType(Type eventType) {
        this.eventType = eventType;
    }

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @JsonIgnore
    public String getRoutingkey() {
        return this.getData().getClass().getSimpleName() + "." + this.getEventType();
    }
}