package com.jnj.honeur.storage.comparator;

import com.jnj.honeur.storage.model.StorageFileInfo;
import org.apache.commons.lang3.builder.CompareToBuilder;

import java.util.Comparator;

/**
 * Comparator for sorting storage file info by its last modified date
 * @author Peter Moorthamer
 */
public class StorageFileInfoComparator implements Comparator<StorageFileInfo> {

    @Override
    public int compare(StorageFileInfo i1, StorageFileInfo i2) {
        return new CompareToBuilder()
                .append(i1.getLastModified(), i2.getLastModified())
                .toComparison();
    }

}
