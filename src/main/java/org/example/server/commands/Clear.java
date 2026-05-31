package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.data.IGetterSetter;

/**
 * Очищает коллекцию
 */
public class Clear implements ICommand {
    private final IGetterSetter collectionManager;

    /**
     * Создает команду очистки коллекции.
     * @param collectionManager менеджер коллекции, который будет очищен
     */
    public Clear(IGetterSetter collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * @return название
     */
    @Override
    public String getName() {
        return "clear";
    }

    /**
     * @param request аргументы
     * @return отчищена ли коллекция
     */
    @Override
    public Response execute(Request request) {
        collectionManager.getPerson().clear();
        return new Response("Коллекция отчищена");
    }

    /**
     * @return описание
     */
    @Override
    public String getDescription() {
        return "очищает коллекцию";
    }

    /**
     * Команда clear не принимает аргументов
     * @return выводит false
     */
    @Override
    public boolean acceptsArguments() {
        return false;
    }
}
