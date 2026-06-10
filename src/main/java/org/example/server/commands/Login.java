package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.server.db.DatabaseManager;

/**
 * Команда для аутентификации (входа) пользователя в систему.
 * <p>Принимает логин и пароль из объекта {@link Request}, хэширует введенный пароль
 * и сравнивает его с хэшем, хранящимся в базе данных.</p>
 * <p>При успешном совпадении разрешает выполнение последующих команд.</p>
 */
public class Login implements ICommand {
    private final DatabaseManager dbManager;

    public Login(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * @return название
     */
    @Override
    public String getName() { return "login"; }

    @Override
    public Response execute(Request request) {
        String username = request.username();
        String password = request.password();

        if (dbManager.authenticate(username, password)) {
            return new Response("Аутентификация успешна. Добро пожаловать, " + username + "!", ResponseStatus.OK);
        } else {
            return new Response("Ошибка: Неверный логин или пароль.", ResponseStatus.ERROR);
        }
    }

    /**
     * @return описание
     */
    @Override
    public String getDescription() { return "выполняет вход в систему"; }

    /**
     * Команда login не принимает аргументов
     * @return выводит false
     */
    @Override
    public boolean acceptsArguments() { return false; }
}