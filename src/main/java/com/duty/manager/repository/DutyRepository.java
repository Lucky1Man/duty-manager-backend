package com.duty.manager.repository;

import com.duty.manager.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DutyRepository extends JpaRepository<Duty, UUID> {
}
