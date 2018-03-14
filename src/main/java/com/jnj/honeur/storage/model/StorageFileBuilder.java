package com.jnj.honeur.storage.model;

import java.io.File;

/**
 * Builds the correct implementation of {@link AbstractStorageFile} based on the given input
 * @author Peter Moorthamer
 */
public class StorageFileBuilder {

    public static <T extends AbstractStorageFile> T buildStorageFile(Class<T> clazz, File file, String... path) {
        if(CohortDefinitionFile.class.equals(clazz)) {
            return (T) new CohortDefinitionFile(new StorageFileInfo(path[0], path[1]), file);
        } else if(CohortInclusionResultsFile.class.equals(clazz)) {
            return (T) new CohortInclusionResultsFile(path[0], new StorageFileInfo(path[1], path[2]), file);
        } else if(NotebookFile.class.equals(clazz)) {
            return (T) new NotebookFile(path[0], new StorageFileInfo(path[1], path[2]), file);
        } else if(NotebookResultsFile.class.equals(clazz)) {
            return (T) new NotebookResultsFile(path[0], path[1], new StorageFileInfo(path[2], path[3]), file);
        } else {
            throw new IllegalArgumentException(String.format("Unknown class %s", clazz.getName()));
        }
    }

}
