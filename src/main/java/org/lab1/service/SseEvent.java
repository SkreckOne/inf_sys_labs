package org.lab1.service;


public class SseEvent {

    private final String name;
    private final Object data;

    public SseEvent(String name, Object data) {
        this.name = name;
        this.data = data;
    }

    public String getName() {
        return name;
    }

    public Object getData() {
        return data;
    }
}
