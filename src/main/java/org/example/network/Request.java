package org.example.network;

import org.example.network.data.Person;

import java.io.Serial;
import java.io.Serializable;

/**
 * Класс запроса от клиента к серверу.
 * <p>Реализует интерфейс {@link Serializable}, так как по заданию объекты между клиентом и сервером
 * должны передаваться в сериализованном виде. Обмен "простыми" строками недопустим.</p>
 *
 * @see Response
 */
public class Request implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final String commandName;
    private final String args;
    private final Person person;

    /**
     * Конструктор запроса.
     *
     * @param commandName название команды (например, "insert", "remove_key")
     * @param args        строковые аргументы команды (например, ключ или ID)
     * @param person      объект Person для команд, требующих составного ввода.
     *                    Может быть null, если команда не работает с объектом коллекции.
     */
    public Request(String commandName, String args, Person person) {
        this.commandName = commandName;
        this.args = args;
        this.person = person;
    }

    /**
     * Возвращает имя команды.
     * @return название команды
     */
    public String getCommandName() { return commandName; }

    /**
     * Возвращает строковые аргументы команды.
     * @return аргументы или null, если они отсутствуют
     */
    public String getArgs() { return args; }

    /**
     * Возвращает объект Person, прикрепленный к запросу.
     * @return объект коллекции или null
     */
    public Person getPerson() { return person; }
}