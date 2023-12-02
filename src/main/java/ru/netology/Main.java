package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(64);
        Server.addHandler("GET", "/index.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream responseServer) {
                var parts = request.getStartingLine().split(" ");
                var filePath = Path.of(".", "public", parts[1]);
                try {
                    var mimeType = Files.probeContentType(filePath);
                    responseServer.write((
                            "HTTP/1.1 200 OK\r\n" +
                                    "Content-Type: " + mimeType + "\r\n" +
                                    "Content-Length: " + Files.size(filePath) + "\r\n" +
                                    "Connection: close\r\n" +
                                    "\r\n"
                    ).getBytes());
                    Files.copy(filePath, responseServer);
                    responseServer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        try (ServerSocket serverSocket = Server.getServer(9999)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executorService.execute(new Thread(() -> {
                    try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                         final var out = new BufferedOutputStream(socket.getOutputStream())) {
                        var requestLine = in.readLine();
                        final var parts = requestLine.split(" ");

                        Request request = new Request(requestLine);
                        requestLine = in.readLine();
                        while (requestLine != null) {
                            if( requestLine.isEmpty()) break;
                            request.addHeader(requestLine);
                            requestLine = in.readLine();
                        }

                        if (parts.length != 3) {
                            socket.close();
                        }
//                        Server.serverAnswer(parts, out);
                        Handler handler = Server.getHandler(request);
                        handler.handle(request, out);
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


