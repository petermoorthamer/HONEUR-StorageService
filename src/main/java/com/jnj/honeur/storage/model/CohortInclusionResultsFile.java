package com.jnj.honeur.storage.model;

import java.io.File;

/**
 * Represents a (stored or to be stored) cohort definition results file
 * @author Peter Moorthamer
 */
public class CohortInclusionResultsFile extends AbstractStorageFile {

    private String cohortDefinitionUuid;

    public CohortInclusionResultsFile(String cohortDefinitionUuid, StorageFileInfo fileInfo, File file) {
        this.cohortDefinitionUuid = cohortDefinitionUuid;
        setFileInfo(fileInfo);
        setFile(file);
    }

    public String getCohortDefinitionUuid() {
        return cohortDefinitionUuid;
    }
    public void setCohortDefinitionUuid(String cohortDefinitionUuid) {
        this.cohortDefinitionUuid = cohortDefinitionUuid;
    }

    @Override
    public String[] getPath() {
        return new String[] { getCohortDefinitionUuid(), getUuid(), getOriginalFilename() };
    }

}
