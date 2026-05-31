package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.IGetterSetter;
import org.example.network.data.Person;

import java.util.stream.Collectors;

/**
 * Команда для фильтрации элементов коллекции по префиксу имени.
 * Выводит элементы, значение поля name которых начинается с заданной подстроки.
 */
public class FilterStartsWithName implements ICommand {

    private final IGetterSetter collectionManager;

    /**
     * Создает команду фильтрации по имени.
     * @param collectionManager менеджер коллекции для доступа к данным
     */
    public FilterStartsWithName(IGetterSetter collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Возвращает название команды.
     * @return строка "filter_starts_with_name"
     */
    @Override
    public String getName() {
        return "filter_starts_with_name";
    }

    /**
     * Выполняет поиск элементов, имя которых начинается с указанного префикса.
     * Префикс не должен содержать цифр.
     * @param request префикс строки для поиска в имени
     * @return строка с найденными элементами или сообщение об ошибке/отсутствии результатов
     * @throws Exception при ошибке доступа к коллекции
     */
    @Override
    public Response execute(Request request) {
        String prefix = request.getArgs();
        if (prefix == null || prefix.trim().isEmpty()) {
            return new Response("Ошибка: укажите префикс.", ResponseStatus.ERROR);
        }

        // Stream API: фильтрация, маппинг, склейка
        String result = collectionManager.getAllPersons().stream()
                .filter(p -> p.getName().startsWith(prefix.trim()))
                .map(Person::toString)
                .collect(Collectors.joining("\n"));

        if (result.isEmpty()) return new Response("Элементы не найдены.", ResponseStatus.OK);
        return new Response(result, ResponseStatus.OK);
    }

    /**
     * Возвращает описание команды для справки.
     * @return текстовое описание команды
     */
    @Override
    public String getDescription() {
        return "выводит элементы, значение поля name которых начинается с заданной подстроки";
    }
}
