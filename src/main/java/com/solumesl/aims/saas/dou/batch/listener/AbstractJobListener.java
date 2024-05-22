package com.solumesl.aims.saas.dou.batch.listener;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.solumesl.aims.saas.dou.model.BulkArticle;
import com.solumesl.aims.saas.dou.repository.ArticleMasterRepository;
import com.solumesl.aims.saas.dou.repository.DouglasMessageStatusRepository;
import com.solumesl.aims.saas.dou.repository.PricingDataRepository;
import com.solumesl.aims.saas.dou.service.DouglasDataTransferService;
import com.solumesl.aims.saas.dou.utils.DouglasUtils;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public abstract class AbstractJobListener extends JobExecutionListenerSupport {

	@Value("${dou-properties.integration.saas.processing-path:C:\\WORK\\DOU\\data\\saas\\processing}")
	protected String saasProcesingPath;
	@Value("${dou-properties.integration.saas.output-path:C:\\WORK\\DOU\\data\\saas\\out}")
	protected String saasOutputPath;
	@Autowired
	protected DouglasDataTransferService douglasDataTransferService;

	@Value("${dou-properties.integration.processing-path:C:\\WORK\\DOU\\data\\processing}")
	protected String processingPath;

	@Value("${dou-properties.integration.archive-path:C:\\WORK\\DOU\\data\\archieve}")
	protected String archivePath;

	@Autowired
	protected PricingDataRepository pricingDataRepository;

	@Autowired
	protected DouglasMessageStatusRepository douglasMessageStatusRepository;
	@Autowired
	protected ArticleMasterRepository articleMasterRepository;

	@Override
	public void beforeJob(JobExecution jobExecution) {
		log.info("Started async batch job for douglas processing");
	}

	protected void moveFileToSaasOutFolder(JobExecution jobExecution) {
		File inputFile = new File(getDataFromJobParamByKey(jobExecution, "outFileName"));
		File outFile = new File(saasOutputPath + File.separator + inputFile.getName());
		try {
			log.debug("Moving file to out {} folder", inputFile);
			FileUtils.moveFile(inputFile, outFile);

			pushFileToSaas(outFile, jobExecution);
		} catch (IOException e) {
			log.error("Error in moveFileToSaasOutFolder {}", e);
		}
	}
	protected void pushFileToSaas(File outFile, JobExecution jobExecution) {
		BulkArticle bulkArticle = new BulkArticle();
		bulkArticle.setArticleFileName(outFile.getName());
		if(outFile.getName().endsWith(".dat")) {
			try {
			
				if(outFile.length() > 0) {
				    String zipFileName = DouglasUtils.zipFile(outFile.getAbsolutePath());
	                bulkArticle.setZipFileBase64(DouglasUtils.convertZipFileToBaseEncodeString(zipFileName));
				    douglasDataTransferService.pushFileToSaas( getDataFromJobParamByKey(jobExecution, "companyCode"), bulkArticle);
				}else {
				    log.info("File size is empty {}", outFile.getName());
				}
				
			} catch (IOException e) {
			}
		}
	}
	protected String getDataFromJobParamByKey(JobExecution jobExecution, String key) {
		JobParameters jobParameters = jobExecution.getJobParameters();
		String storeCode = jobParameters.getString(key);
		return storeCode;
	}
	protected void moveFileFromProcessingToArchieve(JobExecution jobExecution) {
		String fileName = getDataFromJobParamByKey(jobExecution, "fileName");
		File file = new File(fileName);
		log.debug("fileName " + fileName);
		File inputfile = new File(archivePath + File.separator + file.getName());
		try {
			log.debug("Moving file to archive {} folder", file);
			FileUtils.moveFile(file, inputfile);
		} catch (IOException e) {
			log.error("Error in job {}", e);
		}
	}
}
