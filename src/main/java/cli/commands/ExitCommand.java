package cli.commands;

import manager.CollectionManager;

import java.util.Scanner;

/**
 * Команда завершения работы приложения.
 */

public class ExitCommand extends Command {

    public ExitCommand(CollectionManager manager, Scanner scanner) {
        super(manager, scanner);
    }

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public void execute(String[] args) {
        System.out.println("Выход из программы...");
        System.exit(0);
    }
}
