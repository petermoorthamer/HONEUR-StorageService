package com.jnj.honeur.storage.model;

import java.io.File;

/**
 * Represents a (stored or to be stored) cohort definition file
 * @author Peter Moorthamer
 */
public class CohortDefinitionFile extends AbstractStorageFile {

    public CohortDefinitionFile(StorageFileInfo fileInfo, File file) {
        setFileInfo(fileInfo);
        setFile(file);
    }

}
