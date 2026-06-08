package org.example.network;

import java.io.Serial;
import java.io.Serializable;

/**
 * Класс ответа от сервера клиенту.
 * <p>Инкапсулирует результат выполнения команды на сервере.</p>
 *
 * @see Request
 */
public record Response(String message, ResponseStatus status) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Конструктор ответа.
     *
     * @param message текстовое сообщение с результатом успешного выполнения
     */
    public Response(String message) {
        this(message, ResponseStatus.OK);
    }

    /**
     * Конструктор ответа.
     *
     * @param message текстовое сообщение с результатом выполнения или ошибкой
     * @param status  статус выполнения команды ({@link ResponseStatus#OK} или {@link ResponseStatus#ERROR})
     */
    public Response {
    }

    /**
     * Возвращает сообщение сервера.
     *
     * @return текст результата
     */
    @Override
    public String message() {
        return message;
    }

    /**
     * Возвращает статус ответа.
     *
     * @return статус выполнения
     */
    @Override
    public ResponseStatus status() {
        return status;
    }
}