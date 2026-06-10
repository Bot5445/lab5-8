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
public record Request(String commandName, String args, Person person, String username, String password) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Конструктор запроса.
     *
     * @param commandName название команды (например, "insert", "remove_key")
     * @param args        строковые аргументы команды (например, ключ или ID)
     * @param person      объект Person для команд, требующих составного ввода.
     *                    Может быть null, если команда не работает с объектом коллекции.
     */
    public Request {
    }

    /**
     * Возвращает имя команды.
     *
     * @return название команды
     */
    @Override
    public String commandName() {
        return commandName;
    }

    /**
     * Возвращает строковые аргументы команды.
     *
     * @return аргументы или null, если они отсутствуют
     */
    @Override
    public String args() {
        return args;
    }

    /**
     * Возвращает объект Person, прикрепленный к запросу.
     *
     * @return объект коллекции или null
     */
    @Override
    public Person person() {
        return person;
    }
}