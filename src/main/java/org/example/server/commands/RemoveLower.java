package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.ICollManager;
import org.example.network.data.Person;

/**
 * Удаляет из коллекции все элементы, меньшие, чем заданный
 */
public class RemoveLower implements ICommand{
    private final ICollManager collectionManager;

    public RemoveLower(ICollManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * @return название
     */
    @Override
    public String getName() {
        return "remove_lower";
    }

    /**
     * @param request данные для сравнения
     * @return сколько удалено объектов
     */
    @Override
    public Response execute(Request request) {
        Person template = request.person();
        if (template == null) return new Response("Ошибка: нет объекта для сравнения.", ResponseStatus.ERROR);

        // Используем Stream API!
        int removedCount = collectionManager.removeLower(template);
        return new Response("Удалено элементов: " + removedCount, ResponseStatus.OK);
    }

    /**
     * @return описание
     */
    @Override
    public String getDescription() {
        return "удаляет из коллекции все элементы, меньшие, чем заданный";
    }

    @Override
    public boolean requiresCompoundDataInput() {
        return true;
    }
}
