package com.dmm.task;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/create/{date}")
    public String createForm(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Model model) {
        model.addAttribute("date", date);
        return "create";
    }

    @PostMapping("/create")
    public String createTask(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String title,
                             @RequestParam String text,
                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Users loginUser = usersRepository.findById(userDetails.getUsername()).orElseThrow();

        Task task = new Task();
        task.setTitle(title);
        task.setText(text);
        task.setDate(date);
        task.setUser(loginUser);
        task.setDone(false);
        taskRepository.save(task);

        return "redirect:/main";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Task task = taskRepository.findById(id).orElseThrow();
        model.addAttribute("task", task);
        return "edit";
    }

    @PostMapping("/edit/{id}")
    public String editTask(@PathVariable Long id,
                           @RequestParam String title,
                           @RequestParam String text,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           @RequestParam(required = false) boolean done) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setTitle(title);
        task.setText(text);
        task.setDate(date);
        task.setDone(done);
        taskRepository.save(task);
        return "redirect:/main";
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return "redirect:/main";
    }
}