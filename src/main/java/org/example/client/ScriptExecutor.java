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

    // Потокобезопасный Set для защиты от рекурсии
    private static final Set<String> runningScripts = ConcurrentHashMap.newKeySet();

    public ScriptExecutor(UDPClient udpClient) {
        this.udpClient = udpClient;
    }

    public void executeScript(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            System.out.println("Ошибка: укажите имя файла.");
            return;
        }

        filePath = filePath.trim().replace("\"", "");

        // Проверка на рекурсию
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
        System.out.println("--- Начало выполнения скрипта: " + filePath + " ---");

        try (Scanner fileScanner = new Scanner(scriptFile)) {
            int lineNumber = 0;

            while (fileScanner.hasNextLine()) {
                lineNumber++;
                String line = fileScanner.nextLine().trim();

                // Пропускаем пустые строки и комментарии
                if (line.isEmpty() || line.startsWith("#")) continue;

                System.out.println("> " + line);
                String[] tokens = line.split("\\s+", 2);
                String commandName = tokens[0];
                String stringArgs = (tokens.length > 1) ? tokens[1] : null;

                if ("exit".equals(commandName)) {
                    System.out.println("Команда 'exit' пропущена внутри скрипта.");
                    continue;
                }

                // Проверка на команды, требующие интерактивного ввода
                if (CommandRules.requiresCompoundData(commandName)) {
                    System.out.println("Пропуск: Команда '" + commandName + "' требует ручного ввода и не может быть в скрипте.");
                    continue;
                }

                Request request = new Request(commandName, stringArgs, null);

                // repeater
                int maxRetries = 5;
                boolean commandSuccess = false;

                for (int attempt = 1; attempt <= maxRetries; attempt++) {
                    try {
                        Response response = udpClient.sendRequest(request);

                        if (response != null) {
                            // Успех: сервер ответил
                            System.out.println(response.getMessage());
                            commandSuccess = true;
                            break; // Выходим из цикла попыток, переходим к следующей команде
                        } else {
                            // Неудача: таймаут (response == null)
                            if (attempt < maxRetries) {
                                System.out.println("Попытка " + attempt + " из " + maxRetries + " не удалась (таймаут). Сервер не ответил. Повторная отправка команды '" + commandName + "' через 0.5 сек...");
                                Thread.sleep(500); // Пауза, чтобы дать серверу время обработать очередь
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Сетевая ошибка при попытке " + attempt + ": " + e.getMessage());
                        if (attempt < maxRetries) {
                            try { Thread.sleep(500); } catch (InterruptedException ie) { /* игнорируем */ }
                        }
                    }
                }

                // Если все попытки провалились, ПРЕКРАЩАЕМ выполнение скрипта
                if (!commandSuccess) {
                    System.err.println("КРИТИЧЕСКАЯ ОШИБКА в строке " + lineNumber + ": Не удалось выполнить команду '" + commandName + "' после " + maxRetries + " попыток. Сервер недоступен. Выполнение скрипта прервано.");
                    break; // <--- ЭТОТ ОПЕРАТОР ОСТАНАВЛИВАЕТ ВЕСЬ СКРИПТ
                }
            }

            System.out.println("--- Конец выполнения скрипта: " + filePath + " ---");

        } catch (FileNotFoundException e) {
            System.err.println("Файл \"" + filePath + "\" не найден.");
        } finally {
            // Обязательно удаляем скрипт из списка выполняющихся при любом исходе
            runningScripts.remove(filePath);
        }
    }
}