import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TodoWebApp {
    private static final List<String> todos = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        // Start CPU stress threads
        startCpuStress();

        // Create HTTP server on port 8080 (0.0.0.0 binds to all interfaces, making it accessible remotely)
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        
        server.createContext("/", new StaticHandler());
        server.createContext("/api/todos", new ApiHandler());
        
        server.setExecutor(null); // creates a default executor
        server.start();
        
        System.out.println("Web server started successfully!");
        System.out.println("Listening on http://0.0.0.0:8080");
        System.out.println("Background processing is running at 100% CPU...");
    }

    static class StaticHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                try {
                    byte[] response = Files.readAllBytes(Paths.get("index.html"));
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, response.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(response);
                    os.close();
                } catch (IOException e) {
                    String error = "Could not load index.html. Ensure it is in the same directory.";
                    exchange.sendResponseHeaders(500, error.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(error.getBytes());
                    os.close();
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class ApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if ("GET".equals(method)) {
                // Return JSON array of todos
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < todos.size(); i++) {
                    json.append("\"").append(escapeJson(todos.get(i))).append("\"");
                    if (i < todos.size() - 1) json.append(",");
                }
                json.append("]");

                byte[] response = json.toString().getBytes();
                exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
                exchange.sendResponseHeaders(200, response.length);
                OutputStream os = exchange.getResponseBody();
                os.write(response);
                os.close();

            } else if ("POST".equals(method)) {
                // Parse simple JSON: {"task":"..."}
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());
                
                int start = body.indexOf("\"task\":\"") + 8;
                int end = body.indexOf("\"", start);
                if (start >= 8 && end > start) {
                    String task = body.substring(start, end);
                    todos.add(task);
                }
                String response = "{\"status\":\"success\"}";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();

            } else if ("DELETE".equals(method)) {
                String query = exchange.getRequestURI().getQuery(); // e.g. "index=0"
                if (query != null && query.startsWith("index=")) {
                    try {
                        int index = Integer.parseInt(query.substring(6));
                        if (index >= 0 && index < todos.size()) {
                            todos.remove(index);
                        }
                    } catch (NumberFormatException e) {
                        // ignore
                    }
                }
                String response = "{\"status\":\"success\"}";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

        private String escapeJson(String text) {
            return text.replace("\\", "\\\\").replace("\"", "\\\"");
        }
    }

    private static void startCpuStress() {
        int cores = Runtime.getRuntime().availableProcessors();
        for (int i = 0; i < cores; i++) {
            Thread t = new Thread(() -> {
                while (true) {
                    // Complex math operations to prevent JIT optimization and ensure maximum CPU usage
                    double v = Math.pow(Math.random(), Math.random());
                }
            });
            t.setDaemon(true);
            t.start();
        }
    }
}
