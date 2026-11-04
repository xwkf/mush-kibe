package br.com.muxe.core.bukkit.config.edit;

public record ConfigOutcome(Status status, String path, String detail, ReloadReport reload) {

    public enum Status {
        CHANGED,
        INVALID_PATH,
        PROTECTED,
        UNKNOWN_PATH,
        IS_SECTION,
        BLOCKED,
        TYPE_MISMATCH,
        NOT_A_LIST,
        MISSING_ITEM
    }

    public static ConfigOutcome refused(Status status, String path, String detail) {
        return new ConfigOutcome(status, path, detail, ReloadReport.NONE);
    }

    public boolean changed() {
        return status == Status.CHANGED;
    }
}
