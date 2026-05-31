package org.example.server;

import org.example.network.Request;

/**
 * Обертка для передачи запроса вместе с адресом клиента.
 */
public record RequestWrapper(Request request, java.net.SocketAddress clientAddress) {
}