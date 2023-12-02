package ru.netology;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static ServerSocket server;
    private static boolean isServerCreated = false;
    final static List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");
    final static Map<String, Handler> handlers = new ConcurrentHashMap<>();

    public static ServerSocket getServer(int port) throws IOException {
        if (!isServerCreated) {
            isServerCreated = true;
            server = new ServerSocket(port);
        }
        return server;
    }

    public static void serverAnswer(String[] parts, BufferedOutputStream out) throws IOException {

        final var path = parts[1];
        if (!validPaths.contains(path)) {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
            return;
        }

        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return;
        }

        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    public static void addHandler(String requestType, String pathFile, Handler handler) {
        boolean flag = false;
        for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
            if (entry.getKey().equals(requestType + pathFile)) {
                flag = true;
                break;
            }
        }
        if (!flag) handlers.put(requestType + pathFile, handler);
    }

    public static Handler getHandler(Request request) {
        var parts = request.getStartingLine().split(" ");
        var findKey = parts[0]+parts[1];
        for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
            if (entry.getKey().equals(findKey)) {
                return entry.getValue();
            }
        }
        return new Handler() {
            @Override
            public void handle(Request string, BufferedOutputStream responseServer) throws IOException {
                responseServer.write((
                        "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());
                responseServer.flush();
            }
        };
    }


}
