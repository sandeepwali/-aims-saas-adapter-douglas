package com.solumesl.aims.saas.dou.batch.listener;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ArticleJobCompletionNotificationListener extends AbstractJobListener {

	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			log.info("Ended async batch job for douglas processing");
			moveFileFromProcessingToArchieve(jobExecution);
			douglasDataTransferService.triggerDatFileGenerationByStore();
		}
		
	}

	@Override
	public void beforeJob(JobExecution jobExecution) {
		log.info("Started async batch job for douglas processing");
	}
}
