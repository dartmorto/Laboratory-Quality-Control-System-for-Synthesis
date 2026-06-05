package cli.commands;

import manager.CollectionManager;
import user.AuthService;

import java.util.Locale;
import java.util.Scanner;

public abstract class Command {

    protected final CollectionManager manager;
    protected final Scanner scanner;
    protected final AuthService authService;

    protected Command(CollectionManager manager, Scanner scanner) {
        this(manager, scanner, null);
    }

    protected Command(CollectionManager manager, Scanner scanner, AuthService authService) {
        this.manager = manager;
        this.scanner = scanner;
        this.authService = authService;
    }

    public abstract String name();

    public abstract void execute(String[] args);

    /**
     * Прерывает команду, если пользователь ввёл маркер отмены.
     * Допустимо на любом шаге многошагового ввода: {@code cancel}, {@code отмена}, {@code q}.
     */
    protected void cancelIfCancelled(String line) {
        if (line == null) {
            return;
        }
        String t = line.strip().replace("\uFEFF", "");
        if (t.isEmpty()) {
            return;
        }
        String lower = t.toLowerCase(Locale.ROOT);
        if (lower.equals("cancel") || lower.equals("отмена") || lower.equals("q")) {
            throw new CommandCancelledException();
        }
    }

    protected long parseId(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("ID не указан");
        }
        try {
            long id = Long.parseLong(raw.trim());
            if (id <= 0) {
                throw new IllegalArgumentException("ID должен быть положительным");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный ID: " + raw);
        }
    }

    protected void requireLogin() {
        if (authService == null) {
            throw new IllegalArgumentException("Авторизация не настроена");
        }
        authService.requireLogin();
    }

    protected String currentUsername() {
        requireLogin();
        return authService.getCurrentUsername();
    }
}
