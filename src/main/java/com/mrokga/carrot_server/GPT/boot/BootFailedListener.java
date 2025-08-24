package com.mrokga.carrot_server.GPT.boot;

import com.mrokga.carrot_server.GPT.ai.ErrorExplainService;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class BootFailedListener implements ApplicationListener<ApplicationFailedEvent> {
    private final ErrorExplainService gpt;
    public BootFailedListener(ErrorExplainService gpt) { this.gpt = gpt; }

    @Override public void onApplicationEvent(ApplicationFailedEvent event) {
        String analysis = gpt.explain(event.getException());
        System.err.println("\n=== GPT 에러 분석 ===\n" + analysis + "\n====================\n");
    }
}
