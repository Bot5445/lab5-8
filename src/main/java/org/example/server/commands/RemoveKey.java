package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.ICollManager;
import org.example.network.data.Person;
import org.example.server.db.DatabaseManager;

/**
 *Удаляет элемент из коллекции по его ключу
 */
public class RemoveKey implements ICommand {

    private final ICollManager collectionManager;
    private final DatabaseManager dbManager;

    public RemoveKey(ICollManager collectionManager, DatabaseManager dbManager) {

        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
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
            Person person = collectionManager.getPersonById(id);

            if (person == null) {
                return new Response("Элемент с ID " + id + " не найден.", ResponseStatus.ERROR);
            }

            // Проверка прав на модификацию
            if (!person.getOwner().equals(request.username())) {
                return new Response("Ошибка: У вас нет прав на удаление чужого объекта.", ResponseStatus.ERROR);
            }

            // Удаляем из БД
            if (dbManager.deletePerson(id, request.username())) {
                // Удаляем из памяти
                collectionManager.deletePerson(id);
                return new Response("Элемент с ID " + id + " удален.");
            } else {
                return new Response("Ошибка при удалении из базы данных.", ResponseStatus.ERROR);
            }
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
