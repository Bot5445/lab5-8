package org.example.client;

import org.example.network.Request;
import org.example.network.Response;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Класс для сетевого взаимодействия клиента с сервером по протоколу UDP.
 * <p>Обеспечивает сериализацию запросов и десериализацию ответов.</p>
 * <p><b>Важно:</b> Для обмена данными на клиенте используется сетевой канал
 * ({@link DatagramChannel}), работающий в <b>неблокирующем режиме</b>.</p>
 * <p>Класс реализует корректную обработку временной недоступности сервера
 * с помощью механизма таймаутов.</p>
 */
public class UDPClient implements AutoCloseable {
    private final InetSocketAddress serverAddress;
    private final DatagramChannel channel;

    /**
     * Создает экземпляр UDP-клиента и открывает канал в неблокирующем режиме.
     *
     * @param host хост сервера
     * @param port порт сервера
     * @throws Exception если не удалось открыть канал или настроить неблокирующий режим
     */
    public UDPClient(String host, int port) throws Exception {
        this.serverAddress = new InetSocketAddress(host, port);
        this.channel = DatagramChannel.open();
        // По ТЗ: Сетевые каналы должны использоваться в неблокирующем режиме
        this.channel.configureBlocking(false);
    }

    /**
     * Отправляет сериализованный запрос на сервер и ожидает ответа.
     * <p>Так как канал работает в неблокирующем режиме, метод опрашивает канал
     * на наличие входящих данных. Если сервер не отвечает в течение заданного
     * таймаута, метод информирует пользователя о недоступности сервера.</p>
     *
     * @param request объект запроса {@link Request}
     * @return объект ответа {@link Response} в случае успеха, или null, если сервер недоступен
     * @throws Exception при ошибке сериализации/сети
     */
    public Response sendRequest(Request request) throws Exception {
        // Сериализация запроса
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);
        byte[] sendData = baos.toByteArray();

        // Отправка датаграммы
        ByteBuffer sendBuffer = ByteBuffer.wrap(sendData);
        channel.send(sendBuffer, serverAddress);

        // Ожидание ответа с обработкой временной недоступности сервера
        ByteBuffer receiveBuffer = ByteBuffer.allocate(65507);
        long startTime = System.currentTimeMillis();
        long timeout = 5000; // 5 секунд таймаут

        while (System.currentTimeMillis() - startTime < timeout) {
            // В неблокирующем режиме receive() сразу возвращается.
            // Если пакет не пришел, вернет null.
            InetSocketAddress responseAddress = (InetSocketAddress) channel.receive(receiveBuffer);

            if (responseAddress != null) {
                // Пакет получен, десериализуем ответ
                receiveBuffer.flip();
                byte[] responseData = new byte[receiveBuffer.remaining()];
                receiveBuffer.get(responseData);

                try (ByteArrayInputStream bais = new ByteArrayInputStream(responseData);
                     ObjectInputStream ois = new ObjectInputStream(bais)) {
                    return (Response) ois.readObject();
                }
            }
            // Небольшая пауза, чтобы не нагружать процессор (CPU) холостым циклом
            Thread.sleep(50);
        }

        // Таймаут истек - сервер временно недоступен
        System.out.println("Сервер не отвечает. Проверьте подключение и попробуйте позже.");
        return null;
    }

    /**
     * Закрывает сетевой канал.
     */
    public void close() {
        try {
            if (channel != null) channel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}