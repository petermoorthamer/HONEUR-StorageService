package com.jnj.honeur.storage.service;

import com.jnj.honeur.storage.exception.StorageException;
import com.jnj.honeur.storage.model.AbstractStorageFile;
import com.jnj.honeur.storage.model.StorageFileInfo;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Backend service used by the HONEUR Storage Service API ({@link com.jnj.honeur.storage.controller.StorageController})
 * @author Peter Moorthamer
 */
public abstract class StorageService {

    public abstract void saveStorageFile(AbstractStorageFile storageFile) throws StorageException;

    public abstract void deleteStorageFile(String fileKey) throws StorageException;

    public abstract <T extends AbstractStorageFile> T getStorageFileWithKey(String fileKey) throws StorageException;

    public abstract <T extends AbstractStorageFile> T getStorageFileWithUuid(String uuid) throws StorageException;

    public abstract <T extends AbstractStorageFile> T getStorageFile(Class<T> clazz, String... prefixes) throws StorageException;

    public abstract List<StorageFileInfo> getAllStorageFileKeys();

    public abstract List<StorageFileInfo> getMatchingStorageFileKeys(Class<? extends AbstractStorageFile> clazz, String... prefixes);

    public abstract <T extends AbstractStorageFile> Collection<T> getAllStorageFiles(Class<T> clazz, String... prefixes) throws StorageException;

    public static String buildFileName(String filename, String uuid) {
        String prefix = com.google.common.io.Files.getNameWithoutExtension(filename);
        String suffix = com.google.common.io.Files.getFileExtension(filename);
        return prefix + "_" + uuid + "." + suffix;
    }

    public static File createTempFile(String filename) throws IOException {
        String prefix = com.google.common.io.Files.getNameWithoutExtension(filename);
        prefix = StringUtils.rightPad(prefix, 3, '_');
        String suffix = "." + com.google.common.io.Files.getFileExtension(filename);
        return File.createTempFile(prefix, suffix);
    }

}
