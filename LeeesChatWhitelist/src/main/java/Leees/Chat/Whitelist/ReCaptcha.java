package Leees.Chat.Whitelist;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class ReCaptcha
{
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(80), 0);
        server.createContext("/", new MyHandler());
        server.createContext("/submit", new MyHandler2());
        server.setExecutor(null);
        server.start();
    }

    static String jsonText;

    static class MyHandler
            implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String response = "<html>\r\n  <head>\r\n    <title>6b6t - AntiBot</title>\r\n    <script type=\"text/javascript\">\r\n      var onloadCallback = function() {\r\n        grecaptcha.render('html_element', {\r\n          'sitekey' : 'Put Your Site Key Here'\r\n        });\r\n      };\r\n    </script>\r\n  </head>\r\n  <body>\r\n    <form action=\"/submit\">\r\n      <div  align=\"center\">      <label for=\"username\"><b>Username</b></label>\r\n      <input type=\"text\" placeholder=\"Enter Username\" name=\"username\" required>\r\n      <div id=\"html_element\"></div>\r\n      <br>\r\n      <input type=\"submit\" value=\"Submit\">\r\n    </form>\r\n    <script src=\"https://www.google.com/recaptcha/api.js?onload=onloadCallback&render=explicit\"\r\n        async defer>\r\n    </script>\r\n      </div>  </body>\r\n</html>";


























            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static Map<String, String> xd(String a) {
        HashMap<String, String> map = new HashMap<String, String>();
        for (String s : a.split("&")) {
            map.put(s.split("=")[0], s.split("=")[1]);
        }
        return map;
    }

    static class MyHandler2
            implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            boolean captchaValid = false;
            String response = null;
            Map<String, String> map = ReCaptcha.xd(t.getRequestURI().getRawQuery());
            try {
                if (ReCaptcha.isCaptchaValid("Put Your Secret Key Here", (String)map.get("g-recaptcha-response"))) {
                    captchaValid = true;
                }
                if (captchaValid)
                { response = "Valid you may relog now."; }
                else { response = "Captcha Failed"; }
                if (captchaValid) {
                    String name = (String)map.get("username");
                    Main.verified.add(name);
                    Main.save();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    public static boolean isCaptchaValid(String secretKey, String response) {
        try {
            String url = "https://www.google.com/recaptcha/api/siteverify?secret=" + secretKey + "&response=" + response;



            InputStream res = (new URL(url)).openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(res, Charset.forName("UTF-8")));
            jsonText = "";
            rd.lines().forEach(s -> jsonText += s);
            System.out.print(jsonText);
            res.close();

            JsonElement json = (new JsonParser()).parse(jsonText);
            System.out.print(json);
            return json.getAsJsonObject().get("success").getAsBoolean();
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }
}
