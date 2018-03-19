package com.jnj.honeur.storage.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import static java.util.Arrays.asList;

/**
 * REST client consuming the REST API exposed by Zeppelin
 * @author Peter Moorthamer
 */

@Service
public class ZeppelinRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZeppelinRestClient.class);

    @Value("${zeppelin.server.url}")
    private String zeppelinServerApiBaseUrl = "http://localhost:8080";
    @Value("${zeppelin.api.version}")
    private String zeppelinVersionApi = "/api/version";
    @Value("${zeppelin.api.notebook.export}")
    private String zeppelinExportNotebookApi = "/api/notebook/export";
    @Value("${zeppelin.api.notebook.import}")
    private String zeppelinImportNotebookApi = "/api/notebook/import";
    private Gson gson = new Gson();

    public boolean isZeppelinServerRunning() throws IOException {
        GetMethod request = httpGet(zeppelinVersionApi);
        return request.getStatusCode() == HttpStatus.SC_OK;
    }

    public String getZeppelinServerApiVersion() throws IOException {
        GetMethod get = httpGet(zeppelinVersionApi);
        return getResponseBody(get);
    }

    public void downloadNotebook(String notebookId, File targetFile) throws IOException {
        final GetMethod get = httpGet(zeppelinExportNotebookApi + "/" + notebookId);
        if(get.getStatusCode() == HttpStatus.SC_OK) {
            String notebookJson = getResponseBody(get);
            final FileWriter fileWriter = new FileWriter(targetFile);
            fileWriter.write(notebookJson);
            fileWriter.close();
        }
    }

    private String getResponseBody(final GetMethod get) throws IOException {
        Map<String, Object> responseMap = responseToMap(get.getResponseBodyAsString());
        return (String)responseMap.get("body");
    }

    private Map<String, Object> responseToMap(String responseBody) {
        return gson.fromJson(responseBody, new TypeToken<Map<String, Object>>() {}.getType());
    }

    private GetMethod httpGet(String path) throws IOException {
        LOGGER.info("Connecting to {}", zeppelinServerApiBaseUrl + path);
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(zeppelinServerApiBaseUrl + path);
        getMethod.addRequestHeader("Origin", zeppelinServerApiBaseUrl);
        httpClient.executeMethod(getMethod);
        LOGGER.info("{} - {}", getMethod.getStatusCode(), getMethod.getStatusText());
        return getMethod;
    }

    public void downloadNotebook2(String notebookId, File targetFile) {

        String zeppelinExportNotebookUrl = zeppelinServerApiBaseUrl + zeppelinExportNotebookApi + "/" + notebookId;
        LOGGER.info("Zeppelin export notebook URL: " + zeppelinExportNotebookUrl);

        // Create a new RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Optional Accept header
        RequestCallback requestCallback = request -> request.getHeaders()
                .setAccept(asList(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL));

        // Streams the response instead of loading it all in memory
        ResponseExtractor<Void> responseExtractor = response -> {
            // Here I write the response to a file but do what you like
            Files.copy(response.getBody(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return null;
        };
        restTemplate.execute(URI.create(zeppelinExportNotebookUrl), HttpMethod.GET, requestCallback, responseExtractor);

/*
        // Set the Accept header
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<?> requestEntity = new HttpEntity<>(requestHeaders);

        // Add the Jackson message converter
        restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

        // Make the HTTP GET request, marshaling the response from JSON to an array of Objects
        ResponseEntity<Object[]> responseEntity = restTemplate.exchange(zeppelinExportNotebookUrl, HttpMethod.GET, requestEntity, Object[].class);
        Object[] events = responseEntity.getBody();

*/

        /*
        if (response.getStatusCode() == HttpStatus.OK) {
            Files.write(targetFile.toPath(), response.getBody());
        }*/

    }

    public static void main(String[] args) {
        ZeppelinRestClient restClient = new ZeppelinRestClient();
        try {
            restClient.isZeppelinServerRunning();
            System.out.println("Zeppelin API version: " + restClient.getZeppelinServerApiVersion());
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

}
