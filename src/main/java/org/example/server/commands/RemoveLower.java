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
 * Удаляет из коллекции все элементы, меньшие, чем заданный
 */
public class RemoveLower implements ICommand{
    private final ICollManager collectionManager;
    private final DatabaseManager dbManager;

    public RemoveLower(ICollManager collectionManager, DatabaseManager dbManager) {
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
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

        String currentUser = request.username();

        // Stream API: находим ID элементов, которые принадлежат пользователю и меньше шаблона
        List<Integer> idsToRemove = collectionManager.getAllPersons().stream()
                .filter(p -> p.getOwner().equals(currentUser)) // Только свои объекты
                .filter(p -> p.compareTo(template) < 0)        // Которые меньше шаблона
                .map(Person::getId)
                .collect(Collectors.toList());

        if (idsToRemove.isEmpty()) {
            return new Response("Нет принадлежащих вам элементов, меньших заданного.", ResponseStatus.OK);
        }

        // Удаляем из БД и из памяти
        int successCount = 0;
        for (Integer id : idsToRemove) {
            if (dbManager.deletePerson(id, currentUser)) {
                collectionManager.deletePerson(id);
                successCount++;
            }
        }

        return new Response("Удалено принадлежащих вам элементов: " + successCount, ResponseStatus.OK);
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
