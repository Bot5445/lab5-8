package org.example.server.commands;

import org.example.network.data.ICollManager;
import org.example.network.data.Person;
import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;

import java.time.LocalDateTime;

/**
 * Команда добавления нового элемента.
 * Сервер назначает авто-генерируемые поля (id, creationDate).
 */
public class Insert implements ICommand {
    private final ICollManager collectionManager;

    public Insert(ICollManager collectionManager) {
        this.collectionManager = collectionManager;
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
        Person person = request.getPerson();
        if (person == null) {
            return new Response("Ошибка: Отсутствует объект Person.", ResponseStatus.ERROR);
        }

        // 1. Генерация ID и Даты на сервере!
        int newId = collectionManager.generateNextId();
        person.setId(newId);
        person.setCreationDate(LocalDateTime.now());

        // 2. Добавление
        collectionManager.addPerson(person);
        return new Response("Элемент успешно добавлен с ID " + newId, ResponseStatus.OK);
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
