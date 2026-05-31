package org.example.network;

import java.io.Serializable;

/**
 * Класс ответа от сервера клиенту.
 * <p>Инкапсулирует результат выполнения команды на сервере.</p>
 *
 * @see Request
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String message;
    private final ResponseStatus status;

    /**
     * Конструктор ответа.
     *
     * @param message текстовое сообщение с результатом успешного выполнения
     */
    public Response(String message) {
        this.message = message;
        this.status = ResponseStatus.OK;
    }

    /**
     * Конструктор ответа.
     *
     * @param message текстовое сообщение с результатом выполнения или ошибкой
     * @param status  статус выполнения команды ({@link ResponseStatus#OK} или {@link ResponseStatus#ERROR})
     */
    public Response(String message, ResponseStatus status) {
        this.message = message;
        this.status = status;
    }

    /**
     * Возвращает сообщение сервера.
     * @return текст результата
     */
    public String getMessage() { return message; }

    /**
     * Возвращает статус ответа.
     * @return статус выполнения
     */
    public ResponseStatus getStatus() { return status; }
}