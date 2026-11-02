package br.com.muxe.core.common.cosmetic.domain;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Predicate;

public final class CosmeticOrder {

    private static final Locale PORTUGUESE = Locale.forLanguageTag("pt-BR");

    private CosmeticOrder() {
    }

    public static <T> Comparator<T> ownedFirst(Predicate<T> owned, Function<T, String> name) {
        Collator alphabetical = Collator.getInstance(PORTUGUESE);
        alphabetical.setStrength(Collator.PRIMARY);
        Comparator<T> byName = Comparator.comparing(name, alphabetical);
        return Comparator.<T, Boolean>comparing(cosmetic -> !owned.test(cosmetic)).thenComparing(byName);
    }
}
