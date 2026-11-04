package br.com.muxe.afk.route;

public sealed interface Destination {

    record Server(String id) implements Destination {
    }

    record Group(String name) implements Destination {
    }
}
