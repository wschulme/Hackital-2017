import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;

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
    
    InputStream in = Main.class.getClassLoader().getResourceAsStream(name);
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
                for(FileItem fi : result) {
                    

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
                    
                    String s = null;

                    try {
                        
                    	// run the Unix "ps -ef" command
                    	// using the Runtime exec method:
                    	Process p = Runtime.getRuntime().exec("/Users/rohithvenkatesh/Desktop/machinelearning.command " + outputimage.getPath() + " " + path + "output.txt");

                    	BufferedReader stdInput = new BufferedReader(new 
                    			InputStreamReader(p.getInputStream()));

                    	BufferedReader stdError = new BufferedReader(new 
                    			InputStreamReader(p.getErrorStream()));

                    	// read the output from the command
                    	System.out.println("Here is the standard output of the command:\n");
                    	while ((s = stdInput.readLine()) != null) {
                    		System.out.println(s);
                    	}

                    	// read any errors from the attempted command
                    	System.out.println("Here is the standard error of the command (if any):\n");
                    	while ((s = stdError.readLine()) != null) {
                    		System.out.println(s);
                    	}

                    }
                    catch (IOException e) {
                    	System.out.println("exception happened - here's what I know: ");
                    	e.printStackTrace();
                    }
                

            	      
            	      
              	 
                    
                    System.out.println("File-Item: " + fi.getFieldName() + " = " + fi.getName());
                    
                    String response = Main.loadStaticPage("result.html");
                    
                    File output = new File(path + "output.txt");
                    String[] data = Main.parseOutput(output);
                    
                    response = response.replaceAll("@temppath@", output.getAbsolutePath());
                    System.out.println(output.getAbsolutePath());
  
                    response = response.replaceAll("@FIRST_NAME@", data[0]);
                    
                		DecimalFormat df = new DecimalFormat("##.##");
                		Double dd = Double.parseDouble(data[1])*100;
                    response = response.replaceAll("@FIRST_PERCENT@", df.format(dd).toString());
                    
                    response = response.replaceAll(Pattern.quote("@SECOND_NAME@"), data[2]);
                    response = response.replaceAll(Pattern.quote("@SECOND_PERCENT@"), data[3]);

                    response =  response.replaceAll(Pattern.quote("@THIRD_NAME@"), data[4]);
                    response =  response.replaceAll(Pattern.quote("@THIRD_PERCENT@"), data[5]);
                    
                    //t.getResponseHeaders().add("Content-type", "text/result.plain");
                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    

                    os.write(response.getBytes());
                    os.close();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }
    }
    
    static String[] parseOutput(File output) {
    	  String[] data = new String[6];
    	  
			FileReader fileReader;
			try {
				fileReader = new FileReader(output);

			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			int i = 0;
			while ((line = bufferedReader.readLine()) != null) {
				data[i] = line.substring(0, line.indexOf(' '));
				data[i + 1] = line.substring(line.indexOf(' ') + 1, line.length());
				i += 2;
				if (i == 6) {
					break;
				}
			}
			
			fileReader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    	  return data;
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
