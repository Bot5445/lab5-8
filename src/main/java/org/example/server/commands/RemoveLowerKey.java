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
 * Команда для удаления всех элементов коллекции, ключ (ID) которых меньше заданного.
 */
public class RemoveLowerKey implements ICommand {
    private final ICollManager collectionManager;
    private final DatabaseManager dbManager;

    /**
     * Создает команду remove_lower_key.
     * @param collectionManager менеджер коллекции
     */
    public RemoveLowerKey(ICollManager collectionManager, DatabaseManager dbManager) {
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
    }

    /**
     * Возвращает название команды.
     * @return строка "remove_lower_key"
     */
    @Override
    public String getName() {
        return "remove_lower_key";
    }

    /**
     * Удаляет элементы, ключ которых меньше указанного.
     * Извлекает пороговый ID из строковых аргументов запроса и делегирует удаление менеджеру коллекции.
     *
     * @param request запрос от клиента, содержащий пороговый ID в аргументах
     * @return ответ с количеством удаленных элементов или сообщением об ошибке
     */
    @Override
    public Response execute(Request request) {
        String args = request.args();
        if (args == null || args.trim().isEmpty()) {
            return new Response("Ошибка: укажите ключ (ID) для сравнения.", ResponseStatus.ERROR);
        }

        int thresholdId;
        try {
            thresholdId = Integer.parseInt(args.trim());
        } catch (NumberFormatException e) {
            return new Response("Ошибка: ключ должен быть целым числом.", ResponseStatus.ERROR);
        }

        String currentUser = request.username();

        // Stream API: фильтрация по ключу И по владельцу
        List<Integer> idsToRemove = collectionManager.getAllPersons().stream()
                .filter(p -> p.getId() < thresholdId)
                .filter(p -> p.getOwner() != null && p.getOwner().equals(currentUser))
                .map(Person::getId)
                .collect(Collectors.toList());

        if (idsToRemove.isEmpty()) {
            return new Response("Нет принадлежащих вам элементов с ключом меньше " + thresholdId + ".", ResponseStatus.OK);
        }

        int deletedCount = 0;
        for (Integer id : idsToRemove) {
            if (dbManager.deletePerson(id, currentUser)) {
                collectionManager.deletePerson(id);
                deletedCount++;
            }
        }

        return new Response("Удалено принадлежащих вам элементов с ключом меньше " + thresholdId + ": " + deletedCount, ResponseStatus.OK);
    }

    /**
     * Возвращает описание команды для справки.
     * @return текстовое описание команды
     */
    @Override
    public String getDescription() {
        return "удаляет из коллекции все элементы, ключ которых меньше, чем заданный";
    }
}