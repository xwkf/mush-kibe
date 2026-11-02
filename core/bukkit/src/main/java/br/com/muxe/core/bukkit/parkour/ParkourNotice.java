package br.com.muxe.core.bukkit.parkour;

import br.com.muxe.core.common.parkour.domain.ParkourOutcome;
import br.com.muxe.core.common.parkour.domain.ParkourTime;
import java.util.Optional;

public record ParkourNotice(String key, String... placeholders) {

    public static final String NEW_RECORD = "parkour-new-record";
    public static final String BEST_TIME = "parkour-best-time";

    public static Optional<ParkourNotice> after(ParkourOutcome outcome) {
        if (outcome.previous().isEmpty()) {
            return Optional.empty();
        }
        String previous = ParkourTime.format(outcome.previous().get());
        if (outcome.newRecord()) {
            return Optional.of(new ParkourNotice(NEW_RECORD, "previous", previous, "time", ParkourTime.format(outcome.time())));
        }
        return Optional.of(new ParkourNotice(BEST_TIME, "best", ParkourTime.format(outcome.best())));
    }
}
