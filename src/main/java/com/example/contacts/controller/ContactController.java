package com.example.contacts.controller;

import com.example.contacts.model.Contact;
import com.example.contacts.service.ContactService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/contacts")
public class ContactController {
    private static final String USER_ID = "userId";

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @GetMapping
    public String list(@RequestParam(required = false, defaultValue = "") String search,
                       @RequestParam(required = false) String type,
                       HttpSession session,
                       Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        String selectedType = normalizeType(type);

        model.addAttribute("contacts", contactService.findContacts(
                currentUserId(session), search, selectedType));
        model.addAttribute("search", search == null ? "" : search.trim());
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("contactTypes", Contact.TYPES);

        return "contacts/list";
    }

    @GetMapping("/new")
    public String createPage(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        addFormAttributes(model, new Contact(), null);
        return "contacts/create";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("contact") Contact contact,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, contact, null);
            return "contacts/create";
        }

        contactService.createContact(currentUserId(session), contact);
        redirectAttributes.addFlashAttribute("success", "Контакт добавлен");
        return "redirect:/contacts";
    }

    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        Optional<Contact> contact = contactService.findContact(id, currentUserId(session));
        if (contact.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Контакт не найден");
            return "redirect:/contacts";
        }

        addFormAttributes(model, contact.get(), id);
        return "contacts/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("contact") Contact contact,
                         BindingResult bindingResult,
                         HttpSession session,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            addFormAttributes(model, contact, id);
            return "contacts/edit";
        }

        try {
            contactService.updateContact(id, currentUserId(session), contact);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/contacts";
        }

        redirectAttributes.addFlashAttribute("success", "Контакт обновлён");
        return "redirect:/contacts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        try {
            contactService.deleteContact(id, currentUserId(session));
            redirectAttributes.addFlashAttribute("success", "Контакт удалён");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }

        return "redirect:/contacts";
    }

    private String normalizeType(String type) {
        if (type == null) {
            return null;
        }

        String normalized = type.trim();
        return Contact.TYPES.contains(normalized) ? normalized : null;
    }

    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute(USER_ID) != null;
    }

    private Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute(USER_ID);
    }

    private void addFormAttributes(Model model, Contact contact, Long contactId) {
        model.addAttribute("contact", contact);
        model.addAttribute("contactTypes", Contact.TYPES);
        if (contactId != null) {
            model.addAttribute("contactId", contactId);
        }
    }
}
