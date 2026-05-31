package org.example.server;

import org.example.network.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * Модуль чтения запроса.
 * Принимает датаграммы и десериализует их в объекты Request.
 */
public class RequestReceiver {
    private static final Logger logger = LoggerFactory.getLogger(RequestReceiver.class);
    private final DatagramSocket socket;
    private final byte[] buffer = new byte[65507];

    public RequestReceiver(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Блокируется до получения пакета, затем десериализует его.
     * @return объект RequestWrapper, содержащий запрос и адрес отправителя
     * @throws Exception при ошибке сети или десериализации
     */
    public RequestWrapper receive() throws Exception {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet); // Блокирующий вызов

        SocketAddress clientAddress = packet.getSocketAddress();
        try (ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData(), 0, packet.getLength());
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Request request = (Request) ois.readObject();
            logger.info("Получен запрос от {}: Команда '{}'", clientAddress, request.getCommandName());
            return new RequestWrapper(request, clientAddress);
        }
    }
}