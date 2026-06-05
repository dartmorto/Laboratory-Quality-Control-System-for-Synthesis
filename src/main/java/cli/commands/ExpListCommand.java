package cli.commands;

import domain.Experiment;
import manager.CollectionManager;

import java.util.Scanner;

/**
 * Команда вывода списка всех экспериментов.
 */

public class ExpListCommand extends Command {

    public ExpListCommand(CollectionManager manager, Scanner scanner) {
        super(manager, scanner);
    }

    @Override
    public String name() {
        return "exp_list";
    }

    @Override
    public void execute(String[] args) {

        if (manager.getAllExperiments().isEmpty()) {
            System.out.println("Экспериментов нет.");
            return;
        }

        System.out.println("ID  Name  Owner");

        for (Experiment e : manager.getAllExperiments().values()) {
            System.out.println(e.getId() + "  " + e.getName() + "  " + e.getOwnerUsername());
        }


    }
}
