package br.com.muxe.core.common.config.live;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class LiveValue<T> implements Supplier<T> {

    private final AtomicReference<T> current;

    public LiveValue(T initial) {
        this.current = new AtomicReference<>(Objects.requireNonNull(initial, "initial"));
    }

    @Override
    public T get() {
        return current.get();
    }

    public T swap(T next) {
        return current.getAndSet(Objects.requireNonNull(next, "next"));
    }

    public T reload(Supplier<? extends T> loader) {
        return swap(loader.get());
    }
}
