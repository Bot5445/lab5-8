package org.example.server;

import org.example.network.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class RequestReceiver {
    private static final Logger logger = LoggerFactory.getLogger(RequestReceiver.class);
    private final DatagramChannel channel;
    private final int bufferSize;

    public RequestReceiver(DatagramChannel channel, int bufferSize) {
        this.channel = channel;
        this.bufferSize = bufferSize;
    }

    public RequestWrapper receive() throws Exception {
        // В неблокирующем режиме receive() вернет null, если пакет не пришел
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        SocketAddress clientAddress = channel.receive(buffer);

        if (clientAddress == null) {
            return null; // Данных нет, возвращаем null (не блокируем поток!)
        }

        buffer.flip();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Request request = (Request) ois.readObject();
            logger.info("Получен запрос от {}: Команда '{}'", clientAddress, request.getCommandName());
            return new RequestWrapper(request, clientAddress);
        }
    }
}