package com.example.WiKPIdia.controller;

import com.example.WiKPIdia.entity.User;
import com.example.WiKPIdia.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    // шифрувальник паролів
    private final PasswordEncoder passwordEncoder;
    // змінна-сесія, щоб сайт пам'ятав, хто зараз онлайн
    public static String currentSessionUser = null;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> userData) {
        String username = userData.get("username");
        String password = userData.get("password");
        // перевірка на існування користувача
        if (userRepository.existsByUsername(username)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Цей логін вже зайнятий!"));
        }
        // створюємо нового користувача і записуємо в бд
        User newUser = new User();
        newUser.setUsername(username);
        // хешування паролю перед збереженням у PostgreSQL
        newUser.setPassword(passwordEncoder.encode(password));

        newUser.setEmail(userData.getOrDefault("email", username + "@edu.kpi.ua"));
        newUser.setFullName(userData.getOrDefault("fullName", "Користувач " + username));
        newUser.setRole("Користувач"); // За замовчуванням всі новачки — звичайні користувачі

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        newUser.setCreatedAt(today);

        userRepository.save(newUser);

        currentSessionUser = username; // одразу авторизуємо після реєстрації
        return ResponseEntity.ok(Map.of("message", "Реєстрація успішна!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        Optional<User> userOptional = userRepository.findByUsername(username); // пошук користувача в БД
        // перевірка на існування користувача
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 3. БЕЗПЕЧНЕ ПОРІВНЯННЯ: Використовуємо passwordEncoder.matches()
            // Він бере чистий пароль "12345", хешує його під капотом і порівнює з хешем із БД
            if (passwordEncoder.matches(password, user.getPassword())) {

                // оновлення глобальної сесії за успішного введення паролю
                AuthController.currentSessionUser = user.getUsername();
                return ResponseEntity.ok(Map.of(
                        // повернення успішної відповіді на фронт
                        "token", "real-session-token",
                        "username", user.getUsername(),
                        "role", user.getRole()
                ));
            }
        }
        // якщо логін не знайдено або паролі не збіглися, відкидаємо запит
        return ResponseEntity.status(401).body(Map.of("message", "Неправильний логін або пароль."));
    }
}