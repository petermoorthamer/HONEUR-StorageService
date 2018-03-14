package com.jnj.honeur.storage.model;

import java.util.Date;

/**
 * Represents the meta data of a storage file
 * @author Peter Moorthamer
 */
public class StorageFileInfo {

    private String uuid;
    private String originalFilename;
    private Date lastModified;
    private String key;

    public StorageFileInfo(String uuid, String originalFilename) {
        this.uuid = uuid;
        this.originalFilename = originalFilename;
    }

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }
    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Date getLastModified() {
        return lastModified;
    }
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
}
