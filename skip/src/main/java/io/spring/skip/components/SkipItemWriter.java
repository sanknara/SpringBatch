package io.spring.skip.components;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class SkipItemWriter implements ItemWriter<String> {

    private boolean skip = false;
    private int attemptCount = 0;

    @Override
    public void write(List<? extends String> items) throws Exception {
        for(String item : items) {
            System.out.println("Writing item : "+item);
            if (skip && item.equalsIgnoreCase("-84")) {
                attemptCount++;
                if (attemptCount >= 5) {
                    System.out.println("Success!");
                    skip = false;
                    System.out.println(item);
                } else {
                    System.out.println("Writing of item :" + item + " failed");
                    throw new CustomRetryableException("Write failed. Attempt: " + attemptCount);
                }
            } else {
                System.out.println(item);
            }
        }
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }
}
