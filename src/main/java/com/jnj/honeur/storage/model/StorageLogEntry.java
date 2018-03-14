package com.jnj.honeur.storage.model;



import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
@Table(name="STORAGE_LOG")
public class StorageLogEntry {

    public enum Action {
        UPLOAD, DOWNLOAD, DELETE
    }

    @Id
    @GeneratedValue
    @Column(name = "storage_id")
    private Long id;
    @Column(name = "storage_user")
    private String user;
    @Enumerated(EnumType.STRING)
    @Column(name = "storage_action", nullable = false)
    private Action action;
    @Column(name = "storage_file_class")
    private String storageFileClass;
    @Column(name = "storage_file_name")
    private String storageFileName;
    @Column(name = "storage_file_uuid")
    private String storageFileUuid;
    @Column(name = "storage_file_key")
    private String storageFileKey;
    @Column(name = "storage_date_time", nullable = false)
    private ZonedDateTime dateTime;

    public StorageLogEntry() {}

    public StorageLogEntry(String user, Action action, AbstractStorageFile storageFile) {
        this.user = user;
        this.action = action;
        this.storageFileClass = storageFile.getClass().getSimpleName();
        this.storageFileName = storageFile.getOriginalFilename();
        this.storageFileUuid = storageFile.getUuid();
        this.storageFileKey = storageFile.getKey();
        this.dateTime = ZonedDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public Action getAction() {
        return action;
    }
    public void setAction(Action action) {
        this.action = action;
    }

    public String getStorageFileClass() {
        return storageFileClass;
    }
    public void setStorageFileClass(String storageFileClass) {
        this.storageFileClass = storageFileClass;
    }

    public String getStorageFileName() {
        return storageFileName;
    }
    public void setStorageFileName(String storageFileName) {
        this.storageFileName = storageFileName;
    }

    public String getStorageFileUuid() {
        return storageFileUuid;
    }
    public void setStorageFileUuid(String storageFileUuid) {
        this.storageFileUuid = storageFileUuid;
    }

    public String getStorageFileKey() {
        return storageFileKey;
    }
    public void setStorageFileKey(String storageFileKey) {
        this.storageFileKey = storageFileKey;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }
    public void setDateTime(ZonedDateTime dateTime) {
        this.dateTime = dateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if(id == null) {
            return super.equals(o);
        }
        StorageLogEntry that = (StorageLogEntry) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        if(id == null) {
            return super.hashCode();
        }
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StorageLogEntry{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", action=" + action +
                ", storageFileClass='" + storageFileClass + '\'' +
                ", storageFileName='" + storageFileName + '\'' +
                ", storageFileUuid='" + storageFileUuid + '\'' +
                ", storageFileKey='" + storageFileKey + '\'' +
                ", dateTime=" + dateTime +
                '}';
    }
}
