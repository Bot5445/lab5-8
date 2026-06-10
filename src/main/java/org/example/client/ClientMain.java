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
        String host = "localhost"; // args[0];
        int port = 5555; // Integer.parseInt(args[1]);

        try (UDPClient udpClient = new UDPClient(host, port);
             Scanner scanner = new Scanner(System.in)) {

            PersonInputReader personReader = new PersonInputReader(scanner);

            // === ЭТАП АВТОРИЗАЦИИ ===
            System.out.println("=== Авторизация ===");
            String username = "";
            String password = "";
            boolean isAuthenticated = false;

            while (!isAuthenticated) {
                System.out.print("Введите '1' для входа или '2' для регистрации: ");
                String authChoice = scanner.nextLine().trim();
                String cmd = authChoice.equals("2") ? "register" : "login";

                System.out.print("Логин: ");
                username = scanner.nextLine().trim();
                System.out.print("Пароль: ");
                password = scanner.nextLine().trim();

                Request authRequest = new Request(cmd, null, null, username, password);
                Response resp = udpClient.sendRequest(authRequest);

                if (resp != null && resp.status() == ResponseStatus.OK) {
                    isAuthenticated = true;
                    System.out.println(resp.message());
                } else {
                    System.err.println(resp != null ? resp.message() : "Сервер не отвечает. Попробуйте снова.");
                }
            }

            // Передаем логин и пароль в ScriptExecutor, чтобы скрипты тоже могли работать
            ScriptExecutor scriptExecutor = new ScriptExecutor(udpClient, username, password);

            System.out.println("Клиент запущен. Введите команду (help для справки):");
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) break;
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                String[] tokens = input.split("\\s+", 2);
                String commandName = tokens[0];
                String stringArgs = (tokens.length > 1) ? tokens[1] : null;

                if ("exit".equals(commandName)) {
                    System.out.println("Завершение работы клиента.");
                    break;
                }
                if ("execute_script".equals(commandName)) {
                    scriptExecutor.executeScript(stringArgs);
                    continue;
                }

                Person person = null;
                if (CommandRules.requiresCompoundData(commandName)) {
                    String[] personData = personReader.readPersonData(stringArgs);
                    try {
                        person = PersonFactory.createFromStringArray(personData);
                        stringArgs = null;
                    } catch (Exception e) {
                        System.err.println("Ошибка валидации объекта: " + e.getMessage());
                        continue;
                    }
                }

                // ВАЖНО: Передаем username и password в каждый запрос!
                Request request = new Request(commandName, stringArgs, person, username, password);
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
        if (response == null) return;
        if (response.status() == ResponseStatus.OK) {
            System.out.println(response.message());
        } else {
            System.err.println("Ошибка: " + response.message());
        }
    }
}