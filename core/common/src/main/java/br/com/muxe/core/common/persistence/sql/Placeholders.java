package br.com.muxe.core.common.persistence.sql;

import java.util.ArrayList;
import java.util.List;

final class Placeholders {

    private Placeholders() {
    }

    static List<String> split(String sql, int expected) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        for (int index = 0; index < sql.length(); index++) {
            if (sql.charAt(index) == '?') {
                parts.add(sql.substring(start, index));
                start = index + 1;
            }
        }
        parts.add(sql.substring(start));
        if (parts.size() - 1 != expected) {
            throw new IllegalArgumentException("The SQL has " + (parts.size() - 1) + " placeholders but " + expected + " parameters were given: " + sql);
        }
        return List.copyOf(parts);
    }
}
