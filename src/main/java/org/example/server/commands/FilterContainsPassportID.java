package org.example.server.commands;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.IGetterSetter;
import org.example.network.data.Person;

/**
 * Команда для фильтрации элементов коллекции по подстроке в поле passportID.
 * Выводит элементы, значение поля passportID которых содержит заданную подстроку.
 */
public class FilterContainsPassportID implements ICommand {
    private final IGetterSetter collectionManager;

    /**
     * Создает команду фильтрации по passportID.
     * @param collectionManager менеджер коллекции для доступа к данным
     */
    public FilterContainsPassportID(IGetterSetter collectionManager) {
        this.collectionManager = collectionManager;
    }

    /**
     * Возвращает название команды.
     * @return строка "filter_contains_passport_i_d"
     */
    @Override
    public String getName() {
        return "filter_contains_passport_i_d";
    }

    /**
     * Выполняет поиск элементов, passportID которых содержит указанную подстроку.
     * Аргумент должен состоять только из цифр (пробелы игнорируются).
     * @param args подстрока для поиска в passportID
     * @return строка с найденными элементами или сообщение об ошибке/отсутствии результатов
     * @throws Exception при ошибке доступа к коллекции
     */
    @Override
    public Response execute(Request args) {
        if (args == null || args.getArgs().trim().isEmpty() || !args.getArgs().replace(" ", "").matches("\\d+")) {
            return new Response("Ошибка: укажите строку, состоящая из чисел, для поиска в passportID.", ResponseStatus.ERROR);
        }
        String substring = args.getArgs().trim().replace(" ", "");

        StringBuilder result = new StringBuilder();
        boolean found = false;

        for (Person p : collectionManager.getAllPersons()) {
            String passport = p.getPassportID();

            // Проверяем, что passportID не null и содержит подстроку
            if (passport != null && passport.contains(substring)) {
                result.append(p.getId() + ": " + p + "\n");
                found = true;
            }
        }
        if (!found) {
            return new Response("Элементы с passportID, содержащим '" + substring + "', не найдены.");
        }

        return new Response(result.toString());
    }

    /**
     * Возвращает описание команды для справки.
     * @return текстовое описание команды
     */
    @Override
    public String getDescription() {
        return "выводит элементы, значение поля passportID которых содержит заданную подстроку.";
    }
}
