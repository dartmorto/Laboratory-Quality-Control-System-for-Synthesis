package user;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Простой репозиторий пользователей.
 * Пользователи сохраняются в файл users.properties.
 */
public class UserRepository {
    private final File file;
    private final Map<String, User> users = new TreeMap<>();

    public UserRepository() {
        this(new File("users.properties"));
    }

    public UserRepository(File file) {
        this.file = file;
        load();
    }

    public boolean existsByLogin(String login) {
        return users.containsKey(normalize(login));
    }

    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(normalize(login)));
    }

    public void save(User user) {
        String login = normalize(user.getLogin());
        if (users.containsKey(login)) {
            throw new IllegalArgumentException("Логин уже занят");
        }

        users.put(login, user);
        saveToFile();
    }

    public void saveOrUpdate(User user) {
        users.put(normalize(user.getLogin()), user);
        saveToFile();
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }

    private void load() {
        if (!file.exists()) {
            return;
        }

        Properties properties = new Properties();
        try (InputStream input = new FileInputStream(file)) {
            properties.load(input);
            for (String login : properties.stringPropertyNames()) {
                String passwordHash = properties.getProperty(login);
                users.put(login, new User(login, passwordHash));
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки пользователей", e);
        }
    }

    private void saveToFile() {
        Properties properties = new Properties();
        for (User user : users.values()) {
            properties.setProperty(user.getLogin(), user.getPasswordHash());
        }

        try (OutputStream output = new FileOutputStream(file)) {
            properties.store(output, "Users");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка сохранения пользователей", e);
        }
    }

    private String normalize(String login) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        return login.trim();
    }
}
