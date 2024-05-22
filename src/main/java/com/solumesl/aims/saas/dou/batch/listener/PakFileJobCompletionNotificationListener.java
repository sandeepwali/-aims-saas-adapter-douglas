package com.solumesl.aims.saas.dou.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

@Component
public class PakFileJobCompletionNotificationListener extends AbstractJobListener {

	private static final Logger log = LoggerFactory.getLogger(PakFileJobCompletionNotificationListener.class);


	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			moveFileFromProcessingToArchieve(jobExecution);
			moveFileToSaasOutFolder(jobExecution);
			String storeCode = getDataFromJobParamByKey(jobExecution, "storeCode");
			pricingDataRepository.updateMarkForTransfer(storeCode);
			String groupId = getDataFromJobParamByKey(jobExecution, "groupId");
			douglasMessageStatusRepository.updateStatus(storeCode, groupId);
			log.info("Ended PAK file processing for store {}", storeCode);
		}
	}



}
