package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
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
        ServerSocket serverSocket = null;
        try {
            serverSocket = Server.getServer(9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Server.start(serverSocket);
    }
}


