package com.duty.manager.repository;

import com.duty.manager.entity.Testimony;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestimonyRepository extends JpaRepository<Testimony, UUID> {

    List<Testimony> findAllByExecutionFactId(UUID id, Pageable pageable);

    Optional<Testimony> findByExecutionFactIdAndWitnessId(UUID executionFactId, UUID witnessId);
}
