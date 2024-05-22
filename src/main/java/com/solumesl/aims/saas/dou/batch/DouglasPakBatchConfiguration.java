package com.solumesl.aims.saas.dou.batch;

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
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;

import com.solumesl.aims.saas.dou.batch.listener.PakFileJobCompletionNotificationListener;
import com.solumesl.aims.saas.dou.batch.processor.PakItemProcessor;
import com.solumesl.aims.saas.dou.batch.writer.PricingItemWriter;
import com.solumesl.aims.saas.dou.dto.PricingDataDTO;
import com.solumesl.aims.saas.dou.entity.PricingData;

import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableBatchProcessing
@Slf4j
public class DouglasPakBatchConfiguration {


	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	@Qualifier("pricingToArticleStep")
	public Step pricingToArticleStep;

	@Autowired
	private TaskExecutor taskExecutor;
	@Value("${dou-properties.integration.chunk:10000}")
	private Integer chunk;
	

	@Bean
	@StepScope
	public ItemProcessor<PricingDataDTO, PricingData> pakItemProcessor() {
		return new PakItemProcessor();
	}

	@Bean
	@StepScope
	public PricingItemWriter pricingItemWriter() {
		return new PricingItemWriter();
	}

	@Bean
	public AsyncItemProcessor<PricingDataDTO, PricingData> asyncPakItemProcessor() {
		AsyncItemProcessor<PricingDataDTO, PricingData> asyncItemProcessor = new AsyncItemProcessor<>();
		asyncItemProcessor.setTaskExecutor(taskExecutor);
		asyncItemProcessor.setDelegate(pakItemProcessor());
		return asyncItemProcessor;
	}



	@Bean
	@StepScope
	public AsyncItemWriter<PricingData> asyncdouPakItemWriter() {
		AsyncItemWriter<PricingData> asyncItemWriter = new AsyncItemWriter<>();
		asyncItemWriter.setDelegate(pricingItemWriter());
		return asyncItemWriter;
	}


	@Bean
	public Step processPakFile() {
		return this.stepBuilderFactory.get("processPakFile").<PricingDataDTO, PricingData>chunk(chunk)
				.reader(pakFileReader(null)).processor(pakItemProcessor())
				.writer(pricingItemWriter()).build();
	}
	
	@Bean
	public Job douglasPakFileReaderJob(PakFileJobCompletionNotificationListener listener) {
		return jobBuilderFactory.get("douglasPakFileReaderJob").incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(processPakFile()).next(pricingToArticleStep)
				.end().
				build();
	}
	

	@Bean
	@StepScope
	public FlatFileItemReader<PricingDataDTO> pakFileReader(@Value("#{jobParameters[fileName]}") String resource) 
	{
		FlatFileItemReader<PricingDataDTO> reader = new FlatFileItemReader<PricingDataDTO>();

		reader.setResource(new FileSystemResource(resource));

		reader.setLinesToSkip(1);   

		//Configure how each line will be parsed and mapped to different values
		reader.setLineMapper(new DefaultLineMapper() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setDelimiter(";");
						setNames(new String[] {"Update","ArtikelNr","Preis aktuelle","Preis Liste","Grundpreiskennzeichen","Grundpreis","Vergleichsinhalt","Mengeneinheit","Hintergrundfarbe"});
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<PricingDataDTO>() {
					{
						setTargetType(PricingDataDTO.class);
					}
				});
			}
		});
		return reader;
	}
}
