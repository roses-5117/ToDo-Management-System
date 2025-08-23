package com.dmm.task.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dmm.task.data.entity.Tasks;
import com.dmm.task.data.entity.Users;
import com.dmm.task.data.repository.TaskRepository;
import com.dmm.task.data.repository.UsersRepository;
import com.dmm.task.form.TaskForm;

//import net.bytebuddy.dynamic.DynamicType.Builder.FieldDefinition.Optional;

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
        
//        LocalDate endDay = startDay.plusDays(42).minusDays(1);
//        List<List<LocalDate>> month = new ArrayList<>();
//        LocalDate currentDay = startDay;
//        for (int i = 0; i < 6; i++) {
//            List<LocalDate> week = new ArrayList<>();
//            for (int j = 0; j < 7; j++) {
//                week.add(currentDay);
//                currentDay = currentDay.plusDays(1);
//            }
//            month.add(week);
//        }
        
     // カレンダーの週数を動的に計算
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weeksInMonth = (int) firstDayOfMonth.range(weekFields.weekOfMonth()).getMaximum();
        LocalDate endDay = startDay.plusDays(weeksInMonth * 7).minusDays(1);

        List<List<LocalDate>> month = new ArrayList<>();
        LocalDate currentDay = startDay;

        for (int i = 0; i < weeksInMonth; i++) {
            List<LocalDate> week = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                week.add(currentDay);
                currentDay = currentDay.plusDays(1);
            }
            month.add(week);
        }


        List<Tasks> taskList;
        if ("admin".equalsIgnoreCase(role)) {
            taskList = taskRepository.findByDateBetween(startDay, endDay);
        } else {
            taskList = taskRepository.findByDateBetweenAndName(startDay, endDay, loginUser.getName());
        }

        Map<LocalDate, List<Tasks>> tasksMap = new HashMap<>();
        for (Tasks task : taskList) {
            LocalDate taskDate = task.getDate();
            tasksMap.computeIfAbsent(taskDate, k -> new ArrayList<>()).add(task);
        }
        
        model.addAttribute("tasks", tasksMap);
        model.addAttribute("date", date);
        model.addAttribute("loginUser", loginUser);
        model.addAttribute("matrix", month);  // カレンダーのデータ
        
        // 当月表示
        model.addAttribute("month", date.getYear() + "年" + date.getMonthValue() + "月");
        
     // 前月と翌月の日付を計算
        LocalDate prevMonthDate = date.minusMonths(1);
        LocalDate nextMonthDate = date.plusMonths(1);
        
        // Modelに前月と翌月の日付を追加
        model.addAttribute("prev", prevMonthDate);
        model.addAttribute("next", nextMonthDate);
        // model.addAttribute("month", date.getYear() + "年" + date.getMonthValue() + "月");
     // タスク登録時のリダイレクト先として使用するため、現在の表示日付をモデルに追加
        model.addAttribute("currentDate", date);
        

        return "main";
    }
	
	// タスク登録画面の表示用
	@GetMapping("/main/create/{date}")
		public String create(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
			    Model model, HttpSession session){
	    TaskForm taskForm = new TaskForm();
	    taskForm.setDate(date);
	    model.addAttribute("taskForm", taskForm);
	    // 登録後のリダイレクト先として、元の月を保持
	    if (returnDate == null) {
	        returnDate = date.withDayOfMonth(1);
	    }
	    model.addAttribute("returnDate", returnDate);
	    session.setAttribute("returnDate", returnDate); // ★セッションにreturnDateを保存
	    return "create";
	}

	// ...
    
	// タスク登録用（★追加）
    @PostMapping("/main/create")
    public String createPost(@AuthenticationPrincipal UserDetails userDetails, TaskForm taskForm, HttpSession session) {

        // ユーザー名でUsersオブジェクトを検索して取得
    	Users loginUser = usersRepository.findByUserName(userDetails.getUsername());
        
        
        // TaskFormからTasksエンティティにデータをコピー
        Tasks task = new Tasks();
        task.setTitle(taskForm.getTitle());
        task.setText(taskForm.getText());
        task.setDate(taskForm.getDate());
        task.setDone(taskForm.isDone());
        
        
     // ログインユーザーの名前をタスクに設定
       task.setName(loginUser.getName());
        
        taskRepository.save(task);
        
     // セッションから元の表示月を取得
        LocalDate returnDate = (LocalDate) session.getAttribute("returnDate");
        session.removeAttribute("returnDate"); // ★使用後はセッションから削除
        
     // セッションに値がない場合（想定外のアクセスなど）に備え、デフォルトの挙動を指定
        if (returnDate == null) {
        	return "redirect:/main?date=" + taskForm.getDate();
        }
        
     // リダイレクト先を、セッションに保存した元の月に設定
        return "redirect:/main?date=" + returnDate;
    }
    
 // 編集画面の表示用（★追加）
    @GetMapping("/main/edit/{id}")
    public String edit(@PathVariable Long id, 
    	    @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate,
    	    Model model, HttpSession session) {
		Optional<Tasks> task = taskRepository.findById(id);

        // タスクが見つかった場合、モデルに追加してeditビューを返す
        if (task.isPresent()) {
            model.addAttribute("task", task.get());
            model.addAttribute("returnDate", returnDate); // ★追加
            session.setAttribute("returnDate", returnDate);
            return "edit";
        }

        // タスクが見つからない場合は、エラーページまたはメイン画面にリダイレクト
        return "redirect:/main";
    }
    
    @PostMapping("/main/edit/{id}")
    public String update(@PathVariable Long id, @ModelAttribute TaskForm taskForm, HttpSession session) { // 変更点：引数を変更
        Optional<Tasks> task = taskRepository.findById(id);

        if (task.isPresent()) {
            Tasks existingTask = task.get();
            existingTask.setTitle(taskForm.getTitle());
            existingTask.setText(taskForm.getText());
            existingTask.setDate(taskForm.getDate());
            existingTask.setDone(taskForm.isDone());
            
            taskRepository.save(existingTask);
            
            LocalDate returnDate = (LocalDate) session.getAttribute("returnDate"); // 変更点：この行を追加
            session.removeAttribute("returnDate"); // 変更点：この行を追加

            if (returnDate == null) { // 変更点：この行を追加
                return "redirect:/main?date=" + existingTask.getDate(); // 変更点：この行を追加
            }
            
            return "redirect:/main?date=" + returnDate; // 変更点：この行を変更
        }
        
        return "redirect:/main";
    }
    
    // タスクの削除
    @PostMapping("/main/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) { // 変更点：引数を変更
        taskRepository.deleteById(id);
        
        LocalDate returnDate = (LocalDate) session.getAttribute("returnDate"); // 変更点：この行を追加
        session.removeAttribute("returnDate"); // 変更点：この行を追加

        if (returnDate == null) { // 変更点：この行を追加
            return "redirect:/main"; // 変更点：この行を追加
        }
        
        return "redirect:/main?date=" + returnDate; // 変更点：この行を変更
    }
}