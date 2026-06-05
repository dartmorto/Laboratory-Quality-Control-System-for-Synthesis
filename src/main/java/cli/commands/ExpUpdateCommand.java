package cli.commands;

import domain.Experiment;
import manager.CollectionManager;
import user.AuthService;

import java.util.Scanner;

/**
 * Команда обновления данных эксперимента.
 */

public class ExpUpdateCommand extends Command {

    public ExpUpdateCommand(CollectionManager manager, Scanner scanner, AuthService authService) {
        super(manager, scanner, authService);
    }

    @Override
    public String name() {
        return "exp_update";
    }

    @Override
    public void execute(String[] args) {
        requireLogin();

        System.out.print("ID эксперимента: ");
        String idLine = scanner.nextLine();
        cancelIfCancelled(idLine);
        long id = parseId(idLine);

        final Experiment old;
        try {
            old = manager.getById(id);
        } catch (IllegalArgumentException e) {
            System.out.println("Эксперимент не найден");
            return;
        }

        if (!old.getOwnerUsername().equals(currentUsername())) {
            System.out.println("Ошибка: у вас нет прав на изменение этого объекта");
            return;
        }

        System.out.print("Новое название: ");
        String name = scanner.nextLine();
        cancelIfCancelled(name);

        System.out.print("Новое описание: ");
        String description = scanner.nextLine();
        cancelIfCancelled(description);

        Experiment updated = new Experiment(id, name, description, old.getOwnerUsername());

        manager.updateExperiment(id, updated);

        System.out.println("Эксперимент обновлен");
    }
}
