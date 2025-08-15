package com.dmm.task;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.date BETWEEN :start AND :end")
    List<Task> findByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Task> findByDateBetweenAndUser(LocalDate start, LocalDate end, Users user);
}