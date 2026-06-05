package cli.commands;

import domain.MeasurementParam;
import domain.Result;
import domain.Run;
import manager.CollectionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Команда вывода сводной статистики по эксперименту.
 * Рассчитывает min/max/avg по каждому параметру
 * на основе всех запусков эксперимента.
 */

public class ExpSummaryCommand extends Command {

    public ExpSummaryCommand(CollectionManager manager, Scanner scanner) {
        super(manager, scanner);
    }

    @Override
    public String name() {
        return "exp_summary";
    }

    @Override
    public void execute(String[] args) {

        Long expId;
        while (true) {
            System.out.print("ID эксперимента: ");
            try {
                String line = scanner.nextLine();
                cancelIfCancelled(line);
                expId = Long.parseLong(line);
                if (expId > 0) break;
                System.out.println("Ошибка: ID должен быть положительным");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число");
            }
        }

        try {
            manager.getById(expId);
        } catch (IllegalArgumentException e) {
            System.out.println("Эксперимент не найден");
            return;
        }

        Map<MeasurementParam, List<Double>> stats = new HashMap<>();

        for (Run run : manager.getAllRuns().values()) {
            if (run.getExperimentId() != expId) continue;

            for (Result res : manager.getResultsByRunId(run.getId()).values()) {
                stats.computeIfAbsent(res.getParam(), k -> new ArrayList<>())
                        .add(res.getValue());
            }
        }

        if (stats.isEmpty()) {
            System.out.println("Нет данных");
            return;
        }

        stats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    MeasurementParam param = entry.getKey();
                    List<Double> values = entry.getValue();

                    double min = Collections.min(values);
                    double max = Collections.max(values);
                    double avg = values.stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0);

                    System.out.printf(
                            "%s: count=%d min=%.2f max=%.2f avg=%.2f%n",
                            param,
                            values.size(),
                            min,
                            max,
                            avg
                    );
                });
    }
}
