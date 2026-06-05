package user;

import java.security.MessageDigest;

/**
 * Пользователь системы.
 * В БД потом будет храниться login и passwordHash.
 */
public class User {
    private final String login;
    private final String passwordHash;

    public User(String login, String passwordHash) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        this.login = login.trim();
        this.passwordHash = passwordHash;
    }

    public static User create(String login, String password) {
        return new User(login, hashPassword(password));
    }

    public String getLogin() {
        return login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean checkPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    public static String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes("UTF-8"));

            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка хеширования пароля", e);
        }
    }
}
