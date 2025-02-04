package co.edu.eci.arep;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class HttpServer {

    // Mapa para almacenar los endpoints REST registrados.
    private static Map<String, BiFunction<HttpRequest, HttpResponse, String>> getEndpoints = new HashMap<>();

    // Directorio donde se encuentran los archivos estáticos.
    private static String staticFolder = "";

    /**
     * Configura la carpeta de archivos estáticos.
     * Ejemplo: staticfiles("/webroot")
     * (Recuerda que en un entorno Maven, los archivos podrían ubicarse en target/classes/webroot)
     */
    public static void staticfiles(String folder) {
        staticFolder = folder;
        System.out.println("Directorio de archivos estáticos configurado: " + staticFolder);
    }

    /**
     * Registra un servicio REST para solicitudes GET.
     * Ejemplo: get("/hello", (req, resp) -> "Hello " + req.getValues("name"));
     * Se utiliza el prefijo "/App" para distinguir las rutas REST.
     */
    public static void get(String route, BiFunction<HttpRequest, HttpResponse, String> handler) {
        String fullRoute = "/App" + route;
        getEndpoints.put(fullRoute, handler);
        System.out.println("Servicio GET registrado en: " + fullRoute);
    }

    /**
     * Inicia el servidor HTTP en el puerto 8080.
     * Las solicitudes a rutas que comienzan con "/App" se tratarán como servicios REST;
     * las demás se servirán como archivos estáticos.
     */
    public static void start(String[] args) throws IOException, URISyntaxException {
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Servidor iniciado en el puerto " + port + "...");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    // Maneja cada conexión de cliente.
    private static void handleClient(Socket clientSocket) {
        try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
        ) {
            String requestLine = in.readLine();
            if (requestLine == null) {
                clientSocket.close();
                return;
            }
            System.out.println("Recibido: " + requestLine);
            String[] tokens = requestLine.split(" ");
            String method = tokens[0];
            String resource = tokens[1];

            URI uri = new URI(resource);
            String path = uri.getPath();
            String query = uri.getQuery(); // Para solicitudes GET

            if (path.startsWith("/App")) {
                // Se trata de una solicitud REST
                HttpRequest req = new HttpRequest(path, query);
                HttpResponse resp = new HttpResponse();
                BiFunction<HttpRequest, HttpResponse, String> handler = getEndpoints.get(path);
                String response;
                if (handler != null) {
                    String body = handler.apply(req, resp);
                    response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json\r\n" +
                            "\r\n" +
                            "{\"response\":\"" + body + "\"}";
                } else {
                    response = "HTTP/1.1 404 Not Found\r\n\r\n";
                }
                out.println(response);
            } else {
                // Se trata de una solicitud de archivo estático.
                serveStaticFile(path, out);
            }
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Sirve archivos estáticos ubicados en el directorio configurado.
    private static void serveStaticFile(String path, PrintWriter out) {
        if (staticFolder == null || staticFolder.isEmpty()) {
            out.println("HTTP/1.1 404 Not Found\r\n\r\n");
            return;
        }
        if (path.equals("/")) {
            path = "/index.html";
        }
        File file = new File("target/classes" + staticFolder + path);
        if (file.exists() && !file.isDirectory()) {
            try {
                byte[] fileData = Files.readAllBytes(file.toPath());
                String contentType = Files.probeContentType(file.toPath());
                String header = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + fileData.length + "\r\n" +
                        "\r\n";
                // Enviamos cabeceras y luego el contenido del archivo
                out.print(header);
                out.flush();
                OutputStream dataOut = new BufferedOutputStream(new DataOutputStream(new SocketOutputStreamWrapper(out)));
                dataOut.write(fileData, 0, fileData.length);
                dataOut.flush();
            } catch (IOException e) {
                out.println("HTTP/1.1 500 Internal Server Error\r\n\r\n");
            }
        } else {
            out.println("HTTP/1.1 404 Not Found\r\n\r\n");
        }
    }

    // SocketOutputStreamWrapper permite usar PrintWriter para escribir bytes.
    private static class SocketOutputStreamWrapper extends OutputStream {
        private final PrintWriter out;
        public SocketOutputStreamWrapper(PrintWriter out) {
            this.out = out;
        }
        @Override
        public void write(int b) throws IOException {
            out.write(b);
        }
    }
}
