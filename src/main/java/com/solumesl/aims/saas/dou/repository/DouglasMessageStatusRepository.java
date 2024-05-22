package com.solumesl.aims.saas.dou.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.solumesl.aims.saas.dou.entity.DouglasMessage;
import com.solumesl.aims.saas.dou.entity.DouglasMessagePk;

public interface DouglasMessageStatusRepository extends JpaRepository<DouglasMessage,DouglasMessagePk>{
	 
	@Transactional
	@Modifying(clearAutomatically = true)
    @Query("UPDATE DouglasMessage c SET c.status = true WHERE c.groupId = :groupId and  c.storeCode = :storeCode")
    int updateStatus(@Param("storeCode") String storeCode, @Param("groupId") String groupId );
	
	List<DouglasMessage> findByStoreCodeAndGroupId(String storeCode, String groupId);

	List<DouglasMessage> findByGroupId(String groupId);
 
} 