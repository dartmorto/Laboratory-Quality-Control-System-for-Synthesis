package validation;

/**
 * Класс для проверки корректности входных данных.
 * Содержит статические методы для валидации значений,
 * используемых при создании и обновлении сущностей.
 * Принцип работы: при некорректных данных выбрасывается
 * IllegalArgumentException.
 * Класс не имеет состояния и не может быть инстанцирован.
 */
public final class Validator {

    private Validator() {}

    public static void requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " должен быть положительным"
            );
        }
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " не может быть пустым"
            );
        }
    }

    public static void requireNotNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException(
                    fieldName + " не может быть null"
            );
        }
    }

    public static void requireExists(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}