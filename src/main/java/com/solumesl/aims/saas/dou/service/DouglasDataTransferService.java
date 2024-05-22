package com.solumesl.aims.saas.dou.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Service;

import com.solumesl.aims.saas.adapter.constants.SaasConstants;
import com.solumesl.aims.saas.adapter.service.SaasRestService;
import com.solumesl.aims.saas.adapter.util.SolumSaasConfigUtil;
import com.solumesl.aims.saas.adapter.util.SolumSaasUtil;
import com.solumesl.aims.saas.dou.entity.DouglasMessage;
import com.solumesl.aims.saas.dou.model.BulkArticle;
import com.solumesl.aims.saas.dou.repository.DouglasMessageStatusRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DouglasDataTransferService {

    @Autowired
    private SaasRestService saasRestService;
    @Autowired
    @Qualifier("prepareSaaSDatFile")
    private MessageChannel saasMessageChannel;
    @Value("${dou-properties.saas.company:DTH}")
    private String companyCode;
    @Autowired
    private DouglasMessageStatusRepository douglasMessageStatusRepository;

    public CompletableFuture<?> pushFileToSaas(String company, BulkArticle bulkArticle) {
        return saasRestService.articlePushByZipFile(company, bulkArticle );
    }

    public CompletableFuture<?> getArticleFormat(String company) {
        Map<String, String> articleFormatQueryMap = new HashMap<>();
        articleFormatQueryMap.put(SaasConstants.COMPANY, company);
        CompletableFuture<?> format = saasRestService.getArticleFormat(articleFormatQueryMap);
        return format;
    }

    public void triggerDatFileGenerationByStore() {
        Map<String, String> queryMap = new HashMap<>();
        int page = 0;int  size = 200;
        queryMap.put("size",String.valueOf(size));
        queryMap.put("page",String.valueOf(page));
        CompletableFuture<?> result = saasRestService.getStore(companyCode, queryMap);
        try {

            List<Object> storeIds = getStores(result);

            if(SolumSaasUtil.isNotEmpty(storeIds)) {
                log.info("Stores found {}", storeIds);
                createStoresMessage( storeIds, companyCode);
                try {
                    createStoresMessage(SolumSaasConfigUtil.getMap("DphToDthMap").values().stream().collect(Collectors.toCollection(ArrayList::new)),"DTH");
                } catch (Exception e) {
                
                }
            }else {
                log.info("No Stores found ");
            }
        } catch (InterruptedException | ExecutionException e) { }
    }

    private void createStoresMessage(List<Object> storeIds, String companyCode ) {
        UUID requestId = UUID.randomUUID();
        List<Message<DouglasMessage>> messagesList = new ArrayList<>();
        storeIds.forEach(storeId->{
            DouglasMessage douglasMessage = createDouglasMessage(requestId, storeId, "ARTICLE", companyCode);
            Message<DouglasMessage> message = MessageBuilder.withPayload(douglasMessage).build();
            messagesList.add(message);
        });
        messagesList.forEach(a->{
            saasMessageChannel.send(a);
        });
    }

    public DouglasMessage createDouglasMessage(UUID requestId, Object storeId, String source, String companyCode) {
        DouglasMessage douglasMessage = new DouglasMessage();
        douglasMessage.setStoreCode((String) storeId);
        douglasMessage.setGroupId(requestId.toString());
        douglasMessage.setSource(source);
        douglasMessage.setCompanyCode(companyCode);
        douglasMessageStatusRepository.save(douglasMessage);
        return douglasMessage;
    }

    private List<Object> getStores(CompletableFuture<?> stores) throws InterruptedException, ExecutionException {
        @SuppressWarnings("unchecked")
        Map<String,Object> storesMap =   (Map<String,Object>) stores.get();
        List<Object> storeIds = getStoreIds(Optional.ofNullable(storesMap));
        return storeIds;
    }
    private List<Object> getStoreIds(Optional<Map<String, Object>> storesMap) {
        if(storesMap.isPresent()) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> storesList = (List<Map<String,Object>>) storesMap.get().get(SaasConstants.STORES);
            List<Object> storeIds = getStoreList(storesList);
            return storeIds;
        }else {
            return null;
        }

    }
    private List<Object> getStoreList(List<Map<String, Object>> storesList) {
        List<Object> storeIds = storesList.stream().map(a->a.get(SaasConstants.STORE)).collect(Collectors.toList());
        return storeIds;
    }

}
