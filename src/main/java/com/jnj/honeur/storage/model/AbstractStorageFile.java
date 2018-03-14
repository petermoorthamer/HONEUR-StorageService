package com.jnj.honeur.storage.model;

import java.io.File;
import java.util.Date;

/**
 * Represents a file stored or to be stored by the {@link com.jnj.honeur.storage.service.StorageService}
 * @author Peter Moorthamer
 */

public abstract class AbstractStorageFile {

    private StorageFileInfo fileInfo;
    private File file;

    public StorageFileInfo getFileInfo() {
        return fileInfo;
    }
    public void setFileInfo(StorageFileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public String getUuid() {
        return fileInfo.getUuid();
    }

    public String getOriginalFilename() {
        return fileInfo.getOriginalFilename();
    }

    public Date getLastModified() {
        return fileInfo.getLastModified();
    }

    public File getFile() {
        return file;
    }
    public void setFile(File file) {
        this.file = file;
    }

    public String[] getPath() {
        return new String[] { getUuid(), getOriginalFilename() };
    }

}
