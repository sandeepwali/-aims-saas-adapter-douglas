package com.solumesl.aims.saas.dou.batch.writer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.solumesl.aims.saas.dou.dto.DouglasSaasData;
import com.solumesl.aims.saas.dou.dto.DouglasSaasDataWrapper;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ArticlePricingUnpackItemWriter implements ItemWriter<DouglasSaasDataWrapper>, ItemStream, InitializingBean {

    private FlatFileItemWriter<DouglasSaasData> delegate;
    @Override
    public void write(List<? extends DouglasSaasDataWrapper> items) throws Exception {
        final List<DouglasSaasData> consolidatedList = new ArrayList<>();
        if(items!=null && !items.isEmpty()) {
            for (final DouglasSaasDataWrapper list : items) {
                if(list!=null) {
                    consolidatedList.addAll(list.getData());
                }
            }
            log.debug("Transformed size {}",consolidatedList.size());
            if(consolidatedList.size()>0)
                delegate.write(consolidatedList);
        }

    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(delegate, "You must set a delegate!");
    }

    @Override
    public void open(ExecutionContext executionContext) {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).open(executionContext);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).update(executionContext);
        }
    }

    @Override
    public void close() {
        if (delegate instanceof ItemStream) {
            ((ItemStream) delegate).close();
        }
    }

    public void setDelegate(FlatFileItemWriter<DouglasSaasData> delegate) {
        this.delegate = delegate;
    }


}