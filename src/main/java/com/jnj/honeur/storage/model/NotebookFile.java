package com.jnj.honeur.storage.model;

import java.io.File;

/**
 * Represents a (stored or to be stored) notebook file
 * @author Peter Moorthamer
 */
public class NotebookFile extends AbstractStorageFile {

    private String studyId;

    public NotebookFile(String studyId, StorageFileInfo fileInfo, File file) {
        this.studyId = studyId;
        setFileInfo(fileInfo);
        setFile(file);
    }

    public String getStudyId() {
        return studyId;
    }
    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    @Override
    public String[] getPath() {
        return new String[] { getStudyId(), getUuid(), getOriginalFilename() };
    }
}
