package org.example.server;

import org.example.network.*;

import org.example.network.data.CollectionManager;
import org.example.network.data.Person;
import org.example.server.commands.*;

import org.example.server.ioStorage.FileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Главный класс UDP-сервера.
 * <p>Ожидает подключения и запросы от клиента, обрабатывает полученные команды
 * и отправляет ответы клиенту.</p>
 * <p><b>Важно:</b> По заданию сервер работает в <b>однопоточном режиме</b>.
 * Каждое соединение не выделяется в отдельный поток. Сервер последовательно
 * принимает датаграмму, обрабатывает команду и отправляет ответ.</p>
 * <p>Для обмена данными на сервере используются датаграммы ({@link DatagramSocket}).</p>
 */
public class ServerMain {
    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
    private static int PORT; // Порт по умолчанию
    private static final int SOCKET_TIMEOUT = 1000; // 1 секунда для проверки консоли

    /**
     * Точка входа серверного приложения.
     * Запускает цикл прослушивания порта.
     *
     * @param args аргументы командной строки. args[0] — порт сервера.
     */
    public static void main(String[] args) {
        PORT =5555;// Integer.parseInt(args[0]);

        // 1. Инициализация хранилища и коллекции
        String filePath = System.getenv("LAB_FILE"); // По ТЗ переменная окружения
        if (filePath == null || filePath.isEmpty()) {
            logger.error("Переменная окружения LAB_FILE не задана!");
            System.err.println("Задайте переменную окружения LAB_FILE (путь к CSV файлу).");
            System.exit(1);
        }
        logger.info("Используется CSV файл: {}", filePath);

        FileStorage storage = new FileStorage(filePath);
        CollectionManager collectionManager = new CollectionManager();

        try {
            List<Person> loaded = storage.load();
            collectionManager.setPersons(loaded);
        } catch (IOException e) {
            logger.error("Не удалось загрузить коллекцию: {}", e.getMessage());
        }

        // 2. Инициализация команд (реестр)
        Map<String, ICommand> commands = new HashMap<>();
        ICommand[] cmds = new ICommand[]{
                new Show(collectionManager),
                new Info(collectionManager),
                new Insert(collectionManager),
                new Update(collectionManager),
                new Clear(collectionManager),
                new SumOfHeight(collectionManager),
                new FilterContainsPassportID(collectionManager),
                new FilterStartsWithName(collectionManager),
                new RemoveKey(collectionManager),
                new RemoveLower(collectionManager),
                new RemoveLowerKey(collectionManager),
                new ReplaceIfGreater(collectionManager),
                new Help(commands),
        };
        for (ICommand cmd : cmds) commands.put(cmd.getName(), cmd);

        CommandProcessor processor = new CommandProcessor(commands);

        // 3. Запуск сетевого цикла с использованием NIO DatagramChannel
        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(PORT));
            channel.configureBlocking(false); //неблокирующий режим!

            RequestReceiver receiver = new RequestReceiver(channel, 65507);
            ResponseSender sender = new ResponseSender(channel);
            Scanner consoleScanner = new Scanner(System.in);

            logger.info("Сервер запущен на порту {}. Ожидание запросов (NIO)...", PORT);
            System.out.println("Сервер запущен. Введите 'save' для сохранения коллекции вручную.");

            // Хук для сохранения при завершении (Ctrl+C)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Завершение работы сервера. Сохранение коллекции...");
                try {
                    storage.save(collectionManager.getAllPersons());
                } catch (Exception e) {
                    logger.error("Ошибка при сохранении: {}", e.getMessage());
                } finally {
                    try { channel.close(); } catch (Exception ignored) {}
                }
            }));

            // ГЛАВНЫЙ ОДНОПОТОЧНЫЙ ЦИКЛ (NIO)
            while (true) {
                try {
                    // Пытаемся получить запрос. Если данных нет, вернет null мгновенно.
                    RequestWrapper wrapper = receiver.receive();

                    if (wrapper == null) {
                        // Данных от клиента нет. Проверяем, не ввел ли админ команду в консоль.
                        if (System.in.available() > 0 && consoleScanner.hasNextLine()) {
                            String serverInput = consoleScanner.nextLine().trim();
                            if ("save".equalsIgnoreCase(serverInput)) {
                                logger.info("Получена команда 'save' от серверного оператора.");
                                storage.save(collectionManager.getAllPersons());
                                System.out.println("Коллекция успешно сохранена.");
                            }
                        }
                        // Небольшая пауза, чтобы не нагружать процессор на 100% в холостом цикле
                        Thread.sleep(10);
                        continue;
                    }

                    // Если пакет пришел, обрабатываем его
                    Request request = wrapper.request();
                    Response response = processor.process(request);
                    sender.send(response, wrapper.clientAddress());

                } catch (Exception e) {
                    logger.error("Ошибка при обработке запроса: {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.error("Не удалось запустить сервер: {}", e.getMessage());
        }
    }
}