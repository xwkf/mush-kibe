package br.com.muxe.core.common.cqrs.bus;

import br.com.muxe.core.common.concurrent.TaskExecutor;
import br.com.muxe.core.common.cqrs.middleware.HandlerMiddleware;
import br.com.muxe.core.common.cqrs.middleware.Invocation;
import br.com.muxe.core.common.cqrs.middleware.MessageKind;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

final class MessageDispatcher {

    private final MessageKind kind;
    private final TaskExecutor executor;
    private final List<HandlerMiddleware> middlewares;
    private final Map<Class<?>, Handler> handlers = new ConcurrentHashMap<>();

    MessageDispatcher(MessageKind kind, TaskExecutor executor, List<HandlerMiddleware> middlewares) {
        this.kind = kind;
        this.executor = executor;
        this.middlewares = List.copyOf(middlewares);
    }

    void register(Class<?> type, Handler handler) {
        Handler previous = handlers.putIfAbsent(type, handler);
        if (previous != null) {
            throw new IllegalStateException(kind.tag() + " handler already registered for " + type.getName());
        }
    }

    <R> CompletableFuture<R> dispatch(Object message) {
        Handler handler = handlers.get(message.getClass());
        if (handler == null) {
            return CompletableFuture.failedFuture(new HandlerNotFoundException(message.getClass()));
        }
        return executor.submit(() -> invoke(message, handler, 0));
    }

    @SuppressWarnings("unchecked")
    private <R> R invoke(Object message, Handler handler, int index) throws Exception {
        if (index == middlewares.size()) {
            return (R) handler.handle(message);
        }
        Invocation<R> next = () -> invoke(message, handler, index + 1);
        return middlewares.get(index).around(kind, message, next);
    }

    @FunctionalInterface
    interface Handler {
        Object handle(Object message) throws Exception;
    }
}
