package co.edu.eci.arep;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private String path;
    private String query;
    private Map<String, String> queryParams;

    public HttpRequest(String path, String query) {
        this.path = path;
        this.query = query;
        parseQuery();
    }

    public String getPath() {
        return path;
    }

    public String getQuery() {
        return query;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setQuery(String query) {
        this.query = query;
        parseQuery();
    }

    // Parsea la query y almacena los parámetros en un mapa.
    private void parseQuery() {
        queryParams = new HashMap<>();
        if(query != null && !query.isEmpty()){
            String[] params = query.split("&");
            for(String param: params) {
                String[] keyValue = param.split("=");
                if(keyValue.length == 2){
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    // Retorna el valor del parámetro solicitado.
    public String getValues(String key) {
        return queryParams.getOrDefault(key, "");
    }
}
