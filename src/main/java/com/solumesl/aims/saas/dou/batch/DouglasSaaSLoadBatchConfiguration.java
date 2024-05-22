package com.solumesl.aims.saas.dou.batch;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Sort.Direction;

import com.solumesl.aims.saas.dou.batch.listener.SaasDataGenerationJobCompletionListener;
import com.solumesl.aims.saas.dou.batch.processor.ArticlePricingItemProcessor;
import com.solumesl.aims.saas.dou.batch.writer.ArticlePricingItemWriter;
import com.solumesl.aims.saas.dou.batch.writer.ArticlePricingUnpackItemWriter;
import com.solumesl.aims.saas.dou.batch.writer.CustomFieldExtractor;
import com.solumesl.aims.saas.dou.dto.ArticlePricing;
import com.solumesl.aims.saas.dou.dto.DouglasSaasData;
import com.solumesl.aims.saas.dou.dto.DouglasSaasDataWrapper;
import com.solumesl.aims.saas.dou.repository.ArticleMasterRepository;
import com.solumesl.aims.saas.dou.repository.PricingDataRepository;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class DouglasSaaSLoadBatchConfiguration {


	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;


	@Value("${dou-properties.integration.chunk:1000}")
	private Integer chunk;

	@Value("${dou-properties.integration.thread.core.pool.size:4}")
	private Integer corePoolSize;

	@Value("${dou-properties.integration.thread.max.pool.size:4}")
	private Integer maxPoolSize;

	@Value("${dou-properties.integration.thread.queue.capacity:50}")
	private Integer queueCapacity;
	@Value("${dou-properties.integration.chunk.retry.limit:214748364}")
	private Integer retryLimit;
	@Autowired
	private ArticleMasterRepository articleRepository;
	@Autowired
	private PricingDataRepository pricingDataRepository;
	@Bean
	public ArticlePricingItemProcessor saasProcessor() {
		return  new ArticlePricingItemProcessor();
	}
	@Bean
	@StepScope
	public ArticlePricingUnpackItemWriter itemWriter() {
		ArticlePricingUnpackItemWriter writer = new ArticlePricingUnpackItemWriter();
		writer.setDelegate(saasFlatItemWriter(null));
		return writer;

	}

	@Bean
	public ArticlePricingItemWriter itemWriterDoug() {
		return  new ArticlePricingItemWriter();

	}
	@Bean
	@StepScope
	public RepositoryItemReader<ArticlePricing> articleDataReader(@Value("#{jobParameters[storeCode]}") String storeCode) {

		Map<String, Direction> sortMap = new HashMap<>();

		return new RepositoryItemReaderBuilder<ArticlePricing>()
				.repository(articleRepository)
				.methodName("findMarkForTransferInArticles")
				.arguments(Arrays.asList(storeCode)).pageSize(1000)
				.sorts(sortMap)
				.saveState(false)
				.build();
	}
	@Bean
	@StepScope
	public RepositoryItemReader<ArticlePricing> pricingDataReader(@Value("#{jobParameters[storeCode]}") String storeCode) {

		Map<String, Direction> sortMap = new HashMap<>();

		return new RepositoryItemReaderBuilder<ArticlePricing>()
				.repository(pricingDataRepository)
				.methodName("findMarkForTransferInPricing")
				.arguments(Arrays.asList(storeCode)).pageSize(1000)
				.sorts(sortMap)
				.saveState(false)
				.build();
	}
	@Bean
	public Step articleToPricingStep() {
		return stepBuilderFactory.get("articleToPricingStep")
				.<ArticlePricing, DouglasSaasDataWrapper>chunk(chunk)
				.reader(articleDataReader(null))
				.processor(saasProcessor())
				.writer(itemWriter())
				.build();
	}
	@Bean
	public Step pricingToArticleStep() {
		return stepBuilderFactory.get("pricingToArticleStep")
				.<ArticlePricing, DouglasSaasDataWrapper>chunk(chunk)
				.reader(pricingDataReader(null))
				.processor(saasProcessor())
				.writer(itemWriter())
				.build();
	}
	@Bean
	public Job douglasSaasOutputJob(SaasDataGenerationJobCompletionListener listener) {
		return jobBuilderFactory.get("douglasSaasOutputJob").incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(articleToPricingStep()) 
				.end().
				build();
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<DouglasSaasData> saasFlatItemWriter(@Value("#{jobParameters[outFileName]}") String resource) 
	{
		FlatFileItemWriter<DouglasSaasData> writer = new FlatFileItemWriter<>();

		writer.setResource(new FileSystemResource(resource));

		writer.setAppendAllowed(true);

		writer.setLineAggregator(new DelimitedLineAggregator<DouglasSaasData>() {
			{
				setDelimiter(";");
				setFieldExtractor(new CustomFieldExtractor<DouglasSaasData>());
			}
		});
		return writer;
	}
}
