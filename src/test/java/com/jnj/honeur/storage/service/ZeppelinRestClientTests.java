package com.jnj.honeur.storage.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ZeppelinRestClientTests {

    @Autowired
    private ZeppelinRestClient zeppelinRestClient;

    @Test
    public void isZeppelinServerRunning() throws IOException {
        assertTrue(zeppelinRestClient.isZeppelinServerRunning());
    }

    @Test
    public void getZeppelinServerApiVersion() throws IOException {
        assertEquals("0.7.3", zeppelinRestClient.getZeppelinServerApiVersion());
    }

    @Test
    public void downloadNotebook() throws IOException {
        final File tmpFile = File.createTempFile("zeppelin-notebook-test", ".json");
        zeppelinRestClient.downloadNotebook("2DAD71KN6", tmpFile);
        System.out.println("Donwload file : " + tmpFile.getAbsoluteFile());
        assertTrue(tmpFile.length() > 0);
    }

    @Test
    public void downloadNotebook2() throws IOException {
        final File tmpFile = File.createTempFile("zeppelin-notebook-test2", ".json");
        zeppelinRestClient.downloadNotebook2("2DAD71KN6", tmpFile);
        System.out.println("Donwload file : " + tmpFile.getAbsoluteFile());
        assertTrue(tmpFile.length() > 0);
    }
}