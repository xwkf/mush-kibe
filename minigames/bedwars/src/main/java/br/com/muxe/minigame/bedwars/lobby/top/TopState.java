package br.com.muxe.minigame.bedwars.lobby.top;

import br.com.muxe.minigame.bedwars.stats.domain.PlayerStats;

public final class TopState {

    public static final int PAGES = 10;
    public static final int PAGE_SIZE = 10;

    private int page;
    private int period;
    private String mode = PlayerStats.GENERAL;

    public int page() {
        return page;
    }

    public int period() {
        return period;
    }

    public String mode() {
        return mode;
    }

    public void nextPage() {
        page = (page + 1) % PAGES;
    }

    public void nextPeriod(int periods) {
        period = (period + 1) % Math.max(1, periods);
        page = 0;
    }

    public void mode(String value) {
        mode = value;
        page = 0;
    }
}
