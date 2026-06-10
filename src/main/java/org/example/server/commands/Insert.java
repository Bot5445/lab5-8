package org.example.server.commands;

import org.example.network.data.ICollManager;
import org.example.network.data.Person;
import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.server.db.DatabaseManager;

import java.time.LocalDateTime;

/**
 * Команда добавления нового элемента.
 * Сервер назначает авто-генерируемые поля (id, creationDate).
 */
public class Insert implements ICommand {
    private final ICollManager collectionManager;
    private final DatabaseManager dbManager;

    public Insert(ICollManager collectionManager, DatabaseManager dbManager) {
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
    }

    @Override
    public String getName() {
        return "insert";
    }

    /**
     * Добавляет объект Person в коллекцию.
     * Назначает автоматически генерируемые поля (id и creationDate).
     *
     * @param request запрос от клиента, содержащий объект Person
     * @return ответ об успешном добавлении или ошибке
     */
    @Override
    public Response execute(Request request) {
        Person person = request.person();
        if (person == null) return new Response("Ошибка: Отсутствует объект Person.", ResponseStatus.ERROR);

        // Сервер сам назначает владельца на основе авторизованного запроса (защита от подделки)
        person.setOwner(request.username());
        person.setCreationDate(LocalDateTime.now());

        // 1. Сначала сохраняем в БД (там же сработает sequence для ID)
        Person savedPerson = dbManager.insertPerson(person);

        if (savedPerson != null) {
            // 2. Обновляем состояние коллекции в памяти ТОЛЬКО при успешном добавлении в БД
            collectionManager.addPerson(savedPerson);
            return new Response("Элемент успешно добавлен с ID " + savedPerson.getId(), ResponseStatus.OK);
        } else {
            return new Response("Ошибка: Не удалось сохранить объект в базе данных.", ResponseStatus.ERROR);
        }
    }

    /**
     * Возвращает описание команды для справки.
     * @return текстовое описание
     */
    @Override
    public String getDescription() {
        return "добавляет нового Person. Если первый аргумент число - это ID, иначе ID генерируется. " +
                "Формат полей: name рост [паспортID]";
//                "Формат полей: " + Arrays.toString(Person.getHeaders());
    }

    /**
     * Указывает, что для выполнения команды требуется интерактивный ввод данных объекта.
     * @return true, так как команда требует ввода составного объекта
     */
    @Override
    public boolean requiresCompoundDataInput() { return true; }
}
