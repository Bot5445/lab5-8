package org.example.client;

import org.example.network.CommandRules;
import org.example.network.Request;
import org.example.network.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Класс для выполнения скрипта на стороне клиента.
 * Включает механизм повторных попыток (retry) и аварийную остановку при сбое.
 */
public class ScriptExecutor {
    private final UDPClient udpClient;
    private final String username;
    private final String password;
    private static final Set<String> runningScripts = ConcurrentHashMap.newKeySet(); // Потокобезопасный Set для защиты от рекурсии

    public ScriptExecutor(UDPClient udpClient, String username, String password) {
        this.udpClient = udpClient;
        this.username = username;
        this.password = password;
    }

    public void executeScript(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("Ошибка: укажите имя файла.");
            return;
        }
        filePath = filePath.trim().replace("\"", "");
        if (runningScripts.contains(filePath)) {
            System.out.println("Ошибка: Обнаружена рекурсия!");
            return;
        }
        File scriptFile = new File(filePath);
        if (!scriptFile.exists()) {
            System.out.println("Файл не найден: \"" + filePath + "\".");
            return;
        }

        runningScripts.add(filePath);
        System.out.println("--- Начало выполнения скрипта: " + filePath + " ---");

        try (Scanner fileScanner = new Scanner(scriptFile)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                System.out.println("> " + line);

                String[] tokens = line.split("\\s+", 2);
                String commandName = tokens[0];
                String stringArgs = (tokens.length > 1) ? tokens[1] : null;

                if ("exit".equals(commandName)) continue;
                if (CommandRules.requiresCompoundData(commandName)) {
                    System.out.println("Пропуск: Команда '" + commandName + "' требует ручного ввода.");
                    continue;
                }

                // ВАЖНО: Вставляем username и password в запрос из скрипта
                Request request = new Request(commandName, stringArgs, null, username, password);

                Response response = udpClient.sendRequest(request);
                if (response != null) {
                    System.out.println(response.message());
                } else {
                    System.err.println("Сервер не ответил. Прерывание скрипта.");
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Файл не найден.");
        } finally {
            runningScripts.remove(filePath);
        }
    }
}