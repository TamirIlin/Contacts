package com.example.contacts.controller;

import com.example.contacts.model.User;
import com.example.contacts.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class UserController {
    private static final String USER_ID = "userId";
    private static final String USER_LOGIN = "userLogin";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/contacts";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        if (session.getAttribute(USER_ID) != null) {
            return "redirect:/contacts";
        }
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") User user,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.register(user);
        } catch (UserService.DuplicateLoginException exception) {
            bindingResult.rejectValue("login", "login.duplicate", exception.getMessage());
            return "register";
        } catch (UserService.PasswordTooLongException exception) {
            bindingResult.rejectValue("password", "password.tooLong", exception.getMessage());
            return "register";
        }

        redirectAttributes.addFlashAttribute("registeredLogin", user.getLogin().trim());
        return "redirect:/login?registered";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session, Model model) {
        if (session.getAttribute(USER_ID) != null) {
            return "redirect:/contacts";
        }
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("user") User user,
                        BindingResult bindingResult,
                        HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "login";
        }

        Optional<User> authenticated = userService.authenticate(user.getLogin(), user.getPassword());
        if (authenticated.isEmpty()) {
            bindingResult.reject("login.invalid", "Неверный логин или пароль");
            return "login";
        }

        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(USER_ID, authenticated.get().getId());
        session.setAttribute(USER_LOGIN, authenticated.get().getLogin());

        return "redirect:/contacts";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
