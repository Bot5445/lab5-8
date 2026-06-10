package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.ICollManager;
import org.example.network.data.Person;
import org.example.server.db.DatabaseManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Очищает коллекцию
 */
public class Clear implements ICommand {
    private final ICollManager collectionManager;
    private final DatabaseManager dbManager;

    /**
     * Создает команду очистки коллекции.
     * @param collectionManager менеджер коллекции, который будет очищен
     */
    public Clear(ICollManager collectionManager, DatabaseManager dbManager) {
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
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
        String currentUser = request.username();

        // Stream API: находим ID только тех элементов, которые принадлежат пользователю
        List<Integer> idsToClear = collectionManager.getAllPersons().stream()
                .filter(p -> p.getOwner() != null && p.getOwner().equals(currentUser))
                .map(Person::getId)
                .collect(Collectors.toList());

        if (idsToClear.isEmpty()) {
            return new Response("У вас нет элементов для удаления.", ResponseStatus.OK);
        }

        int deletedCount = 0;
        for (Integer id : idsToClear) {
            // Удаляем из БД (с проверкой owner) и из памяти
            if (dbManager.deletePerson(id, currentUser)) {
                collectionManager.deletePerson(id);
                deletedCount++;
            }
        }

        return new Response("Коллекция очищена от ваших элементов. Удалено: " + deletedCount, ResponseStatus.OK);
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
