package org.example.client;

import org.example.network.CommandRules;
import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;
import org.example.network.data.Person;
import org.example.network.data.PersonFactory;

import java.util.Scanner;

/**
 * Главный класс клиентского приложения.
 */
public class ClientMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Использование: java ClientMain <host> <port>");
            System.exit(0);
        }

        String host = "localhost";//args[0];
        int port = 5555; //Integer.parseInt(args[1]);

        try (UDPClient udpClient = new UDPClient(host, port);
             Scanner scanner = new Scanner(System.in)) {

            PersonInputReader personReader = new PersonInputReader(scanner);
            ScriptExecutor scriptExecutor = new ScriptExecutor(udpClient); // Создаем 1 раз!
            System.out.println("Клиент запущен. Введите команду (help для справки):");

            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                String[] tokens = input.split("\\s+", 2);
                String commandName = tokens[0];
                String stringArgs = (tokens.length > 1) ? tokens[1] : null;

                // Обработка скриптов делегируется ScriptExecutor
                if ("execute_script".equals(commandName)) {
                    scriptExecutor.executeScript(stringArgs);
                    continue;
                }

                // Локальная обработка exit
                if ("exit".equals(commandName)) {
                    System.out.println("Завершение работы клиента.");
                    break;
                }

                Person person = null;

                // Используем общий CommandRules, проверят на запуск итеративного вода
                if (CommandRules.requiresCompoundData(commandName)) {
                    String[] personData = personReader.readPersonData(stringArgs);
                    try {
                        person = PersonFactory.createFromStringArray(personData);
                        stringArgs = null; // Данные перенесены в объект
                    } catch (Exception e) {
                        System.err.println("Ошибка валидации объекта: " + e.getMessage());
                        continue; // Пропускаем отправку, если объект невалиден
                    }
                }

                Request request = new Request(commandName, stringArgs, person);

                // Выносим печать ответа в отдельный метод
                processResponse(udpClient.sendRequest(request));
            }
        } catch (Exception e) {
            System.err.println("Критическая ошибка клиента: " + e.getMessage());
        }
    }

    /**
     * Обрабатывает ответ от сервера и выводит результат в консоль.
     * @param response ответ сервера (может быть null при таймауте)
     */
    private static void processResponse(Response response) {
        if (response == null) return; // UDPClient уже вывел сообщение о таймауте

        if (response.getStatus() == ResponseStatus.OK) {
            System.out.println(response.getMessage());
        } else {
            System.err.println("Ошибка: " + response.getMessage());
        }
    }
}