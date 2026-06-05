package cli.commands;

import manager.CollectionManager;
import user.AuthService;

import java.util.Scanner;

/**
 * Вход пользователя в систему.
 */
public class LoginCommand extends Command {

    public LoginCommand(CollectionManager manager, Scanner scanner, AuthService authService) {
        super(manager, scanner, authService);
    }

    @Override
    public String name() {
        return "login";
    }

    @Override
    public void execute(String[] args) {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        cancelIfCancelled(login);

        System.out.print("Пароль: ");
        String password = scanner.nextLine();
        cancelIfCancelled(password);

        authService.login(login, password);
        System.out.println("Вход выполнен. Текущий пользователь: " + authService.getCurrentUsername());
    }
}
