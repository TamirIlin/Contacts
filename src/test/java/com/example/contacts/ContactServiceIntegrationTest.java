package com.example.contacts;

import com.example.contacts.model.Contact;
import com.example.contacts.model.User;
import com.example.contacts.service.ContactService;
import com.example.contacts.service.UserService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ContactServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @Autowired
    private ContactService contactService;

    @Autowired
    private Validator validator;

    @Test
    void registrationStoresHashedPasswordAndAllowsAuthentication() {
        User user = register("alice");

        assertThat(user.getPassword()).isNotEqualTo("secret");
        assertThat(userService.authenticate("alice", "secret")).isPresent();
        assertThat(userService.authenticate("alice", "wrong")).isEmpty();
    }

    @Test
    void duplicateLoginIsRejected() {
        register("duplicate");

        User duplicate = buildUser("duplicate", "another");
        assertThatThrownBy(() -> userService.register(duplicate))
                .isInstanceOf(UserService.DuplicateLoginException.class);
    }

    @Test
    void oversizedPasswordIsRejected() {
        User oversized = buildUser("long_password", "я".repeat(37));

        assertThatThrownBy(() -> userService.register(oversized))
                .isInstanceOf(UserService.PasswordTooLongException.class);
    }

    @Test
    void phoneFormatIsValidated() {
        Contact valid = buildContact("Анна", "+7 (900) 111-22-33", "Друг");
        Contact invalid = buildContact("Анна", "abc", "Друг");

        assertThat(validator.validate(valid)).isEmpty();
        assertThat(validator.validate(invalid))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("phone");
    }

    @Test
    void contactsAreSearchableFilterableAndIsolatedByUser() {
        User alice = register("alice_contacts");
        User bob = register("bob_contacts");

        Contact anna = contactService.createContact(alice.getId(),
                buildContact("Анна Смирнова", "+7 900 111-22-33", "Друг"));
        contactService.createContact(alice.getId(),
                buildContact("Иван Петров", "+7 900 555-66-77", "Коллега"));

        assertThat(contactService.findContacts(alice.getId(), "анна", null))
                .extracting(Contact::getName)
                .containsExactly("Анна Смирнова");
        assertThat(contactService.findContacts(alice.getId(), "", "Коллега"))
                .extracting(Contact::getName)
                .containsExactly("Иван Петров");
        assertThat(contactService.findContacts(bob.getId(), "", null)).isEmpty();
        assertThat(contactService.findContact(anna.getId(), bob.getId())).isEmpty();
    }

    @Test
    void foreignUserCannotModifyOrDeleteContact() {
        User alice = register("alice_owner");
        User bob = register("bob_intruder");
        Contact contact = contactService.createContact(alice.getId(),
                buildContact("Анна", "+7 900 000-00-00", "Родственник"));

        Contact modified = buildContact("Изменено", "000", "Другое");

        assertThatThrownBy(() -> contactService.updateContact(contact.getId(), bob.getId(), modified))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> contactService.deleteContact(contact.getId(), bob.getId()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(contactService.findContact(contact.getId(), alice.getId()))
                .get()
                .extracting(Contact::getName)
                .isEqualTo("Анна");
    }

    private User register(String login) {
        return userService.register(buildUser(login, "secret"));
    }

    private User buildUser(String login, String password) {
        return User.builder()
                .login(login)
                .password(password)
                .build();
    }

    private Contact buildContact(String name, String phone, String type) {
        return Contact.builder()
                .name(name)
                .phone(phone)
                .type(type)
                .build();
    }
}
