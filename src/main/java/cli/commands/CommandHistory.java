package cli.commands;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class CommandHistory {

    private final Deque<String> commands = new ArrayDeque<>();
    private final int maxSize;

    public CommandHistory(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("Размер истории должен быть положительным");
        }
        this.maxSize = maxSize;
    }

    public void add(String command) {
        if (command == null || command.isBlank()) {
            return;
        }

        while (commands.size() >= maxSize) {
            commands.removeFirst();
        }

        commands.addLast(command);
    }

    public List<String> getCommands() {
        return new ArrayList<>(commands);
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }
}
