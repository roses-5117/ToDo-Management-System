package com.dmm.task.data.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.entity.Users;

public interface TaskRepository extends JpaRepository<Tasks, Long> {

    @Query("SELECT t FROM Task t WHERE t.date BETWEEN :start AND :end")
    List<Tasks> findByDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Tasks> findByDateBetweenAndUser(LocalDate start, LocalDate end, Users user);
}