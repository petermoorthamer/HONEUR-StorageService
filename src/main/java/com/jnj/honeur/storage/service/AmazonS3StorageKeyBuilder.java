package com.jnj.honeur.storage.service;


import com.jnj.honeur.storage.model.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * Builds the correct file key structure for all objects stored in an Amazon S3 bucket
 * @author Peter Moorthamer
 */

@Service
public class AmazonS3StorageKeyBuilder {

    private static final String FOLDER_SEPARATOR = "/";

    private static final String COHORT_DEFINITION_FOLDER = "cohort-definitions";
    private static final String COHORT_RESULTS_FOLDER = "cohort-results";
    private static final String NOTEBOOK_FOLDER = "notebooks";
    private static final String NOTEBOOK_RESULTS_FOLDER = "notebook-results";

    public String buildKey(final AbstractStorageFile storageFile) {
        return buildKey(storageFile.getClass(), storageFile.getPath());
    }

    public String buildKey(Class<? extends AbstractStorageFile> clazz, String... keyParts) {
        StringBuilder keyBuilder = new StringBuilder();
        keyBuilder.append(getRootFolder(clazz));
        keyBuilder.append(FOLDER_SEPARATOR);
        for(String keyPart : keyParts) {
            keyBuilder.append(keyPart);
            keyBuilder.append(FOLDER_SEPARATOR);
        }
        return keyBuilder.substring(0, keyBuilder.lastIndexOf(FOLDER_SEPARATOR));
    }

    private String getRootFolder(Class<? extends AbstractStorageFile> clazz) {
        if(CohortDefinitionFile.class.equals(clazz)) {
            return COHORT_DEFINITION_FOLDER;
        } else if(CohortInclusionResultsFile.class.equals(clazz)) {
            return COHORT_RESULTS_FOLDER;
        } else if(NotebookFile.class.equals(clazz)) {
            return NOTEBOOK_FOLDER;
        } else if(NotebookResultsFile.class.equals(clazz)) {
            return NOTEBOOK_RESULTS_FOLDER;
        } else {
            throw new IllegalArgumentException(String.format("Unknown StorageFile class %s", clazz));
        }
    }

    public Class<? extends AbstractStorageFile> getStorageFileClass(String fileKey) {
        if(fileKey.contains(COHORT_DEFINITION_FOLDER + "/")) {
            return CohortDefinitionFile.class;
        } else if(fileKey.contains(COHORT_RESULTS_FOLDER + "/")) {
            return CohortInclusionResultsFile.class;
        } else if(fileKey.contains(NOTEBOOK_FOLDER + "/")) {
            return NotebookFile.class;
        } else if(fileKey.contains(NOTEBOOK_RESULTS_FOLDER + "/")) {
            return NotebookResultsFile.class;
        } else {
            throw new IllegalArgumentException(String.format("No StorageFile class found for file key %s", fileKey));
        }
    }

    public String[] getKeyParts(String key) {
        return key.split(FOLDER_SEPARATOR);
    }

    public String[] getKeyPartsWithoutRootFolder(String key) {
        String[] keyParts = getKeyParts(key);
        return Arrays.copyOfRange(keyParts, 1, keyParts.length);
    }

    public String getFilename(String key) {
        return parseFilename(key);
    }

    private String parseFilename(String key) {
        return key.substring(key.lastIndexOf(FOLDER_SEPARATOR) + 1);
    }

    public String getUuid(String key) {
        return parseUuid(key);
    }

    private String parseUuid(String key) {
        return key.substring(key.lastIndexOf(FOLDER_SEPARATOR,key.lastIndexOf(FOLDER_SEPARATOR)-1) + 1, key.lastIndexOf(FOLDER_SEPARATOR));
    }

}
