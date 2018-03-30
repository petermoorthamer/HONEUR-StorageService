package com.jnj.honeur.storage.service;

import com.jnj.honeur.storage.model.StorageFileInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AmazonS3StorageServiceIT {

    @Autowired
    private AmazonS3StorageService amazonS3StorageService;

    @Test
    public void findByUuid() {
        List<StorageFileInfo> storageFileInfoList = new ArrayList<>();
        String uuid1 = UUID.randomUUID().toString();
        String uuid2 = UUID.randomUUID().toString();
        StorageFileInfo info1 = new StorageFileInfo(uuid1, "test1.txt");
        StorageFileInfo info2 = new StorageFileInfo(uuid2, "test2.txt");
        storageFileInfoList.add(info1);
        storageFileInfoList.add(info2);

        assertTrue(amazonS3StorageService.findByUuid(storageFileInfoList, uuid1).isPresent());
        assertTrue(amazonS3StorageService.findByUuid(storageFileInfoList, uuid2).isPresent());
        assertFalse(amazonS3StorageService.findByUuid(storageFileInfoList, UUID.randomUUID().toString()).isPresent());
    }

    @Test
    public void saveCohortDefinition() {
        List<StorageFileInfo> storageFileInfoList = amazonS3StorageService.getAllStorageFileInfo();
        assertNotNull(storageFileInfoList);
    }

}