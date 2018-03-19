package com.jnj.honeur.storage.service;

import com.jnj.honeur.storage.model.*;
import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AmazonS3StorageKeyBuilderTest {

    private AmazonS3StorageKeyBuilder keyBuilder = new AmazonS3StorageKeyBuilder();
    private String uuid = UUID.randomUUID().toString();
    private File testFile = new File("The Zen of Python.json");

    @Test
    public void buildCohortDefinitionKeyPrefix() {
        String keyPrefix = keyBuilder.buildKey(CohortDefinitionFile.class, uuid);
        assertEquals("cohort-definitions/" + uuid, keyPrefix);
    }

    @Test
    public void buildKeyCohortDefinition() {
        CohortDefinitionFile cohortDefinitionFile = new CohortDefinitionFile(new StorageFileInfo(uuid, testFile.getName()), testFile);
        String key = keyBuilder.buildKey(cohortDefinitionFile);
        assertEquals("cohort-definitions/" + uuid + "/" + testFile.getName(), key);
    }

    @Test
    public void buildCohortResultsKeyPrefix() {
        String keyPrefix = keyBuilder.buildKey(CohortInclusionResultsFile.class, uuid);
        assertEquals("cohort-results/" + uuid, keyPrefix);
    }

    @Test
    public void buildKeyCohortInclusionResults() {
        String resultsUuid = UUID.randomUUID().toString();
        CohortInclusionResultsFile cohortInclusionResultsFile = new CohortInclusionResultsFile(uuid, new StorageFileInfo(resultsUuid, testFile.getName()), testFile);
        String key = keyBuilder.buildKey(cohortInclusionResultsFile);
        assertEquals("cohort-results/" + uuid + "/" + resultsUuid + "/" + testFile.getName(), key);
    }

    @Test
    public void buildNotebookKeyPrefix() {
        String keyPrefix = keyBuilder.buildKey(NotebookFile.class, "Study X", uuid);
        assertEquals("notebooks/Study X/" + uuid, keyPrefix);
    }

    @Test
    public void buildKeyNotebook() {
        NotebookFile notebookFile = new NotebookFile("Study X", new StorageFileInfo(uuid, testFile.getName()), testFile);
        String key = keyBuilder.buildKey(notebookFile);
        assertEquals("notebooks/Study X/" + uuid + "/" + testFile.getName(), key);
    }

    @Test
    public void buildNotebookResultsKeyPrefix() {
        String keyPrefix = keyBuilder.buildKey(NotebookResultsFile.class, "Study X", uuid);
        assertEquals("notebook-results/Study X/" + uuid, keyPrefix);
    }

    @Test
    public void buildKeyNotebookResults() {
        String resultsUuid = UUID.randomUUID().toString();
        NotebookResultsFile notebookResultsFile = new NotebookResultsFile("Study X", uuid, new StorageFileInfo(resultsUuid, testFile.getName()), testFile);
        String key = keyBuilder.buildKey(notebookResultsFile);
        assertEquals("notebook-results/Study X/" + uuid + "/" + resultsUuid + "/" + testFile.getName(), key);
    }

    @Test
    public void getFilename() {
        assertEquals("The Zen of Python.json", keyBuilder.getFilename("notebook-results/Study X/b17c3bb3-dba1-4be4-8ced-8d9128007a4e/82829b4a-96a1-4407-8632-75b77fdb860c/The Zen of Python.json"));
    }

    @Test
    public void getUuid() {
        assertEquals("82829b4a-96a1-4407-8632-75b77fdb860c", keyBuilder.getUuid("notebook-results/Study X/b17c3bb3-dba1-4be4-8ced-8d9128007a4e/82829b4a-96a1-4407-8632-75b77fdb860c/The Zen of Python.json"));
    }
}