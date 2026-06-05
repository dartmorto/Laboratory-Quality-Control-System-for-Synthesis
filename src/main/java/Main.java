import cli.CommandHandler;
import manager.CollectionManager;

/**
 * Entry point for the sample detection results backend.
 */
public class Main {
    public static void main(String[] args) {
        CollectionManager manager = new CollectionManager();
        CommandHandler commandHandler = new CommandHandler(manager);

        System.out.println("Бэкенд детекции образца запущен");
        commandHandler.start();
    }
}
