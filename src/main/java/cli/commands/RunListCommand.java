package cli.commands;

import domain.Run;
import manager.CollectionManager;

import java.util.Scanner;

/**
 * Команда вывода списка всех запусков.
 */

public class RunListCommand extends Command {

    public RunListCommand(CollectionManager manager, Scanner scanner) {
        super(manager, scanner);
    }

    @Override
    public String name() {
        return "run_list";
    }

    @Override
    public void execute(String[] args) {

        if (manager.getAllRuns().isEmpty()) {
            System.out.println("Пробегов нет.");
            return;
        }

        System.out.println("ID  Experiment  Name  Operator");

        for (Run r : manager.getAllRuns().values()) {
            System.out.println(r.getId() + "  " + r.getExperimentId() + "  " + r.getName() + "  " + r.getOperatorUsername());
        }
    }
}
