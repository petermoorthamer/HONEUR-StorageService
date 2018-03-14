package com.jnj.honeur.storage.repository;

import com.jnj.honeur.storage.model.StorageLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA CRUD repository for {@link StorageLogEntry}
 */

@Repository
public interface StorageLogRepository extends JpaRepository<StorageLogEntry, Long>  {
}
