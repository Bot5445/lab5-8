package org.example.server;

import org.example.network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ResponseSender {
    private static final Logger logger = LoggerFactory.getLogger(ResponseSender.class);
    private final DatagramChannel channel;

    public ResponseSender(DatagramChannel channel) {
        this.channel = channel;
    }

    public void send(Response response, SocketAddress clientAddress) throws Exception {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(response);
            byte[] sendData = baos.toByteArray();

            System.out.println("[DEBUG] RESPONSE SIZE = " + sendData.length);

            ByteBuffer sendBuffer = ByteBuffer.wrap(sendData);
            channel.send(sendBuffer, clientAddress);

            logger.info("Ответ отправлен клиенту {}: Статус {}", clientAddress, response.getStatus());
        }
        System.out.println("[DEBUG] SEND TO = " + clientAddress);
    }
}