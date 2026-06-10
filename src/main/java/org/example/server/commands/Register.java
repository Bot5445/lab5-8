package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.server.db.DatabaseManager;

/**
 * Команда для регистрации нового пользователя в системе.
 * <p>Принимает логин и пароль из объекта {@link Request}, хэширует пароль
 * с использованием алгоритма SHA-384 и сохраняет учетные данные в базу данных.</p>
 * <p>Если пользователь с таким логином уже существует, возвращает ошибку.</p>
 */
public class Register implements ICommand {
    private final DatabaseManager dbManager;

    public Register(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * @return название
     */
    @Override
    public String getName() { return "register"; }

    @Override
    public Response execute(Request request) {
        String username = request.username();
        String password = request.password();

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return new Response("Ошибка: Логин и пароль не могут быть пустыми.", ResponseStatus.ERROR);
        }

        boolean success = dbManager.registerUser(username, password);
        if (success) {
            return new Response("Пользователь '" + username + "' успешно зарегистрирован.", ResponseStatus.OK);
        } else {
            return new Response("Ошибка: Пользователь с таким логином уже существует или произошла ошибка БД.", ResponseStatus.ERROR);
        }
    }
    /**
     * @return описание
     */
    @Override
    public String getDescription() { return "регистрирует нового пользователя"; }

    /**
     * Команда register не принимает аргументов
     * @return выводит false
     */
    @Override
    public boolean acceptsArguments() { return false; }
}