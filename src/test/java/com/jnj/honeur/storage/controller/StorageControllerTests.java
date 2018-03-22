package com.jnj.honeur.storage.controller;

import com.jnj.honeur.storage.service.AmazonS3StorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(StorageController.class)
public class StorageControllerTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private AmazonS3StorageService storageService;
    @Autowired
    private StorageController storageController;

    @Test
    public void home() throws Exception {
        mvc.perform(get("/storage")
                .contentType(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("The HONEUR storage service is up and running :-)")))
                .andExpect(status().isOk());
    }

    @Test
    public void saveCohortDefinition() {
    }

    @Test
    public void getCohortDefinition() {
    }
}