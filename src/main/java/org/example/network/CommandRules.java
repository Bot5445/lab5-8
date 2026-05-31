package org.example.network;

import java.util.Set;

/**
 * Утилитный класс, содержащий общие правила и метаданные о командах.
 * Решает проблему дублирования (DRY) при проверке типов команд.
 */
public final class CommandRules {
    // Команды, требующие интерактивного ввода объекта Person
    private static final Set<String> COMPOUND_COMMANDS = Set.of(
            "insert", "update", "remove_lower", "replace_if_greater"
    );

    private CommandRules() {} // Запрещаем создание экземпляров

    /**
     * Проверяет, требует ли команда интерактивного ввода составного объекта Person.
     * @param commandName имя команды
     * @return true, если команда требует ввода объекта
     */
    public static boolean requiresCompoundData(String commandName) {
        return COMPOUND_COMMANDS.contains(commandName);
    }
}