package com.jnj.honeur.storage.service;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StorageServiceTest {

    @Test
    public void buildFileName() {
        String filename = "test.txt";
        String uuid = UUID.randomUUID().toString();
        String newFilename = StorageService.buildFileName(filename, uuid);
        assertEquals("test_" + uuid + ".txt", newFilename);
    }

    @Test
    public void createTempFile() throws IOException {
        File tempFile = StorageService.createTempFile("test.txt");
        assertTrue(tempFile.exists());
        System.out.println(tempFile.getAbsolutePath());
    }
}