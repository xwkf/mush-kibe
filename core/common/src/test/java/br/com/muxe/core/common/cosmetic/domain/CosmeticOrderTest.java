package br.com.muxe.core.common.cosmetic.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class CosmeticOrderTest {

    @Test
    void ownedCosmeticsComeFirstAndEachGroupIsAlphabetical() {
        Set<String> owned = Set.of("Tinta", "Apavorante");
        List<String> names = List.of("Tinta", "Coelhos", "Apavorante", "Bola de Canhão", "Abóboras");

        List<String> sorted = names.stream().sorted(CosmeticOrder.ownedFirst(owned::contains, Function.identity())).toList();

        assertThat(sorted).containsExactly("Apavorante", "Tinta", "Abóboras", "Bola de Canhão", "Coelhos");
    }

    @Test
    void accentsAndCaseDoNotChangeTheAlphabeticalOrder() {
        List<String> names = List.of("Ônibus", "ovelha", "Avalanche", "Árvore");

        List<String> sorted = names.stream().sorted(CosmeticOrder.ownedFirst(name -> false, Function.identity())).toList();

        assertThat(sorted).containsExactly("Árvore", "Avalanche", "Ônibus", "ovelha");
    }
}
