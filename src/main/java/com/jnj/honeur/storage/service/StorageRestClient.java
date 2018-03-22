package com.jnj.honeur.storage.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

public class StorageRestClient {

    public void postCohortResults() throws IOException {
        final LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        //map.add("file", new ClassPathResource("The Zen of Python.json"));
        map.add("file", new FileSystemResource(createFile().getAbsolutePath()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);

        String uuid = UUID.randomUUID().toString();
        String url = "https://localhost:8444/cohort-results/" + uuid;

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class);

        System.out.println(response);
    }

    private File createFile() throws IOException {
        File file = File.createTempFile("test", ".txt");
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("test");
        fileWriter.close();
        return file;
    }

    public static void main(String[] args) {
        try {
            StorageRestClient restClient = new StorageRestClient();
            restClient.postCohortResults();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
