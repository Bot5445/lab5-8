package org.example.server;

import org.example.network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;

/**
 * Модуль отправки ответов клиенту.
 * Сериализует Response и отправляет датаграмму по указанному адресу.
 */
public class ResponseSender {
    private static final Logger logger = LoggerFactory.getLogger(ResponseSender.class);
    private final DatagramSocket socket;

    public ResponseSender(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Сериализует и отправляет ответ клиенту.
     * @param response объект ответа
     * @param clientAddress адрес клиента
     * @throws Exception при ошибке сериализации или отправки
     */
    public void send(Response response, SocketAddress clientAddress) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
            byte[] sendData = baos.toByteArray();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress);
            socket.send(sendPacket);
            logger.info("Ответ отправлен клиенту {}: Статус {}", clientAddress, response.getStatus());
        }
    }
}