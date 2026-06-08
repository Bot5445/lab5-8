package org.example.client;

import org.example.network.Request;
import org.example.network.Response;
import org.example.network.ResponseStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Класс для сетевого взаимодействия клиента с сервером по протоколу UDP.
 * Реализация основана на примерах из лекции (DatagramChannel, send, receive),
 * с добавлением неблокирующего режима и обработки таймаутов.
 */
public class UDPClient implements AutoCloseable {
    private final InetSocketAddress serverAddress;
    private final DatagramChannel channel;

    public UDPClient(String host, int port) throws Exception {
        this.serverAddress = new InetSocketAddress(host, port);
        this.channel = DatagramChannel.open();

        // По ТЗ: Неблокирующий режим
        this.channel.configureBlocking(false);

        // Жесткая привязка к IPv4 через класс Inet4Address.
        // Это предотвращает создание IPv6 сокета [0:0:0:0:0:0:0:0], из-за которого Windows теряла пакеты.
        this.channel.bind(new InetSocketAddress(Inet4Address.getByName("0.0.0.0"), 0));

        System.out.println("[DEBUG] Клиент успешно привязан к порту: " + channel.getLocalAddress());
    }

    public Response sendRequest(Request request){
       try {
            // Сериализация запроса
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(request);
            }
            byte[] sendData = baos.toByteArray();

            // Отправка датаграммы (строго как на Слайде 50 презентации)
            ByteBuffer sendBuffer = ByteBuffer.wrap(sendData);
            int sentBytes = channel.send(sendBuffer, serverAddress);
            System.out.println("[DEBUG] Отправлено " + sentBytes + " байт на " + serverAddress);

            // Ожидание ответа с таймаутом
            ByteBuffer receiveBuffer = ByteBuffer.allocate(65507);
            long timeoutMillis = 2000; // 2 секунд
            long deadline = System.currentTimeMillis() + timeoutMillis;

            while (System.currentTimeMillis() < deadline) {
                // Слайд 6: clear() сбрасывает буфер для новой операции чтения
                receiveBuffer.clear();

                // Слайд 50: receive(). В неблокирующем режиме вернет null, если данных еще нет.
                InetSocketAddress responseAddress = (InetSocketAddress) channel.receive(receiveBuffer);

                if (responseAddress != null) {
                    System.out.println("[DEBUG] Получен ответ от " + responseAddress);

                    // Слайд 6: flip() переключает буфер из режима записи в режим чтения
                    receiveBuffer.flip();
                    byte[] responseData = new byte[receiveBuffer.remaining()];
                    receiveBuffer.get(responseData);

                    try (ByteArrayInputStream bais = new ByteArrayInputStream(responseData);
                         ObjectInputStream ois = new ObjectInputStream(bais)) {
                        return (Response) ois.readObject();
                    }
                }

                // Небольшая пауза, чтобы не нагружать процессор на 100% в холостом цикле
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new Response("Клиентский поток был прерван.", ResponseStatus.ERROR);
                }
            }

    //        System.out.println("Сервер не отвечает. Проверьте подключение и попробуйте позже.");
            return new Response("Сервер не отвечает. Проверьте подключение и попробуйте позже.", ResponseStatus.ERROR);
       } catch (Exception e) {
            return new Response("Client network error: " + e.getMessage(), ResponseStatus.ERROR);
       }
    }

    @Override
    public void close() {
        try {
            if (channel != null) channel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}