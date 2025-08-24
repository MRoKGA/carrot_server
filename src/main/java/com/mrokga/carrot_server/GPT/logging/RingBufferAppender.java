package com.mrokga.carrot_server.GPT.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.util.ArrayDeque;
import java.util.Deque;

public class RingBufferAppender extends AppenderBase<ILoggingEvent> {
    private static final int CAP = 400; // 최근 400줄 보관
    private static final Deque<String> BUF = new ArrayDeque<>(CAP);

    @Override protected void append(ILoggingEvent e) {
        synchronized (BUF) {
            if (BUF.size() == CAP) BUF.removeFirst();
            BUF.addLast(e.getFormattedMessage());
        }
    }
    public static String tail() {
        synchronized (BUF) { return String.join("\n", BUF); }
    }
}
