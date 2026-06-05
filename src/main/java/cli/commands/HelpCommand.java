package cli.commands;

import manager.CollectionManager;

import java.util.Scanner;

/**
 * Команда вывода списка доступных команд.
 */
public class HelpCommand extends Command {

    public HelpCommand(CollectionManager manager, Scanner scanner) {
        super(manager, scanner);
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public void execute(String[] args) {
        System.out.println("""
                Доступные команды:
                register          - регистрация пользователя
                login             - вход пользователя
                create_experiment - создать эксперимент
                create_run        - создать запуск
                create_result     - создать ручной результат
                detect_sample     - выполнить детекцию образца по изображению и сохранить результат
                exp_update        - обновить запись об эксперименте
                exp_show          - показать эксперимент
                run_show          - показать запуск
                res_show          - показать результат
                exp_list          - показать все эксперименты
                run_list          - показать все запуски
                res_list          - показать все результаты
                exp_summary       - сводка по эксперименту
                history           - история команд
                exit              - выход
                Во время пошагового ввода: cancel, отмена или q - прервать команду
                """);
    }
}
