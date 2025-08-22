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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.entity.Users;
import com.dmm.task.data.repository.TaskRepository;
import com.dmm.task.data.repository.UsersRepository;
import com.dmm.task.form.TaskForm;

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

         Users loginUser = usersRepository.findByUserName(userDetails.getUsername());
         String role = loginUser.getRoleName();

        LocalDate firstDayOfMonth = date.withDayOfMonth(1);
        DayOfWeek startDayOfWeek = firstDayOfMonth.getDayOfWeek();
        int startDayValue = startDayOfWeek.getValue();
        if (startDayValue == 7) {
             startDayValue = 0;
        }
        
        LocalDate startDay = firstDayOfMonth.minusDays(startDayValue);
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
	

//        model.addAttribute("date", date);
//        model.addAttribute("month", date.getYear() + "年" + date.getMonthValue() + "月");
//        model.addAttribute("prev", prevMonth);
//        model.addAttribute("next", nextMonth);
//        model.addAttribute("matrix", calendarMatrix);
        // model.addAttribute("tasks", tasksMap);
        model.addAttribute("tasks", tasksMap);
        model.addAttribute("date", date);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("matrix", month);  // カレンダーのデータ
        MultiValueMap<LocalDate, Tasks> tasks = new LinkedMultiValueMap<>();
        model.addAttribute("tasks", tasks);  // タスクのデータ

        return "main";
    }
	
	// タスク登録画面の表示用（★追加）
    @GetMapping("/main/create/{date}")
    public String create() {
        // ここにタスク登録画面を表示するためのロジックを記述します
        return "create";
    }
    
	// タスク登録用（★追加）
    @PostMapping("/main/create")
    public String createPost(@AuthenticationPrincipal UserDetails userDetails, TaskForm taskForm) {
        // ユーザー名でUsersオブジェクトを検索して取得
        Users loginUser = usersRepository.findByUserName(userDetails.getUsername());
        
        // TaskFormからTasksエンティティにデータをコピー
        Tasks task = new Tasks();
        task.setTitle(taskForm.getTitle());
        task.setText(taskForm.getText());
        task.setDate(taskForm.getDate());
        task.setDone(taskForm.isDone());
        
        // ここで、正しいUsersオブジェクトがタスクに紐づけられる
        task.setUser(loginUser);
        
        taskRepository.save(task);
        
        return "redirect:/main?date=" + task.getDate();
    }
}