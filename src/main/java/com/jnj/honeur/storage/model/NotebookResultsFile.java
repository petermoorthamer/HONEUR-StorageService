package com.jnj.honeur.storage.model;

import java.io.File;

public class NotebookResultsFile extends AbstractStorageFile {

    private String studyId;
    private String notebookUuid;

    public NotebookResultsFile(String studyId, String notebookUuid, StorageFileInfo fileInfo, File file) {
        this.studyId = studyId;
        this.notebookUuid = notebookUuid;
        setFileInfo(fileInfo);
        setFile(file);
    }

    public String getStudyId() {
        return studyId;
    }
    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    public String getNotebookUuid() {
        return notebookUuid;
    }
    public void setNotebookUuid(String notebookUuid) {
        this.notebookUuid = notebookUuid;
    }

    @Override
    public String[] getPath() {
        return new String[] { getStudyId(), getNotebookUuid(), getUuid(), getOriginalFilename() };
    }
}
