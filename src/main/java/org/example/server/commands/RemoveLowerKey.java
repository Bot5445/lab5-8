package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.ICollManager;

/**
 * Команда для удаления всех элементов коллекции, ключ (ID) которых меньше заданного.
 */
public class RemoveLowerKey implements ICommand {
    private final ICollManager collectionManager;

    /**
     * Создает команду remove_lower_key.
     * @param collectionManager менеджер коллекции
     */
    public RemoveLowerKey(ICollManager collectionManager) {
        this.collectionManager = collectionManager;
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
        // 1. Получаем аргументы из запроса
        String args = request.getArgs();

        if (args == null || args.trim().isEmpty()) {
            return new Response("Ошибка: укажите ключ (ID) для сравнения.", ResponseStatus.ERROR);
        }

        try {
            // 2. Парсим ID
            int thresholdId = Integer.parseInt(args.trim());

            // 3. Вызываем метод менеджера (он уже реализован через Stream API!)
            int removedCount = collectionManager.removeLowerKey(thresholdId);

            // 4. Возвращаем успешный ответ
            return new Response("Удалено элементов с ключом меньше " + thresholdId + ": " + removedCount, ResponseStatus.OK);

        } catch (NumberFormatException e) {
            // Если клиент почему-то прислал не число (хотя должен был валидировать)
            return new Response("Ошибка: ключ должен быть целым числом.", ResponseStatus.ERROR);
        }
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