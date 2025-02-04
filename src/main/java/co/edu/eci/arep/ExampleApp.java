package co.edu.eci.arep;


public class ExampleApp {
    public static void main(String[] args) throws Exception {
        // Configura la carpeta donde se encuentran los archivos estáticos.
        // En este ejemplo, se asume que los archivos estáticos se encuentran en:
        // target/classes/webroot
        HttpServer.staticfiles("/webroot");

        // Registra un servicio REST para GET que responde en "/App/hello"
        HttpServer.get("/hello", (req, resp) -> "Hello " + req.getValues("name"));

        // Registra otro servicio REST para GET que responde en "/App/pi"
        HttpServer.get("/pi", (req, resp) -> String.valueOf(Math.PI));

        // Inicia el servidor
        // Las solicitudes que comiencen con "/App" se gestionarán como servicios REST,
        // mientras que las demás se considerarán peticiones para archivos estáticos.
        HttpServer.start(new String[0]);
    }
}
