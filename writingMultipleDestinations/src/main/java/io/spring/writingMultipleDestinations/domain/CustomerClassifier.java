package io.spring.writingMultipleDestinations.domain;

import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

public class CustomerClassifier implements Classifier<Customer, ItemWriter<? super Customer>> {
    private ItemWriter<Customer> evenItemWriter;
    private ItemWriter<Customer> oddItemWriter;

    public CustomerClassifier(ItemWriter<Object> objectItemWriter, ItemWriter<Object> objectItemWriter1) {
        this.evenItemWriter = (ItemWriter)objectItemWriter;
        this.oddItemWriter = (ItemWriter)objectItemWriter1;
    }


    @Override
    public ItemWriter<? super Customer> classify(Customer customer) {
        return customer.getId() % 2 == 0 ? evenItemWriter : oddItemWriter;
    }
}
