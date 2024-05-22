package com.solumesl.aims.saas.dou.batch.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.stereotype.Component;

import com.solumesl.aims.saas.dou.entity.DouglasMessage;

@Component
public class SaasDataGenerationJobCompletionListener extends AbstractJobListener {

	private static final Logger log = LoggerFactory.getLogger(SaasDataGenerationJobCompletionListener.class);



	@Override
	public void afterJob(JobExecution jobExecution) {
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			String storeCode = getDataFromJobParamByKey(jobExecution,"storeCode");
			if(!"ARTICLE".equals(getDataFromJobParamByKey(jobExecution, "source")))
				pricingDataRepository.updateMarkForTransfer(storeCode);
			else {
				String groupId = getDataFromJobParamByKey(jobExecution, "groupId");
				douglasMessageStatusRepository.updateStatus(storeCode, groupId);
				List<DouglasMessage> findByGroupId = douglasMessageStatusRepository.findByGroupId(groupId);
				if(findByGroupId.stream().allMatch(a->a.isStatus())) {
					articleMasterRepository.updateMarkForTransfer();
				}
			}
			moveFileToSaasOutFolder(jobExecution);
			log.info("Ended async batch job for Saas dat generation {}",storeCode);
		}
	}






}
