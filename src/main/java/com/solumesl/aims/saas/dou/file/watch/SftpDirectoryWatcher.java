package com.solumesl.aims.saas.dou.file.watch;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.solumesl.aims.saas.dou.config.DouglasConfig;
import com.solumesl.aims.saas.dou.sax.handler.SolumBatchJobCreator;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class SftpDirectoryWatcher{

	@Autowired
	private SolumBatchJobCreator jobCreator;
	@Autowired
	private DouglasConfig douglasConfig;

	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	private void watcher() throws IOException, InterruptedException {
		log.info("File Watcher running for the Directory {}",douglasConfig.getSftpDirectory() );

		WatchService watchService = FileSystems.getDefault().newWatchService();

		Path path = Paths.get(douglasConfig.getSftpDirectory());

		path.register(watchService,  StandardWatchEventKinds.ENTRY_CREATE);
		WatchKey key;
		while ((key = watchService.take()) != null) {
			for (WatchEvent<?> event : key.pollEvents()) {
				String context = "C:\\WORK\\DOU\\20220819_0111_Artikel\\20220819_0111_Artikel.xml";
				log.info("Event kind:{} .File affected:{}",event.kind(),context );
				if(checkFileReady(key, event)) {
					Job job =	jobCreator.createJobForArticle(LocalDateTime.now().format(formatter));
					JobParameters jobParameters = new JobParametersBuilder().addLong("time", new Date().getTime()).addString("fileName", context).toJobParameters();
					try {
						jobCreator.launch(job, jobParameters);
					} catch (JobExecutionAlreadyRunningException e) {
						log.error("JobExecutionAlreadyRunningException {}",e.getMessage());
					} catch (JobRestartException e) {
						log.error("JobRestartException {}",e.getMessage());
					} catch (JobInstanceAlreadyCompleteException e) {
						log.error("JobInstanceAlreadyCompleteException {}",e.getMessage());
					} catch (JobParametersInvalidException e) {
						log.error("JobParametersInvalidException {}",e.getMessage());
					}
				}
			}
			key.reset();
		}
	}
	private boolean checkFileReady(WatchKey key, WatchEvent<?> event) {
		Path dir = (Path)key.watchable();
		Path fullPath = dir.resolve((Path) event.context());
		try
		{
			long size1 = 1L;
			log.info(fullPath.toAbsolutePath() + " Size: " + size1 + " Byte");
			long size2 = 0L;

			while(size1 != size2)
			{
				size1 = Files.size(fullPath);
				TimeUnit.MILLISECONDS.sleep(1000);
				size2 = Files.size(fullPath);
				log.info(fullPath.toAbsolutePath() + " =>Upload Size: " + size1 + " Byte versus Size:" + size2 + " Byte");
			}
		} catch (NumberFormatException | IOException | InterruptedException e)
		{
			log.error("checkFileReady {}",e.getMessage());
			return false;
		}
		return true;
	}
	private boolean checkFileReady1(WatchKey key, WatchEvent<?> event) {
		Path dir = (Path)key.watchable();
		Path fullPath = dir.resolve((Path) event.context());
		try
		{
			long size1 = 1L;
			log.info(fullPath.toAbsolutePath() + " Size: " + size1 + " Byte");
			long size2 = 0L;

			while(size1 != size2)
			{
				size1 = Files.size(fullPath);
				TimeUnit.MILLISECONDS.sleep(1000);
				size2 = Files.size(fullPath);
				log.info(fullPath.toAbsolutePath() + " =>Upload Size: " + size1 + " Byte versus Size:" + size2 + " Byte");
			}
		} catch (NumberFormatException | IOException | InterruptedException e)
		{
			log.error("checkFileReady {}",e.getMessage());
			return false;
		}
		return true;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void doSomethingAfterStartup() throws IOException, InterruptedException {
		watcher();
	}


}