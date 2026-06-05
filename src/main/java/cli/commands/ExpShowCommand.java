package cli.commands;

import domain.Experiment;
import manager.CollectionManager;

import java.util.Scanner;

/**
 * Команда отображения информации об эксперименте по ID.
 */

public class ExpShowCommand extends Command {

    public ExpShowCommand(CollectionManager manager, Scanner scanner) {
        super(manager, scanner);
    }

    @Override
    public String name() {
        return "exp_show";
    }

    @Override
    public void execute(String[] args) {
        System.out.print("ID эксперимента: ");
        String idLine = scanner.nextLine();
        cancelIfCancelled(idLine);
        long id = parseId(idLine);

        try {
            Experiment exp = manager.getById(id);
            System.out.println("ID: " + exp.getId());
            System.out.println("Название: " + exp.getName());
            System.out.println("Владелец: " + exp.getOwnerUsername());
        } catch (IllegalArgumentException e) {
            System.out.println("Эксперимент не найден");
        }
    }
}
