import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;


import org.apache.commons.fileupload.FileItem;

import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class Main {

	public static String getSaltString() {
  String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
  StringBuilder salt = new StringBuilder();
  Random rnd = new Random();
  while (salt.length() < 10) { // length of the random string.
      int index = (int) (rnd.nextFloat() * SALTCHARS.length());
      salt.append(SALTCHARS.charAt(index));
  }
  String saltStr = salt.toString();
  return saltStr;

}
	
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
        server.createContext("/imageupload", new Upload());
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

    static class Upload implements HttpHandler {
        @Override
        public void handle(final HttpExchange t) throws IOException {
            for(Entry<String, List<String>> header : t.getRequestHeaders().entrySet()) {
                System.out.println(header.getKey() + ": " + header.getValue().get(0));
            }
            DiskFileItemFactory d = new DiskFileItemFactory();      

            try {
                ServletFileUpload up = new ServletFileUpload(d);
                List<FileItem> result = up.parseRequest(new RequestContext() {

                    @Override
                    public String getCharacterEncoding() {
                        return "UTF-8";
                    }

                    @Override
                    public int getContentLength() {
                        return 0; //tested to work with 0 as return
                    }

                    @Override
                    public String getContentType() {
                        return t.getRequestHeaders().getFirst("Content-type");
                    }

                    @Override
                    public InputStream getInputStream() throws IOException {
                        return t.getRequestBody();
                    }

                });
                t.getResponseHeaders().add("Content-type", "text/plain");
                t.sendResponseHeaders(200, 0);
                OutputStream os = t.getResponseBody();               
                for(FileItem fi : result) {
                    os.write(fi.getName().getBytes());
                    os.write("\r\n".getBytes());
                    

                    String path = "/tmp/" + getSaltString() + "/";                    
                    new File(path).mkdirs();
                    
                    File outputimage = new File(path + fi.getName());

                    outputimage.createNewFile();
                    
                    InputStream stream = fi.getInputStream();
                    FileOutputStream fos = new FileOutputStream(outputimage);
                    
                    byte[] buffer = new byte[4096];
                    while (true) {
                    	  if (stream.read(buffer) != -1) {
                    		  fos.write(buffer);
                    	  } else {
                    		  break;
                    	  }
                    }
                    fos.close();
                    
                   
            	      
            	      
              	 
                    
                    System.out.println("File-Item: " + fi.getFieldName() + " = " + fi.getName());
                }
                os.close();

            } catch (Exception e) {
                e.printStackTrace();
            }            
        }
    }
}







//import java.awt.image.BufferedImage;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//import java.net.InetSocketAddress;
//import java.nio.file.Files;
//import java.util.Random;
//
//import javax.imageio.ImageIO;
//
//import com.sun.net.httpserver.HttpExchange;
//import com.sun.net.httpserver.HttpHandler;
//import com.sun.net.httpserver.HttpServer;
//
//
//import java.util.List;
//import java.util.Map.Entry;
//
//import org.apache.commons.fileupload.FileItem;
//
//import org.apache.commons.fileupload.RequestContext;
//import org.apache.commons.fileupload.disk.DiskFileItemFactory;
//import org.apache.commons.fileupload.servlet.ServletFileUpload;
//
//
//@SuppressWarnings("restriction")
//public class Main {
//	
//	public static String getSaltString() {
//        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
//        StringBuilder salt = new StringBuilder();
//        Random rnd = new Random();
//        while (salt.length() < 10) { // length of the random string.
//            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
//            salt.append(SALTCHARS.charAt(index));
//        }
//        String saltStr = salt.toString();
//        return saltStr;
//
//    }
//
//	
//	public static String loadStaticPage(String name) {
//		String response = "";
//        
//        InputStream in = Main.class.getClassLoader().getResourceAsStream("test.html");
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//        
//        String line = null;
//        try {
//			while ( (line = reader.readLine()) != null) {
//			    // do something with the line here
//					response += line;
//			}
//			
//			return response;
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			return "404 not found";
//		}
//        
//        
//	}
//    public static void main(String[] args) throws Exception {
//        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
//        server.createContext("/test", new MyHandler());
//        server.createContext("/uploadimage", new ImageUpload());
//        server.setExecutor(null); // creates a default executor
//        server.start();
//    }
//
//    static class MyHandler implements HttpHandler {
//        @Override
//        public void handle(HttpExchange t) throws IOException {
//            String response = loadStaticPage("test.html");
//            
//            t.sendResponseHeaders(200, response.length());
//            OutputStream os = t.getResponseBody();
//            os.write(response.getBytes());
//            os.close();
//        }
//    }
//    
//    static class ImageUpload implements HttpHandler {
//
//		@Override
//		public void handle(HttpExchange t) throws IOException {
//	        
//			//BufferedImage bi = ImageIO.read(image); 
//			
//			String path = "/tmp/" + getSaltString() + "/";
//			new File(path).mkdirs();
//			
//			
//			for(Entry<String, List<String>> header : t.getRequestHeaders().entrySet()) {
//                System.out.println(header.getKey() + ": " + header.getValue().get(0));
//            }
//            DiskFileItemFactory d = new DiskFileItemFactory();      
//
//            try {
//                ServletFileUpload up = new ServletFileUpload(d);
//                List<FileItem> result = up.parseRequest(new RequestContext() {
//
//                    @Override
//                    public String getCharacterEncoding() {
//                        return "UTF-8";
//                    }
//
//                    @Override
//                    public int getContentLength() {
//                        return 0; //tested to work with 0 as return
//                    }
//
//                    @Override
//                    public String getContentType() {
//                        return t.getRequestHeaders().getFirst("Content-type");
//                    }
//
//                    @Override
//                    public InputStream getInputStream() throws IOException {
//                        return t.getRequestBody();
//                    }
//
//                });
//                t.getResponseHeaders().add("Content-type", "text/plain");
//                t.sendResponseHeaders(200, 0);
//                OutputStream os = t.getResponseBody();               
//                for(FileItem fi : result) {
//                    //os.write(fi.getName().getBytes());
//                    //os.write("\r\n".getBytes());
//                    System.out.println("File-Item: " + fi.getFieldName() + " = " + fi.getName());
//                }
//                os.close();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }            
//		}
//    		
//    }
//
//}
