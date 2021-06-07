package io.spring.skipRetryListeners.components;

import org.springframework.batch.core.SkipListener;

public class CustomSkipListener implements SkipListener {

    @Override
    public void onSkipInRead(Throwable throwable) {

    }

    @Override
    public void onSkipInWrite(Object o, Throwable throwable) {
        System.out.println(">> Skipping "+ o + " because writing it caused error "+ throwable);
    }

    @Override
    public void onSkipInProcess(Object o, Throwable throwable) {
        System.out.println(">> Skipping "+ o + " because processing it caused error "+ throwable);
    }
}
