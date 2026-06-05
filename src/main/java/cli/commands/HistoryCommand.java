package cli.commands;
import manager.CollectionManager;

import java.util.List;
import java.util.Scanner;

public class HistoryCommand extends Command {

    private final CommandHistory history;

    public HistoryCommand(CollectionManager manager, Scanner scanner, CommandHistory history) {
        super(manager, scanner);
        this.history = history;
    }

    @Override
    public String name() {
        return "history";
    }

    @Override
    public void execute(String[] args) {
        if (history.isEmpty()) {
            System.out.println("История команд пуста.");
            return;
        }

        List<String> commands = history.getCommands();

        System.out.println("История последних команд:");

        for (int i = 0; i < commands.size(); i++) {
            System.out.println((i + 1) + ". " + commands.get(i));
        }
    }
}
