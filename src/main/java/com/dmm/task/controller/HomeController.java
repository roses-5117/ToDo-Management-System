package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
// import java.util.HashMap;
import java.util.List;
// import java.util.Map;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.entity.Users;
import com.dmm.task.data.repository.TaskRepository;
import com.dmm.task.data.repository.UsersRepository;

// import com.dmm.task.data.entity.Tasks;
// import com.dmm.task.data.entity.Users;
// import com.dmm.task.data.repository.TaskRepository;
// import com.dmm.task.data.repository.UsersRepository;

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

        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        DayOfWeek startDayOfWeek = firstDayOfMonth.getDayOfWeek();
        int startDayValue = startDayOfWeek.getValue();
        if (startDayValue == 7) {
             startDayValue = 0;
        }
        
        LocalDate startDay = firstDayOfMonth.minusDays(startDayValue);
        // 追加
        LocalDate endDay = startDay.plusDays(42).minusDays(1);

        List<List<LocalDate>> month = new ArrayList<>();
        LocalDate currentDay = startDay;

        for (int i = 0; i < 6; i++) {
            List<LocalDate> week = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                week.add(currentDay);
                currentDay = currentDay.plusDays(1);
            }
            month.add(week);
        }
        
        // 追加
        List<Tasks> taskList;
        if ("admin".equals(role)) {
            taskList = taskRepository.findByDateBetween(startDay, endDay);
        } else {
            taskList = taskRepository.findByDateBetweenAndUser(startDay, endDay, loginUser);
        }

        Map<LocalDate, List<Tasks>> tasksMap = new HashMap<>();
        for (Tasks task : taskList) {
            LocalDate taskDate = task.getDate();
            tasksMap.computeIfAbsent(taskDate, k -> new ArrayList<>()).add(task);
        }

        model.addAttribute("matrix", month);  // カレンダーのデータ
        model.addAttribute("tasks", tasksMap);
        model.addAttribute("date", date);
        model.addAttribute("loginUser", loginUser);

        return "main";
    }
	
	// タスク登録画面の表示用（★追加）
    @GetMapping("/main/create/{date}")
    // 追加
    public String create(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {
    	model.addAttribute("date", date);
    	model.addAttribute("task", new Tasks());
        return "create";
    }
    
    // タスク登録用（★追加）
    // 追加
    @PostMapping("/main/create")
    public String createPost(@AuthenticationPrincipal UserDetails userDetails, Tasks tasks) {
        Users loginUser = usersRepository.findById(userDetails.getUsername()).orElseThrow();
        tasks.setUser(loginUser);
        taskRepository.save(tasks);
        return "redirect:/main?date=" + tasks.getDate();
    }
    
    // タスク編集画面の表示用
    // 追加
    @GetMapping("/main/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Tasks tasks = taskRepository.findById(id).orElseThrow();
        model.addAttribute("task", tasks);
        return "edit";
    }

    // タスク編集用
    @PostMapping("/main/edit")
    public String editPost(@AuthenticationPrincipal UserDetails userDetails, Tasks tasks) {
        Users loginUser = usersRepository.findById(userDetails.getUsername()).orElseThrow();
        tasks.setUser(loginUser);
        taskRepository.save(tasks);
        return "redirect:/main?date=" + tasks.getDate();
    }
}