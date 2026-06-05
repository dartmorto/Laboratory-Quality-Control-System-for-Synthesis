package cli;

import cli.commands.*;
import manager.*;
import user.AuthService;
import user.UserRepository;

import java.util.*;

/**
 * Обрабатывает пользовательский ввод и запускает команды.
 */
public class CommandHandler {

    private final CollectionManager manager;
    private final AuthService authService;
    private final Scanner scanner;
    private final Map<String, Command> commands = new HashMap<>();
    private final CommandHistory history;

    public CommandHandler(CollectionManager manager) {
        this(manager, new AuthService(new UserRepository()));
    }

    public CommandHandler(CollectionManager manager, AuthService authService) {
        this.manager = Objects.requireNonNull(manager, "manager");
        this.authService = Objects.requireNonNull(authService, "authService");
        this.scanner = new Scanner(System.in);
        this.history = new CommandHistory(readHistorySize());

        registerCommands();
    }

    /**
     * Запускает основной цикл обработки команд.
     */
    public void start() {
        System.out.println("Система управления экспериментами");
        System.out.println("Введите 'help' для списка команд");

        while (true) {
            System.out.print("> ");

            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            executeCommand(line);
        }
    }

    /**
     * Выполняет команду, введённую пользователем.
     *
     * @param line строка пользовательского ввода
     */
    private void executeCommand(String line) {
        String[] parts = line.split("\\s+");
        String commandName = parts[0];

        Command command = commands.get(commandName);

        if (command == null) {
            System.out.println("Неизвестная команда");
            return;
        }

        if (!commandName.equals("history") && !commandName.equals("help")) {
            history.add(line);
        }

        try {
            command.execute(parts);
        } catch (CommandCancelledException e) {
            System.out.println("Команда отменена.");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    /**
     * Регистрирует все доступные команды.
     */
    private void registerCommands() {
        List<Command> availableCommands = List.of(
                new RegisterCommand(manager, scanner, authService),
                new LoginCommand(manager, scanner, authService),
                new HelpCommand(manager, scanner),
                new ExitCommand(manager, scanner),
                new ExpCreateCommand(manager, scanner, authService),
                new ExpListCommand(manager, scanner),
                new ExpShowCommand(manager, scanner),
                new ExpUpdateCommand(manager, scanner, authService),
                new ExpSummaryCommand(manager, scanner),
                new RunCreateCommand(manager, scanner, authService),
                new RunListCommand(manager, scanner),
                new RunShowCommand(manager, scanner),
                new ResultCreateCommand(manager, scanner, authService),
                new ResultListCommand(manager, scanner),
                new ResultShowCommand(manager, scanner),
                new SampleDetectCommand(manager, scanner, authService),
                new HistoryCommand(manager, scanner, history)
        );
        availableCommands.forEach(this::register);
    }

    /**
     * Регистрирует одну команду.
     *
     * @param command команда
     */
    private void register(Command command) {
        commands.put(command.name(), command);
    }

    /**
     * Считывает размер истории команд.
     *
     * @return положительное целое число
     */
    private int readHistorySize() {
        while (true) {
            System.out.print("Введите количество команд, сохраняемых в истории: ");

            String input = scanner.nextLine().trim();

            try {
                int size = Integer.parseInt(input);

                if (size > 0) {
                    return size;
                }

                System.out.println("Ошибка: число должно быть положительным.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }
}


