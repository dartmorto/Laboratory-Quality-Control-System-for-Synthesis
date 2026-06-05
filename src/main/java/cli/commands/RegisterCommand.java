package cli.commands;

import manager.CollectionManager;
import user.AuthService;

import java.util.Scanner;

/**
 * Регистрация нового пользователя.
 */
public class RegisterCommand extends Command {

    public RegisterCommand(CollectionManager manager, Scanner scanner, AuthService authService) {
        super(manager, scanner, authService);
    }

    @Override
    public String name() {
        return "register";
    }

    @Override
    public void execute(String[] args) {
        System.out.print("Логин: ");
        String login = scanner.nextLine();
        cancelIfCancelled(login);

        System.out.print("Пароль: ");
        String password = scanner.nextLine();
        cancelIfCancelled(password);

        authService.register(login, password);
        System.out.println("Пользователь зарегистрирован");
    }
}
