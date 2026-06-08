package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.ICollManager;

/**
 *Удаляет элемент из коллекции по его ключу
 */
public class RemoveKey implements ICommand {

    private final ICollManager collectionManager;

    public RemoveKey(ICollManager collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * @return название
     */
    @Override
    public String getName() {
        return "remove_key";
    }

    /**
     * @param request ID
     * @return удалился ли person
     */
    @Override
    public Response execute(Request request) {
        try {
            int id = Integer.parseInt(request.args().trim());
            if (!collectionManager.containsId(id)) {
                return new Response("Элемент с ID " + id + " не найден.", ResponseStatus.ERROR);
            }
            collectionManager.deletePerson(id);
            return new Response("Элемент с ID " + id + " удален.");
        } catch (Exception e) {
            return new Response("ID должен быть целым числом.", ResponseStatus.ERROR);
        }
    }

    /**
     * @return описание
     */
    @Override
    public String getDescription() {
        return "удаляет элемент из коллекции по его ключу";
    }
}
