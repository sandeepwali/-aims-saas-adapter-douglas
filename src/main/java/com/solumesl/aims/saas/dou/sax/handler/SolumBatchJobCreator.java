package com.solumesl.aims.saas.dou.sax.handler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.solumesl.aims.saas.dou.batch.listener.ArticleJobCompletionNotificationListener;

@Component
public class SolumBatchJobCreator {
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	@Qualifier("processArticleFile")
	private Step processArticleFile;
	
	@Autowired
	JobLauncher jobLauncher;
	
	@Autowired
	private  ArticleJobCompletionNotificationListener jobResultListener;
	
	
	public void launch(Job job, JobParameters jobParameters) throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
		jobLauncher.run(job, jobParameters);
	}

	public Job createJobForArticle(String jobName) {
		return this.jobBuilderFactory.get(jobName).start(processArticleFile).listener(jobResultListener).build();
	}
}
