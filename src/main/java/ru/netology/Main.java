package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(64);

        try (ServerSocket serverSocket = Server.getServer(9999)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.execute(new Thread(() -> {
                    try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         final var out = new BufferedOutputStream(socket.getOutputStream())) {
                        final var requestLine = in.readLine();
                        final var parts = requestLine.split(" ");

                        if (parts.length != 3) {
                            socket.close();
                        }
                        Server.serverAnswer(parts, out);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


