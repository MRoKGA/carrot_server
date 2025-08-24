package com.mrokga.carrot_server.util;

import java.util.regex.Pattern;

public class SafeRedactor {
    private static final Pattern[] PATS = new Pattern[]{
            Pattern.compile("(?i)(authorization|api[_-]?key|password|secret|token)[=: ]+([^\\s\"']+)")
    };
    public static String redact(String s) {
        if (s == null) return null;
        String out = s;
        for (Pattern p : PATS) out = p.matcher(out).replaceAll("$1=***REDACTED***");
        return out;
    }
}
