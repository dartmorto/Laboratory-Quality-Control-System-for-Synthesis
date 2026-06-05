package cli.commands;

import domain.MeasurementParam;
import domain.Result;
import manager.CollectionManager;
import user.AuthService;

import java.util.Scanner;

/**
 * Команда создания результата измерения.
 * Добавляет измерение к конкретному запуску.
 */

public class ResultCreateCommand extends Command {
    public ResultCreateCommand(CollectionManager manager, Scanner scanner, AuthService authService) {
        super(manager, scanner, authService);
    }


    @Override
    public String name() {
        return "create_result";
    }

    @Override
    public void execute(String[] parts) {
        requireLogin();

        long runId;
        if (parts.length < 2) {
            System.out.print("ID запуска: ");
            String input = scanner.nextLine();
            cancelIfCancelled(input);
            runId = parseId(input);
        } else {
            runId = parseId(parts[1]);
        }
        try {
            manager.getRunById(runId);
        } catch (IllegalArgumentException e) {
            System.out.println("Запуск не найден");
            return;
        }

        System.out.println("Комментарий(по желанию)");
        String comment = scanner.nextLine();
        cancelIfCancelled(comment);

        MeasurementParam param;

        String paramInput;
        while (true) {
            System.out.println("Введите параметр (например TEMPERATURE):");
            paramInput = scanner.nextLine().trim().toUpperCase();
            cancelIfCancelled(paramInput);

            try {
                param = MeasurementParam.valueOf(paramInput);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: неизвестный параметр. Попробуйте снова.");
                System.out.println("Доступные параметры:");
                for (MeasurementParam p : MeasurementParam.values()) {
                    System.out.println("- " + p);
                }

            }
        }

        double value;
        while (true) {
            System.out.println("Введите значение:");
            String valueInput = scanner.nextLine().trim();
            cancelIfCancelled(valueInput);
            try {
                value = Double.parseDouble(valueInput);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число");
            }
        }

        System.out.println("Единица измерения");
        String unit = scanner.nextLine();
        cancelIfCancelled(unit);

        Result result = manager.createResult(runId, param, value, unit, comment);
        System.out.println("Результат успешно создан. ID: " + result.getId());
    }
}
