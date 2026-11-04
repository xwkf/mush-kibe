package br.com.muxe.lobby.afk;

import java.util.UUID;

public record AfkCandidate(UUID id, boolean exempt) {
}
