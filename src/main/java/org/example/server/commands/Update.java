package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.ICollManager;
import org.example.network.data.Person;

import java.time.LocalDateTime;

/**
 * Команда для обновления значения конкретного поля элемента коллекции по его ID.
 */
public class Update implements ICommand{
    private final ICollManager collectionManager;

    /**
     * Создает команду обновления элемента.
     * @param collectionManager менеджер коллекции, предоставляющий доступ к данным
     */
    public Update(ICollManager collectionManager) {
        this.collectionManager = collectionManager;
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
            Person person = request.person();

            if (!collectionManager.containsId(id)) {
                return new Response("Элемента с ID " + id + " не существует.", ResponseStatus.ERROR);
            }

            // Назначаем ID и дату старому/новому объекту (по логике обновления)
            person.setId(id);
            person.setCreationDate(LocalDateTime.now());

            collectionManager.updatePerson(id, person);
            return new Response("Элемент с ID " + id + " обновлен.", ResponseStatus.OK);
        } catch (Exception e) {
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
