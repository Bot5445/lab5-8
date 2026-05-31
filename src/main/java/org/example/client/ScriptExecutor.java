package org.example.client;

import org.example.network.CommandRules;
import org.example.network.Request;
import org.example.network.Response;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Класс для выполнения скрипта на стороне клиента.
 */
public class ScriptExecutor {
    private final UDPClient udpClient;
    private static final Set<String> runningScripts = new HashSet<>();

    public ScriptExecutor(UDPClient udpClient) {
        this.udpClient = udpClient;
    }

    public void executeScript(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("Ошибка: укажите имя файла.");
            return;
        }

        filePath = filePath.trim().replace("\"", "");

        if (runningScripts.contains(filePath)) {
            System.out.println("Ошибка: Обнаружена рекурсия! Файл \"" + filePath + "\" уже выполняется.");
            return;
        }

        File scriptFile = new File(filePath);
        if (!scriptFile.exists() || !scriptFile.canRead()) {
            System.out.println("Файл не найден или нет прав на чтение: \"" + filePath + "\".");
            return;
        }

        runningScripts.add(filePath);

        try (Scanner fileScanner = new Scanner(scriptFile)) {
            System.out.println("--- Начало выполнения скрипта: " + filePath + " ---");

            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                System.out.println("> " + line);

                String[] tokens = line.split("\\s+", 2);
                String commandName = tokens[0];
                String stringArgs = (tokens.length > 1) ? tokens[1] : null;

                if ("exit".equals(commandName)) continue;

                // ИСПРАВЛЕНО: Используем общий CommandRules (DRY)
                if (CommandRules.requiresCompoundData(commandName)) {
                    System.out.println("Ошибка: Команда '" + commandName + "' требует ручного ввода и не может быть в скрипте.");
                    continue;
                }

                Request request = new Request(commandName, stringArgs, null);

                try {
                    Response response = udpClient.sendRequest(request);
                    if (response != null) {
                        System.out.println(response.getMessage());
                    } else {
                        System.out.println("Сервер не ответил при выполнении команды из скрипта.");
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка связи: " + e.getMessage());
                }
            }
            System.out.println("--- Конец выполнения скрипта: " + filePath + " ---");

        } catch (FileNotFoundException e) {
            System.out.println("Файл \"" + filePath + "\" не найден.");
        } finally {
            runningScripts.remove(filePath);
        }
    }
}