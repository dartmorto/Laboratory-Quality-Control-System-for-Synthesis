package cli.commands;

import domain.Result;
import manager.CollectionManager;

import java.util.Scanner;

/**
 * Команда вывода списка всех результатов измерений.
 */
public class ResultListCommand extends Command {

    public ResultListCommand(CollectionManager manager, Scanner scanner) {
        super(manager, scanner);
    }

    @Override
    public String name() {
        return "res_list";
    }

    @Override
    public void execute(String[] args) {
        if (manager.getAllResults().isEmpty()) {
            System.out.println("Результатов нет.");
            return;
        }

        for (Result result : manager.getAllResults().values()) {
            System.out.println(
                    "ID: " + result.getId() +
                    " | Run ID: " + result.getRunId() +
                    " | Param: " + result.getParam() +
                    " | Value: " + result.getValue() +
                    " " + result.getUnit()
            );
        }
    }
}
