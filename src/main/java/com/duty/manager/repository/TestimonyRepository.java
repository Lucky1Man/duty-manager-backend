package com.duty.manager.repository;

import com.duty.manager.entity.Testimony;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TestimonyRepository extends JpaRepository<Testimony, UUID> {
}
