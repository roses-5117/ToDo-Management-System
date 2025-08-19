package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Users;
import com.dmm.task.data.repository.TaskRepository;
import com.dmm.task.data.repository.UsersRepository;

@Controller
public class HomeController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/main")
    public String main(@AuthenticationPrincipal UserDetails userDetails,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       Model model) {
        // カレンダーを表示するためのコードをここに記述
        if (date == null) {
            date = LocalDate.now();
        }

        Users loginUser = usersRepository.findById(userDetails.getUsername()).orElseThrow();

        String role = loginUser.getRoleName();

        LocalDate prevMonth = date.minusMonths(1);
        LocalDate nextMonth = date.plusMonths(1);

        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        DayOfWeek startDayOfWeek = firstDayOfMonth.getDayOfWeek();
        int startDayValue = startDayOfWeek.getValue();
        if (startDayValue == 7) {
             startDayValue = 0;
        }

        LocalDate startDay = firstDayOfMonth.minusDays(startDayValue);

        List<List<LocalDate>> calendarMatrix = new ArrayList<>();
        LocalDate currentDay = startDay;

        for (int i = 0; i < 6; i++) {
            List<LocalDate> week = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                week.add(currentDay);
                currentDay = currentDay.plusDays(1);
            }
            calendarMatrix.add(week);
        }

        LocalDate endDay = currentDay.minusDays(1);
        List<com.dmm.task.data.entity.Task> taskList;

        if ("admin".equals(role)) {
            taskList = taskRepository.findByDateBetween(startDay, endDay);
        } else {
            taskList = taskRepository.findByDateBetweenAndUser(startDay, endDay, loginUser);
        }

        Map<LocalDate, List<com.dmm.task.data.entity.Task>> tasksMap = new HashMap<>();
        for (com.dmm.task.data.entity.Task task : taskList) {
            LocalDate taskDate = task.getDate();
            tasksMap.computeIfAbsent(taskDate, k -> new ArrayList<>()).add(task);
        }


        model.addAttribute("date", date);
        model.addAttribute("month", date.getYear() + "年" + date.getMonthValue() + "月");
        model.addAttribute("prev", prevMonth);
        model.addAttribute("next", nextMonth);
        model.addAttribute("matrix", calendarMatrix);
        model.addAttribute("tasks", tasksMap);

        return "main";
    }
}