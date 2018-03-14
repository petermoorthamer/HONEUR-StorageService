package com.jnj.honeur.storage.comparator;

import com.jnj.honeur.storage.model.AbstractStorageFile;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

/**
 * Comparator for sorting storage files by their last modified date
 * @author Peter Moorthamer
 */
public class StorageFileComparator<T extends AbstractStorageFile> implements Comparator<T> {

    @Override
    public int compare(T f1, T f2) {
        return new CompareToBuilder()
                .append(f1.getLastModified(), f2.getLastModified())
                .toComparison();
    }

}
