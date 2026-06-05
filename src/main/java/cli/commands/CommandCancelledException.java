package cli.commands;

/**
 * Сигнал прерывания многошаговой команды (см. {@link Command#cancelIfCancelled(String)}).
 */
public final class CommandCancelledException extends RuntimeException {
}
