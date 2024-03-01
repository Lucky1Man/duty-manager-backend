package com.duty.manager.repository;

import com.duty.manager.entity.ExecutionFact;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExecutionFactRepository extends JpaRepository<ExecutionFact, UUID> {

    @Query("select f from ExecutionFact f "
            + "where f.startTime >= :from and f.startTime <= :to or f.finishTime >= :from and f.finishTime <= :to")
    List<ExecutionFact> getAllInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                      Pageable pageable);

    @Query("select f from ExecutionFact f "
            + "where (f.startTime >= :from and f.startTime <= :to or"
            + " f.finishTime >= :from and f.finishTime <= :to) and f.executor.id = :participantId")
    List<ExecutionFact> getAllInRangeForParticipant(@Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to,
                                                    @Param("participantId") UUID participantId,
                                                    Pageable pageable);

    @Query("select f from ExecutionFact f where f.finishTime >= :from and f.finishTime <= :to")
    List<ExecutionFact> getAllFinishedInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                              Pageable pageable);

    @Query("select f from ExecutionFact f where f.finishTime >= :from and f.finishTime <= :to and f.executor.id = :participantId")
    List<ExecutionFact> getAllFinishedInRangeForParticipant(@Param("from") LocalDateTime from,
                                                            @Param("to") LocalDateTime to,
                                                            @Param("participantId") UUID participantId,
                                                            Pageable pageable);

    @Query("select f from ExecutionFact f where f.startTime >= :from and f.startTime <= :to and f.finishTime is null")
    List<ExecutionFact> getAllActiveInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to,
                                            Pageable pageable);

    @Query("select f from ExecutionFact f where f.startTime >= :from and f.startTime <= :to and f.executor.id = :participantId and f.finishTime is null")
    List<ExecutionFact> getAllActiveInRangeForParticipant(@Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to,
                                                          @Param("participantId") UUID participantId,
                                                          Pageable pageable);

}
