package com.solumesl.aims.saas.dou.batch.writer;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.solumesl.aims.saas.dou.entity.ArticleMaster;
import com.solumesl.aims.saas.dou.repository.ArticleMasterRepository;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class ArticleItemWriter implements ItemWriter<ArticleMaster> {
	@Autowired
	private ArticleMasterRepository articleMasterRepository;
	@Override
	public void write(List<? extends ArticleMaster> items) throws Exception {

		List<? extends ArticleMaster> deltaArticles = items.parallelStream()
				.filter(a-> !articleMasterRepository.existsByHashedCode(a.getHashedCode())).map(c->{
					c.setMarkForTransfer(true);
					return c;
				})
				.collect(Collectors.toList());
		log.info(" delta Size {}",deltaArticles.size());
		 
		 try {
			articleMasterRepository.saveAll(deltaArticles);
		} catch (Exception e) {
		log.error("{}",e.getMessage());
		}

	}

	

}