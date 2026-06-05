package cli.commands;

import domain.Result;
import manager.CollectionManager;

import java.util.Scanner;

/**
 * Команда отображения результата измерения по ID.
 */
public class ResultShowCommand extends Command {
    public ResultShowCommand(CollectionManager manager, Scanner scanner) {
        super(manager, scanner);
    }

    @Override
    public String name() {
        return "res_show";
    }

    @Override
    public void execute(String[] parts) {
        long resId;
        if (parts.length < 2) {
            System.out.print("ID результата: ");
            String input = scanner.nextLine();
            cancelIfCancelled(input);
            resId = parseId(input);
        } else {
            resId = parseId(parts[1]);
        }
        Result result = manager.getResultById(resId);
        System.out.println("ID: " + result.getId());
        System.out.println("ID запуска: " + result.getRunId());
        System.out.println("Параметр: " + result.getParam());
        System.out.println("Значение: " + result.getValue());
        System.out.println("Единицы измерения: " + result.getUnit());
        if (!result.getComment().isBlank()) {
            System.out.println("Комментарий: " + result.getComment());
        }
    }
}
