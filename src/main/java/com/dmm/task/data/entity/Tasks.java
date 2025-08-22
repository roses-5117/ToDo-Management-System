package com.dmm.task.data.entity;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Tasks {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
 
    private Long id;
    private String title;
    private String text;
    private String name; 

    @DateTimeFormat(pattern = "yyyy-MM-dd") // この行を追加
    private LocalDate date;
    private boolean done;
}
