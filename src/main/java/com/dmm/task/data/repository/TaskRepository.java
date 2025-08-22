package com.dmm.task.data.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dmm.task.data.entity.Tasks;

public interface TaskRepository extends JpaRepository<Tasks, Long> {
	List<Tasks> findByDateBetween(LocalDate start, LocalDate end);
    // nameカラムで検索するメソッドに変更
    List<Tasks> findByDateBetweenAndName(LocalDate start, LocalDate end, String name);
}