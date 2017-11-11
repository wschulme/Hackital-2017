import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Main {
	
	public static String loadStaticPage(String name) {
		String response = "";
        
        InputStream in = Main.class.getClassLoader().getResourceAsStream("test.html");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        
        String line = null;
        try {
			while ( (line = reader.readLine()) != null) {
			    // do something with the line here
					response += line;
			}
			
			return response;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return "404 not found";
		}
        
        
	}
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = loadStaticPage("test.html");
            
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
