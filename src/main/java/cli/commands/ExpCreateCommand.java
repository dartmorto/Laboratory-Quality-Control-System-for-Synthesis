package cli.commands;

import domain.Experiment;
import manager.CollectionManager;
import user.AuthService;

import java.util.Scanner;

/**
 * Команда создания эксперимента.
 * Запрашивает у пользователя данные и добавляет
 * новый эксперимент в коллекцию.
 */

public class ExpCreateCommand extends Command {

    public ExpCreateCommand(CollectionManager manager, Scanner scanner, AuthService authService) {
        super(manager, scanner, authService);
    }

    @Override
    public String name() {
        return "create_experiment";
    }

    @Override
    public void execute(String[] args) {
        requireLogin();
        
        String name;
        while (true) {
            System.out.print("Название: ");
            name = scanner.nextLine();
            cancelIfCancelled(name);
            if (!name.isBlank()) {
                break;
            }
            System.out.println("Ошибка: имя не может быть пустым");
        }

        System.out.print("Описание (можно пусто): ");
        String description = scanner.nextLine();
        cancelIfCancelled(description);

        Experiment exp = manager.createExperiment(name, description, currentUsername());
        long expId = exp.getId();

        System.out.println("Добавлен эксперимент. ID: " + expId + " Название: " + name);


    }


}
