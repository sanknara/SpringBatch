package io.spring.skip.components;

import org.springframework.batch.item.ItemProcessor;

public class SkipItemProcessor implements ItemProcessor<String, String> {
    private boolean skip = false;
    private int attemptCount = 0;

    @Override
    public String process(String item) throws Exception {
        System.out.println("processing item : "+item);
        if(skip && item.equalsIgnoreCase("42")){
            attemptCount++;
            if(attemptCount >= 5){
                System.out.println("Success!");
                skip = false;
                return String.valueOf(Integer.valueOf(item) * -1);
            }else{
                System.out.println("Processing of item :"+item+" failed");
                throw new CustomRetryableException("Process failed. Attempt: "+attemptCount);
            }
        }else{
            return String.valueOf(Integer.valueOf(item) * -1);
        }
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }
}
