package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.ICollManager;
import org.example.network.data.Person;
import org.example.server.db.DatabaseManager;

/**
 * Команда для обновления значения конкретного поля элемента коллекции по его ID.
 */
public class Update implements ICommand{
    private final ICollManager collectionManager;
    private final DatabaseManager dbManager;

    /**
     * Создает команду обновления элемента.
     * @param collectionManager менеджер коллекции, предоставляющий доступ к данным
     */
    public Update(ICollManager collectionManager, DatabaseManager dbManager) {
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
    }

    /**
     * Возвращает название команды.
     * @return строка "update"
     */
    @Override
    public String getName() {
        return "update";
    }

    /**
     * Выполняет обновление поля объекта.
     * Ожидает аргументы в формате: "ID имя_поля новое_значение".
     * Если элемент с указанным ID существует, создает его копию с обновленным полем.
     * @param request строка с аргументами, разделенными пробелами (ID, имя поля, значение)
     * @return сообщение об успешном обновлении или текст ошибки
     * @throws Exception если произошла ошибка при поиске элемента или обновлении поля
     */
    @Override
    public Response execute(Request request) {
        try {
            int id = Integer.parseInt(request.args().trim());
            Person oldPerson = collectionManager.getPersonById(id);

            if (oldPerson == null) {
                return new Response("Элемента с ID " + id + " не существует.", ResponseStatus.ERROR);
            }

            // ПРОВЕРКА ВЛАДЕЛЬЦА
            if (!oldPerson.getOwner().equals(request.username())) {
                return new Response("Ошибка: У вас нет прав на изменение чужого объекта.", ResponseStatus.ERROR);
            }

            Person newPerson = request.person();
            newPerson.setId(id);
            newPerson.setOwner(request.username()); // Сохраняем владельца
            newPerson.setCreationDate(oldPerson.getCreationDate()); // Дату создания обычно не меняют при update

            // 1. Обновляем в БД
            boolean updatedInDb = dbManager.updatePerson(newPerson); // Метод нужно добавить в DatabaseManager (UPDATE persons SET ... WHERE id=? AND owner=?)

            if (updatedInDb) {
                // 2. Обновляем в памяти только при успехе
                collectionManager.updatePerson(id, newPerson);
                return new Response("Элемент с ID " + id + " успешно обновлен.", ResponseStatus.OK);
            } else {
                return new Response("Ошибка при обновлении объекта в базе данных.", ResponseStatus.ERROR);
            }
        } catch (NumberFormatException e) {
            return new Response("Неверный формат ID.", ResponseStatus.ERROR);
        }
    }

    /**
     * Возвращает описание команды для справки.
     * @return текстовое описание команды
     */
    @Override
    public String getDescription() {
        return "обновить значение поля элемента по ID";
    }

    /**
     * Указывает, что команда требует ввода сложных данных.
     * Возвращает true, что сигнализирует о необходимости интерактивного ввода
     * или специальной обработки аргументов.
     * @return true
     */
    @Override
    public boolean requiresCompoundDataInput() {
        return true;
    }
}
