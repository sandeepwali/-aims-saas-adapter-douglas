package com.solumesl.aims.saas.dou.batch;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.retry.backoff.ExponentialRandomBackOffPolicy;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.solumesl.aims.saas.dou.batch.listener.ArticleJobCompletionNotificationListener;
import com.solumesl.aims.saas.dou.batch.processor.ArticleItemProcessor;
import com.solumesl.aims.saas.dou.batch.writer.ArticleItemWriter;
import com.solumesl.aims.saas.dou.dto.ArticleDTO;
import com.solumesl.aims.saas.dou.entity.ArticleMaster;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class DouglasArticleMasterBatchConfiguration {


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
 
	@Bean
	@StepScope
	public StaxEventItemReader<ArticleDTO> douArticleItemReader(@Value("#{jobParameters[fileName]}") String resource) {
		return new StaxEventItemReaderBuilder<ArticleDTO>()
				.name("itemReader")
				.resource(new FileSystemResource(resource))
				.addFragmentRootElements("Artikel")
				.unmarshaller(jaxb2Marshaller())
				.build();

	}
	@Bean
	public Jaxb2Marshaller jaxb2Marshaller() {
		Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
		jaxb2Marshaller.setPackagesToScan("com.solumesl.aims.saas.dou.dto");
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("jaxb.formatted.output", true);
		jaxb2Marshaller.setMarshallerProperties(map);
		return jaxb2Marshaller;
	}


	@Bean
	@StepScope
	public ItemProcessor<ArticleDTO, ArticleMaster> articleItemProcessor() {
		return new ArticleItemProcessor();
	}

	@Bean
	@StepScope
	public ItemWriter<ArticleMaster> articleItemWriter() {
		return new ArticleItemWriter();
	}

	@Bean
	public AsyncItemProcessor<ArticleDTO, ArticleMaster> asyncArticleItemProcessor() {
		AsyncItemProcessor<ArticleDTO, ArticleMaster> asyncItemProcessor = new AsyncItemProcessor<>();
		asyncItemProcessor.setTaskExecutor(taskExecutor());
		asyncItemProcessor.setDelegate(articleItemProcessor());
		return asyncItemProcessor;
	}

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(corePoolSize);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.setThreadNamePrefix("MultiThreaded-");
		return executor;
	}

	@Bean
	@StepScope
	public AsyncItemWriter<ArticleMaster> asyncdouArticleWriter() {
		AsyncItemWriter<ArticleMaster> asyncItemWriter = new AsyncItemWriter<>();
		asyncItemWriter.setDelegate(articleItemWriter());
		return asyncItemWriter;
	}


	@Bean
	public Step processArticleFile() {
		return this.stepBuilderFactory.get("processArticleFile").<ArticleDTO, ArticleMaster>chunk(chunk)
				.reader(douArticleItemReader(null)).processor(articleItemProcessor())
				.writer(articleItemWriter()).build();
	}
	@Bean
	public Job douArticleContentJob(ArticleJobCompletionNotificationListener listener) {
		return jobBuilderFactory.get("douglasContentJob").incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(processArticleFile())
				.end().
				build();
	}
	@Bean
	public ExponentialRandomBackOffPolicy backOffPolicy() {
		ExponentialRandomBackOffPolicy expRandomBackOffPolicy = new ExponentialRandomBackOffPolicy();
		expRandomBackOffPolicy.setInitialInterval(1000);
		expRandomBackOffPolicy.setMaxInterval(30000);
		expRandomBackOffPolicy.setMultiplier(2);
		return expRandomBackOffPolicy;
	}


}




