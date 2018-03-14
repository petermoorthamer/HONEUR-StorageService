package com.jnj.honeur.storage.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.jnj.honeur.aws.s3.AmazonS3Service;
import com.jnj.honeur.storage.comparator.StorageFileComparator;
import com.jnj.honeur.storage.comparator.StorageFileInfoComparator;
import com.jnj.honeur.storage.exception.StorageException;
import com.jnj.honeur.storage.model.AbstractStorageFile;
import com.jnj.honeur.storage.model.StorageFileBuilder;
import com.jnj.honeur.storage.model.StorageFileInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Amazon S3 implementation of the HONEUR {@link StorageService}
 *
 * @author Peter Moorthamer
 */

@Service
public class AmazonS3StorageService extends StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmazonS3StorageService.class);

    @Value("${amazon.s3.bucketName}")
    private String bucketName;

    private AmazonS3Service amazonS3Service;
    private AmazonS3StorageKeyBuilder storageKeyBuilder;

    public AmazonS3StorageService(@Autowired AmazonS3Service amazonS3Service, @Autowired AmazonS3StorageKeyBuilder storageKeyBuilder) {
        this.amazonS3Service = amazonS3Service;
        this.storageKeyBuilder = storageKeyBuilder;
    }

    @PostConstruct
    void init() {
        createBucketIfMissing();
    }

    @Override
    public void saveStorageFile(AbstractStorageFile storageFile) throws StorageException {
        try {
            amazonS3Service.uploadFile(
                    bucketName,
                    storageKeyBuilder.buildKey(storageFile),
                    storageFile.getFile());
        } catch (AmazonServiceException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteStorageFile(String fileKey) throws StorageException {
        try {
            amazonS3Service.deleteObject(bucketName, fileKey);
        } catch (AmazonServiceException e) {
            LOGGER.error(e.getMessage(), e);
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public <T extends AbstractStorageFile> T getStorageFile(Class<T> clazz, String... prefixes) throws StorageException {
        final List<T> storageFiles = downloadFiles(clazz, storageKeyBuilder.buildKey(clazz, prefixes));
        if(storageFiles.isEmpty()) {
            return null;
        }
        if(storageFiles.size() > 1) {
            throw new StorageException("Multiple results found with the same UUID!");
        }
        return storageFiles.get(0);
    }

    @Override
    public <T extends AbstractStorageFile> T getStorageFileWithKey(String fileKey) throws StorageException {
        return downloadFileWithKey(fileKey);
    }

    @Override
    public <T extends AbstractStorageFile> T getStorageFileWithUuid(String uuid) throws StorageException {
        return downloadFileWithUuid(uuid);
    }

    @Override
    public List<StorageFileInfo> getAllStorageFileKeys() {
        return getKeys(null);
    }

    @Override
    public List<StorageFileInfo> getMatchingStorageFileKeys(Class<? extends AbstractStorageFile> clazz, String... prefixes) {
        return getKeys(storageKeyBuilder.buildKey(clazz, prefixes));
    }

    @Override
    public <T extends AbstractStorageFile> Collection<T> getAllStorageFiles(Class<T> clazz, String... prefixes) throws StorageException {
        return downloadFiles(clazz, storageKeyBuilder.buildKey(clazz, prefixes));
    }

    private List<StorageFileInfo> getKeys(String prefix) {
        ListObjectsV2Result result;
        if(StringUtils.isNotBlank(prefix)) {
            result = amazonS3Service.getObjects(bucketName, prefix);
        } else {
            result = amazonS3Service.getObjects(bucketName);
        }
        final List<S3ObjectSummary> objects = result.getObjectSummaries();
        final List<StorageFileInfo> keys = new ArrayList<>();
        for (S3ObjectSummary os: objects) {
            final StorageFileInfo fileInfo = new StorageFileInfo(
                    storageKeyBuilder.getUuid(os.getKey()),
                    storageKeyBuilder.getFilename(os.getKey()));
            fileInfo.setLastModified(os.getLastModified());
            fileInfo.setKey(os.getKey());
            keys.add(fileInfo);
        }
        keys.sort(new StorageFileInfoComparator());
        return keys;
    }

    private <T extends AbstractStorageFile> List<T> downloadFiles(Class<T> clazz, String prefix) {
        final ListObjectsV2Result result = amazonS3Service.getObjects(bucketName, prefix);
        final List<S3ObjectSummary> objects = result.getObjectSummaries();
        final List<T> storageFiles = new ArrayList<>();
        try {
            for (S3ObjectSummary os: objects) {
                final File downloadFile = createTempFile(os.getKey());
                amazonS3Service.downloadFile(bucketName, os.getKey(), downloadFile);

                T storageFile = StorageFileBuilder.buildStorageFile(clazz, downloadFile, storageKeyBuilder.getKeyPartsWithoutRootFolder(os.getKey()));
                storageFile.getFileInfo().setLastModified(os.getLastModified());
                storageFiles.add(storageFile);
            }
        } catch (IOException | InterruptedException e) {
            throw new StorageException(e.getMessage(), e);
        }
        storageFiles.sort(new StorageFileComparator<>());
        return storageFiles;
    }

    private <T extends AbstractStorageFile> T downloadFileWithUuid(String uuid) throws StorageException {
        final ListObjectsV2Result result = amazonS3Service.getObjects(bucketName);
        final List<S3ObjectSummary> objects = result.getObjectSummaries();

        for (S3ObjectSummary os: objects) {
            if(os.getKey().contains(uuid)) {
                final T storageFile = downloadFileWithKey(os.getKey());
                storageFile.getFileInfo().setLastModified(os.getLastModified());
                return storageFile;
            }
        }
        return null;
    }

    private <T extends AbstractStorageFile> T downloadFileWithKey(String fileKey) throws StorageException {
        try {
            final File downloadFile = createTempFile(storageKeyBuilder.getFilename(fileKey));
            amazonS3Service.downloadFile(bucketName, fileKey, downloadFile);
            return StorageFileBuilder.buildStorageFile(
                    (Class<T>) storageKeyBuilder.getStorageFileClass(fileKey),
                    downloadFile,
                    storageKeyBuilder.getKeyPartsWithoutRootFolder(fileKey));
        } catch (IOException | InterruptedException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    private void createBucketIfMissing() {
        if(!amazonS3Service.doesBucketExist(bucketName)) {
            amazonS3Service.createBucket(bucketName);
        }
    }
}
