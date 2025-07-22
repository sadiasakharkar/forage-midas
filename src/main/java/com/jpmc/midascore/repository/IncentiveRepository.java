package com.jpmc.midascore.repository;

import com.jpmc.midascore.entity.IncentiveRecord;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IncentiveRepository extends CrudRepository<IncentiveRecord, Long> {
    List<IncentiveRecord> findByUserId(Long userId);
}
