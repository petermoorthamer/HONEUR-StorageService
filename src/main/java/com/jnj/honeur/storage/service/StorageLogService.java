package com.jnj.honeur.storage.service;

import com.jnj.honeur.storage.model.StorageLogEntry;
import com.jnj.honeur.storage.repository.StorageLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for saving and retrieving {@link StorageLogEntry} objects
 *
 * @author Peter Moorthamer
 */

@Service
public class StorageLogService {

    private StorageLogRepository storageLogRepository;

    public StorageLogService(@Autowired  StorageLogRepository storageLogRepository) {
        this.storageLogRepository = storageLogRepository;
    }

    public List<StorageLogEntry> findAll() {
        return storageLogRepository.findAll();
    }

    public List<StorageLogEntry> findByCriteria(final StorageLogEntry probe) {
        return storageLogRepository.findAll(Example.of(probe));
    }

    public void save(final StorageLogEntry storageLogEntry) {
        storageLogRepository.save(storageLogEntry);
    }

}
