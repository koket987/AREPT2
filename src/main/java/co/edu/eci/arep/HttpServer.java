package co.edu.eci.arep;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HttpServer {
    private static final int PORT = 35000;
    private static final String STATIC_FILES_PATH = "src/main/resources/www";
    private static final Set<String> users = new HashSet<>();

    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor iniciado en http://localhost:" + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleRequest(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             OutputStream out = clientSocket.getOutputStream()) {

            String requestLine = in.readLine();
            if (requestLine == null) return;
            System.out.println("Solicitud recibida: " + requestLine);

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 2) return;

            String method = requestParts[0];
            String path = requestParts[1];

            if (method.equals("GET")) {
                if (path.startsWith("/App/hello")) {
                    Map<String, String> queryParams = parseQueryParams(path);
                    String name = queryParams.getOrDefault("name", "World");
                    String response = "Hello " + name;
                    sendResponse(out, "200 OK", "text/plain", response.getBytes());
                } else if (path.startsWith("/App/rests/hello")) {
                    Map<String, String> queryParams = parseQueryParams(path);
                    String response = helloRestService(queryParams.get("name"));
                    sendResponse(out, "200 OK", "application/json", response.getBytes());
                } else {
                    serveStaticFile(out, path);
                }
            } else if (method.equals("POST") && path.startsWith("/App/rests/hello")) {
                String body = readRequestBody(in);
                String response = postRestService(body);
                sendResponse(out, "200 OK", "application/json", response.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String helloRestService(String name) {
        if (name == null || name.isEmpty()) {
            name = "Unknown";
        }
        String message = users.contains(name) ? "Hola, " + name + ". ¡Bienvenido de nuevo!" : "El usuario " + name + " no está registrado.";
        return jsonResponse(message);
    }

    private static String postRestService(String body) {
        String name = body.contains("name=") ? body.split("=")[1] : "Unknown";
        if (!users.contains(name)) {
            users.add(name);
        }
        return jsonResponse("Usuario " + name + " registrado correctamente.");
    }

    private static Map<String, String> parseQueryParams(String path) {
        Map<String, String> params = new HashMap<>();
        if (path.contains("?")) {
            String[] parts = path.split("\\?");
            if (parts.length > 1) {
                for (String param : parts[1].split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2) {
                        params.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }
        return params;
    }

    private static String readRequestBody(BufferedReader in) throws IOException {
        StringBuilder body = new StringBuilder();
        while (in.ready()) {
            body.append((char) in.read());
        }
        return body.toString();
    }

    private static void serveStaticFile(OutputStream out, String path) throws IOException {
        if (path.equals("/App/") || path.equals("/App/index.html")) {
            path = "/index.html";
        } else {
            path = path.replaceFirst("/App", "");
        }

        File file = new File(STATIC_FILES_PATH + path);
        if (file.exists() && file.isFile()) {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String contentType = Files.probeContentType(file.toPath());
            if (contentType == null) {
                contentType = "text/plain";
            }
            sendResponse(out, "200 OK", contentType, fileBytes);
        } else {
            sendResponse(out, "404 Not Found", "text/plain", "404 Not Found".getBytes());
        }
    }

    private static void sendResponse(OutputStream out, String status, String contentType, byte[] content) throws IOException {
        PrintWriter writer = new PrintWriter(out, true);
        writer.println("HTTP/1.1 " + status);
        writer.println("Content-Type: " + contentType);
        writer.println("Content-Length: " + content.length);
        writer.println();
        out.write(content);
        out.flush();
    }

    private static String jsonResponse(String message) {
        return "{\"message\": \"" + message + "\"}";
    }
}
