package com.github.anrimian.musicplayer.domain.models.player.events;

public class ErrorEvent implements PlayerEvent {

    private Throwable throwable;

    public ErrorEvent(Throwable throwable) {
        this.throwable = throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}