package com.solumesl.aims.saas.dou.batch.integration;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.batch.integration.launch.JobLaunchingMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.DirectoryScanner;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.RecursiveDirectoryScanner;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.solumesl.aims.saas.adapter.util.SolumSaasConfigUtil;
import com.solumesl.aims.saas.dou.entity.DouglasMessage;
import com.solumesl.aims.saas.dou.service.DouglasDataTransferService;
import com.solumesl.aims.saas.dou.utils.DouglasUtils;

import lombok.extern.slf4j.Slf4j;


@Configuration
@EnableIntegration
@Slf4j
public class DouglasBatchJobLauncher {

    @Value("${dou-properties.integration.inbound-path:C:\\WORK\\DOU\\data\\inbound}")
    private String inboundPath;

    @Value("${dou-properties.integration.processing-path:C:\\WORK\\DOU\\data\\processing}")
    private String processingPath;

    @Value("${dou-properties.integration.archive-path:C:\\WORK\\DOU\\data\\archieve}")
    private String archivePath;
    @Value("${dou-properties.integration.saas.processing-path:C:\\WORK\\DOU\\data\\saas\\processing}")
    protected String saasProcesingPath;
    @Value("${dou-properties.integration.saas.output-path:C:\\WORK\\DOU\\data\\saas\\out}")
    protected String saasOutputPath;
    @Autowired
    @Qualifier("douArticleContentJob")
    Job douglasContentJob;

    @Autowired
    @Qualifier("douglasPakFileReaderJob")
    Job douglasPakFileReaderJob;

    @Autowired
    @Qualifier("douglasSaasOutputJob")
    Job douglasSaasOutputJob;
    @Autowired
    JobExplorer jobExplorer;
    @Autowired
    JobLauncher jobLauncher;

    @Value("${dou-properties.saas.company:DPH}")
    private String companyCode;
    private ExecutorService  orderExecutor = Executors.newFixedThreadPool(2,new CustomizableThreadFactory("orderExecutor-"));
    private Logger logger = LoggerFactory.getLogger(DouglasBatchJobLauncher.class);
    @Autowired
    private DouglasDataTransferService service;

    @Bean
    @InboundChannelAdapter(value = "loadArticleMasterDataFile", poller = @Poller(fixedDelay = "30"))
    public MessageSource<File> loadArticleMasterXmlFileForReading(DirectoryScanner directoryScannerForXmlPak) {
        FileReadingMessageSource messageSource = new FileReadingMessageSource();
        messageSource.setDirectory(new File(inboundPath));
        messageSource.setScanner(directoryScannerForXmlPak);
        return messageSource;
    }





    @Bean
    public DirectoryScanner directoryScannerForFiles() {
        DirectoryScanner scanner = new RecursiveDirectoryScanner();
        return scanner;
    }

    @Transformer(inputChannel = "loadArticleMasterDataFile", outputChannel = "movingInboundToProcessing")
    public File moveCSVToProcessing(File articleFile) {
        try {
            File masterArticleProcessingFile = new File(processingPath + File.separator + articleFile.getName());
            FileUtils.deleteQuietly(masterArticleProcessingFile);
            if(DouglasUtils.checkFileReady(articleFile))
                FileUtils.moveFile(articleFile, masterArticleProcessingFile);
            return masterArticleProcessingFile;
        } catch (IOException e) {
            logger.error("ERROR: {}", e);
        }
        return null;
    }

    @Transformer(inputChannel = "movingInboundToProcessing", outputChannel = "loadMasterFileToJob")
    public JobLaunchRequest transform(File inputFileName) {
        String fileName = inputFileName.getAbsolutePath();
        JobLaunchRequest request = null;
        if(fileName.contains(".xml")) {
            String[] requestIdArray = (FilenameUtils.removeExtension(fileName)).split("_");
            String saasOutputFileName = saasOutputPath + File.separator + requestIdArray[1] +".dat";
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("fileName", fileName)
                    .addString("outFileName", saasOutputFileName)
                    .addString("requestId", requestIdArray[1])
                    .addString("companyCode", companyCode)
                    .addDate("date", new Date())
                    .toJobParameters();
            request = new JobLaunchRequest(douglasContentJob, jobParameters);
        }else if(fileName.contains(".pak")) {
           
            request = createJobRequest(fileName, companyCode);
            cloneFile(inputFileName,fileName);
            
            
        }

        return request;
    }





    private JobLaunchRequest createJobRequest(String fileName, String companyCode) {
        JobLaunchRequest request;
        String[] requestIdArray = (FilenameUtils.removeExtension(fileName)).split("_");
        UUID groupId = UUID.randomUUID();
        String storeCode = requestIdArray[1];
        log.info("Received {}", storeCode);
        storeCode = storeCodeFromConfig(storeCode);
        log.info("Mapped to {}", storeCode);
        service.createDouglasMessage(groupId,storeCode , "PAK",companyCode);
        String saasOutputFileName = saasProcesingPath + File.separator + storeCode + "_"+ groupId.toString() +".dat";
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("fileName", fileName)
                .addString("outFileName", saasOutputFileName)
                .addString("requestId", storeCode)
                .addString("storeCode", storeCode)
                .addString("companyCode", companyCode)
                .addString("source", "PAK")
                .addDate("date", new Date())
                .toJobParameters();
        request = new JobLaunchRequest(douglasPakFileReaderJob, jobParameters);
        return request;
    }




    
    private void cloneFile(File inputFile, String fileName) {
        try {
            String[] requestIdArray = (FilenameUtils.removeExtension(fileName)).split("_");
            String storeCode = requestIdArray[1];
            String cloneStore = (String) SolumSaasConfigUtil.getMap("DphToDthMap").get(storeCode);
            if(cloneStore == null) {
                
            }else {
                File clonedFile = new File(processingPath + File.separator + inputFile.getName().replaceAll(storeCode, cloneStore));
                try {
                    FileUtils.copyFile(inputFile, clonedFile);
                    orderExecutor.execute(()->{
                        JobLaunchRequest job = createJobRequest(clonedFile.getAbsolutePath(), "DTH");
                        JobLaunchingMessageHandler jobLaunchingMessageHandler = new JobLaunchingMessageHandler(jobLauncher);
                        try {
                            jobLaunchingMessageHandler.launch(job);
                        } catch (JobExecutionException e) {
                          
                        }
                    });
             
                } catch (IOException e) {
                }
            }
        } catch (Exception e) {
           
        }
    }





    private String storeCodeFromConfig(String storeCode) {
        try {
            String store = (String) SolumSaasConfigUtil.getMap("StoreCodeMap").get(storeCode);
            if(store == null) {
                return storeCode;
            }
            return store;
        } catch (Exception e) {
        }
        return storeCode;
    }

    @Transformer(inputChannel = "prepareSaaSDatFile", outputChannel = "loadMasterFileToJob")
    public JobLaunchRequest saasJob(DouglasMessage   message) {
        log.info("JobNames {}",jobExplorer.getJobNames());
        JobLaunchRequest request = null;
        String fileName = saasProcesingPath + File.separator + message.getStoreCode() + "_"+ message.getGroupId() + ".dat";
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("outFileName", fileName)
                .addString("storeCode", message.getStoreCode())
                .addString("groupId", message.getGroupId())
                .addString("source", message.getSource())
                .addString("companyCode", message.getCompanyCode())
                .addDate("date", new Date())
                .toJobParameters();
        request = new JobLaunchRequest(douglasSaasOutputJob, jobParameters);

        return request;
    }
    
    @Bean
    @ServiceActivator(inputChannel = "loadMasterFileToJob", outputChannel = "nullChannel")
    protected JobLaunchingMessageHandler launcher(JobLauncher jobLauncher) {
        JobLaunchingMessageHandler jobLaunchingMessageHandler = new JobLaunchingMessageHandler(jobLauncher);
        return jobLaunchingMessageHandler;
    }

}
