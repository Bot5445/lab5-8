package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.IGetterSetter;
import org.example.network.data.Person;

/**
 * Команда для вывода суммы значений поля height для всех элементов коллекции.
 */
public class SumOfHeight implements ICommand{
    private final IGetterSetter collectionManager;

    /**
     * Создает команду подсчета суммы роста.
     * @param collectionManager менеджер коллекции для доступа к данным
     */
    public SumOfHeight(IGetterSetter collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Возвращает название команды.
     * @return строка "sum_of_height"
     */
    @Override
    public String getName() {
        return "sum_of_height";
    }

    /**
     * Вычисляет и возвращает сумму значений поля height всех объектов в коллекции.
     * @param request аргументы команды (не используются)
     * @return строковое представление суммы роста всех элементов
     */
    @Override
    public Response execute(Request request) {
        // Stream API: маппинг в Long, суммирование
        long sum = collectionManager.getAllPersons().stream()
                .mapToLong(Person::getHeight)
                .sum();

        return new Response("Сумма значений поля height: " + sum, ResponseStatus.OK);
    }

    /**
     * Возвращает описание команды для справки.
     * @return текстовое описание команды
     */
    @Override
    public String getDescription() {
        return "выводит сумму значений поля height для всех элементов коллекции";
    }

    /**
     * Команда sum_of_height не принимает аргументов
     * @return выводит false
     */
    @Override
    public boolean acceptsArguments() {
        return false;
    }
}
