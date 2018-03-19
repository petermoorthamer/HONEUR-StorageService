package com.jnj.honeur.storage.model;

import org.junit.Test;

import java.io.File;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StorageFileBuilderTest {

    private static final String TEST_FILENAME = "test.txt";

    private File testFile = new File(TEST_FILENAME);
    private UUID uuid = UUID.randomUUID();

    @Test
    public void buildCohortDefinitionFile() {
        CohortDefinitionFile cohortDefinitionFile = StorageFileBuilder.buildStorageFile(CohortDefinitionFile.class, testFile, uuid.toString(), testFile.getName());
        assertNotNull(cohortDefinitionFile);
        assertEquals(uuid.toString(), cohortDefinitionFile.getUuid());
        assertEquals(TEST_FILENAME, cohortDefinitionFile.getOriginalFilename());
        assertEquals(testFile, cohortDefinitionFile.getFile());
    }

    @Test
    public void buildCohortInclusionResultsFile() {
        String cohortDefinitionUuid = UUID.randomUUID().toString();
        CohortInclusionResultsFile cohortInclusionResultsFile = StorageFileBuilder.buildStorageFile(CohortInclusionResultsFile.class, testFile, cohortDefinitionUuid, uuid.toString(), testFile.getName());
        assertNotNull(cohortInclusionResultsFile);
        assertEquals(cohortDefinitionUuid, cohortInclusionResultsFile.getCohortDefinitionUuid());
        assertEquals(uuid.toString(), cohortInclusionResultsFile.getUuid());
        assertEquals(TEST_FILENAME, cohortInclusionResultsFile.getOriginalFilename());
        assertEquals(testFile, cohortInclusionResultsFile.getFile());
    }

    @Test
    public void buildNotebookFile() {
        String studyId = "studyX";
        NotebookFile notebookFile = StorageFileBuilder.buildStorageFile(NotebookFile.class, testFile, studyId, uuid.toString(), testFile.getName());
        assertNotNull(notebookFile);
        assertEquals(studyId, notebookFile.getStudyId());
        assertEquals(uuid.toString(), notebookFile.getUuid());
        assertEquals(TEST_FILENAME, notebookFile.getOriginalFilename());
        assertEquals(testFile, notebookFile.getFile());
    }

    @Test
    public void buildNotebookResultsFile() {
        String studyId = "studyX";
        String notebookUuid = UUID.randomUUID().toString();
        NotebookResultsFile notebookResultsFile = StorageFileBuilder.buildStorageFile(NotebookResultsFile.class, testFile, studyId, notebookUuid, uuid.toString(), testFile.getName());
        assertNotNull(notebookResultsFile);
        assertEquals(studyId, notebookResultsFile.getStudyId());
        assertEquals(notebookUuid, notebookResultsFile.getNotebookUuid());
        assertEquals(uuid.toString(), notebookResultsFile.getUuid());
        assertEquals(TEST_FILENAME, notebookResultsFile.getOriginalFilename());
        assertEquals(testFile, notebookResultsFile.getFile());
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildStorageFileInvalidClass() {
        StorageFileBuilder.buildStorageFile(DummyStorageFile.class, testFile, uuid.toString(), testFile.getName());
    }


}

class DummyStorageFile extends AbstractStorageFile {}