package com.cloudwithvarjosh;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class App {

    // 1) Hard-coded secret (SAST hotspot)
    private static final String API_SECRET = "CVWJ_SECRET_2025";

    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            String body = brandHtml();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            byte[] out = body.getBytes();
            exchange.sendResponseHeaders(200, out.length);
            OutputStream os = exchange.getResponseBody();
            os.write(out);
            os.close();
        });

        server.createContext("/vuln", exchange -> {
            String query = exchange.getRequestURI().getQuery();
            String user = (query != null && query.startsWith("q=")) ? query.substring(2) : "guest";
            // 2) SQL concatenation vulnerability (simulated)
            String sql = "SELECT * FROM users WHERE name='" + user + "'";
            // 3) Weak MD5 fingerprint (SAST hotspot)
            String fp = fingerprint(user);
            // 4) Predictable random (small range)
            String rnd = weakRandom();
            String body = "Executed: " + sql + "\nFP: " + fp + "\nRND: " + rnd + "\nSecret used:" + API_SECRET;
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=UTF-8");
            byte[] out = body.getBytes();
            exchange.sendResponseHeaders(200, out.length);
            OutputStream os = exchange.getResponseBody();
            os.write(out);
            os.close();
        });

        System.out.println("CVWJ app running on port " + port);
        server.start();
    }

    public static String brandHtml() {
        return "<html><head><title>Cloud With VarJosh</title></head>"
                + "<body style='font-family: Arial; text-align:center; padding:30px;'>"
                + "<h1>Cloud With VarJosh</h1>"
                + "<p>Learn Cloud and DevOps with a cheeky demo.</p>"
                + "</body></html>";
    }

    // weak MD5 fingerprint (SAST should flag)
    public static String fingerprint(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] d = md.digest((input == null ? "" : input).getBytes());
            return Base64.getEncoder().encodeToString(d);
        } catch (Exception e) {
            return "";
        }
    }

    // predictable random
    public static String weakRandom() {
        SecureRandom r = new SecureRandom();
        int v = r.nextInt(100); // small range, predictable
        return "RND-" + v;
    }
}
