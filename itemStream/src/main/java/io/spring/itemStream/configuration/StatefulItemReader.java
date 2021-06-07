package io.spring.itemStream.configuration;

import org.springframework.batch.item.*;

import java.util.List;

public class StatefulItemReader implements ItemStreamReader {

    private final List<String> items;
    private int currentIndex = -1;
    private boolean restart = false;

    public StatefulItemReader(List<String> items) {
        this.items = items;
        this.currentIndex = 0;
    }


    @Override
    public Object read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

        String item = null;

        if(this.currentIndex < this.items.size()){
            item = this.items.get(this.currentIndex);
            this.currentIndex++;
        }

        if(this.currentIndex == 42 && !restart){
            throw new RuntimeException("Run Time Exception");
        }

        return item;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {

        if(executionContext.containsKey("currentIndex")){
            this.currentIndex = executionContext.getInt("currentIndex");
            this.restart = true;
        }else{
            this.currentIndex = 0;
            executionContext.put("currentIndex", this.currentIndex);
        }


    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.put("currentIndex", this.currentIndex);
    }

    @Override
    public void close() throws ItemStreamException {
        // do nothing
    }
}
