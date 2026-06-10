package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.ICollManager;
import org.example.network.data.Person;
import org.example.server.db.DatabaseManager;

import java.time.LocalDateTime;

/**
 * Команда для замены значения элемента в коллекции по ключу (ID),
 * если новое значение больше старого.
 * Сравнение объектов происходит по полю {@code height} (рост).
 * Если рост нового объекта больше роста существующего объекта с таким же ID,
 * то происходит замена.
 */
public class ReplaceIfGreater implements ICommand{
    private final ICollManager collectionManager;
    private final DatabaseManager dbManager;

    /**
     * Конструктор команды.
     *
     * @param collectionManager менеджер коллекции, предоставляющий доступ к данным
     */
    public ReplaceIfGreater(ICollManager collectionManager, DatabaseManager dbManager) {
        this.collectionManager = collectionManager;
        this.dbManager = dbManager;
    }

    /**
     * Возвращает название команды.
     * @return строка "replace_if_greater"
     */
    @Override
    public String getName() {
        return "replace_if_greater";
    }

    /**
     * Выполняет замену элемента, если новое значение больше старого.
     * Логика выполнения:
     *   1. Разбирает строку аргументов (CSV формат), извлекая ID и данные нового объекта.
     *   2. Проверяет наличие элемента с указанным ID в коллекции.
     *   3. Создает объект {@link Person} из переданных данных.
     *   4. Сравнивает поле height нового и старого объектов.
     *   5. Если новый рост строго больше старого, заменяет элемент в коллекции.
     * @param request строка с данными в формате CSV, где первый элемент — ID,
     *             а остальные — поля объекта Person.
     * @return сообщение о результате выполнения (замена произведена, замена не требуется или ошибка).
     */
    @Override
    public Response execute(Request request) {
        String args = request.args();
        if (args == null || args.trim().isEmpty()) {
            return new Response("Ошибка: укажите ID элемента.", ResponseStatus.ERROR);
        }

        int id;
        try {
            id = Integer.parseInt(args.trim());
        } catch (NumberFormatException e) {
            return new Response("Ошибка: ID должен быть целым числом.", ResponseStatus.ERROR);
        }

        Person oldPerson = collectionManager.getPersonById(id);
        if (oldPerson == null) {
            return new Response("Элемент с ID " + id + " не найден.", ResponseStatus.ERROR);
        }

        // ПРОВЕРКА ВЛАДЕЛЬЦА
        if (!oldPerson.getOwner().equals(request.username())) {
            return new Response("Ошибка: У вас нет прав на изменение чужого объекта.", ResponseStatus.ERROR);
        }

        Person newPerson = request.person();
        if (newPerson == null) {
            return new Response("Ошибка: отсутствует объект Person для сравнения.", ResponseStatus.ERROR);
        }

        if (newPerson.compareTo(oldPerson) > 0) {
            newPerson.setId(id);
            newPerson.setOwner(request.username());
            newPerson.setCreationDate(LocalDateTime.now());

            // 1. Обновляем в БД (можно использовать тот же updatePerson или отдельный метод)
            boolean updatedInDb = dbManager.updatePerson(newPerson);

            if (updatedInDb) {
                collectionManager.updatePerson(id, newPerson);
                return new Response("Элемент с ID " + id + " успешно заменен на больший.", ResponseStatus.OK);
            } else {
                return new Response("Ошибка при сохранении в базу данных.", ResponseStatus.ERROR);
            }
        } else {
            return new Response("Новый элемент не больше старого. Замена не произведена.", ResponseStatus.OK);
        }
    }

    /**
     * Возвращает описание команды для справки.
     * @return описание функционала команды
     */
    @Override
    public String getDescription() {
        return "заменяет значение по ключу, если новое значение больше старого";
    }

    /**
     * Указывает, что команда требует ввода сложного объекта (Person).
     * @return {@code true}, так как команда требует ввода данных объекта.
     */
    @Override
    public boolean requiresCompoundDataInput() {
        return true;
    }
}
