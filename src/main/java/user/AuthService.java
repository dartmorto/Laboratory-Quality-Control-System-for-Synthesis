package user;

/**
 * Хранит текущего пользователя CLI-сессии.
 */
public class AuthService {
    private final UserRepository userRepository;
    private User currentUser;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void register(String login, String password) {
        if (userRepository.existsByLogin(login)) {
            throw new IllegalArgumentException("Логин уже занят");
        }

        userRepository.save(User.create(login, password));
    }

    public void login(String login, String password) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Неверный логин или пароль"));

        if (!user.checkPassword(password)) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }

        currentUser = user;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasUsers() {
        return !userRepository.isEmpty();
    }

    public String getCurrentUsername() {
        requireLogin();
        return currentUser.getLogin();
    }

    public void logout() {
        currentUser = null;
    }

    public void requireLogin() {
        if (!isLoggedIn()) {
            throw new IllegalArgumentException("Сначала выполните login");
        }
    }
}
