package org.example.server;

import org.example.network.*;

import org.example.network.data.CollectionManager;
import org.example.network.data.Person;
import org.example.server.commands.*;

import org.example.server.db.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private static final int PORT = 5555;

    /**
     * Точка входа серверного приложения.
     * Запускает цикл прослушивания порта.
     *
     * @param args аргументы командной строки. args[0] — порт сервера.
     */
    public static void main(String[] args) {
        DatabaseManager dbManager = new DatabaseManager();
        CollectionManager collectionManager = new CollectionManager();

        // 1. Загрузка коллекции из БД в память при старте
        List<Person> loaded = dbManager.loadAllPersons();
        collectionManager.setPersons(loaded);

        // 2. Инициализация команд (реестр)
        Map<String, ICommand> commands = new HashMap<>();
        ICommand[] cmds = new ICommand[]{
                new Register(dbManager),
                new Login(dbManager),
                new Show(collectionManager),
                new Info(collectionManager),
                new Insert(collectionManager, dbManager),
                new Update(collectionManager, dbManager),
                new Clear(collectionManager, dbManager),
                new SumOfHeight(collectionManager),
                new FilterContainsPassportID(collectionManager),
                new FilterStartsWithName(collectionManager),
                new RemoveKey(collectionManager, dbManager),
                new RemoveLower(collectionManager, dbManager),
                new RemoveLowerKey(collectionManager, dbManager),
                new ReplaceIfGreater(collectionManager, dbManager),
                new Help(commands),
        };
        for (ICommand cmd : cmds) commands.put(cmd.getName(), cmd);

        CommandProcessor processor = new CommandProcessor(commands, dbManager);

        // 3. Запуск сетевого цикла с использованием NIO DatagramChannel
        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.bind(new InetSocketAddress(PORT));
            channel.configureBlocking(false); //неблокирующий режим!

            RequestReceiver receiver = new RequestReceiver(channel, 65507);
            ResponseSender sender = new ResponseSender(channel);

            // Пулы потоков
            ExecutorService processingPool = Executors.newFixedThreadPool(10); // Для обработки
            ExecutorService sendingPool = Executors.newCachedThreadPool();      // Для отправки

            logger.info("Сервер запущен на порту {}. Ожидание запросов (NIO)...", PORT);

            while (true) {
                // Чтение в неблокирующем режиме
                RequestWrapper wrapper = receiver.receive();

                if (wrapper != null) {
                    // "Для многопоточного чтения запросов использовать создание нового потока"
                    // Создаем новый поток для обработки поступившего запроса
                    new Thread(() -> {
                        try {
                            // Обработка запроса в Fixed Thread Pool
                            processingPool.submit(() -> {
                                try {
                                    Response response = processor.process(wrapper.request());

                                    // Отправка ответа в Cached Thread Pool
                                    sendingPool.submit(() -> {
                                        try {
                                            sender.send(response, wrapper.clientAddress());
                                        } catch (Exception e) {
                                            logger.error("Ошибка отправки ответа: {}", e.getMessage());
                                        }
                                    });
                                } catch (Exception e) {
                                    logger.error("Ошибка обработки команды: {}", e.getMessage());
                                }
                            });
                        } catch (Exception e) {
                            logger.error("Ошибка при постановке задачи в пул: {}", e.getMessage());
                        }
                    }).start();
                } else {
                    Thread.sleep(10); // Небольшая пауза, чтобы не грузить CPU
                }
            }
        } catch (Exception e) {
            logger.error("Критическая ошибка сервера: {}", e.getMessage());
        }
    }
}