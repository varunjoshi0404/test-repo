package com.cloudwithvarjosh;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;

public class App {

    // 1) Hard-coded secret (SAST hotspot)
    private static final String API_SECRET = "CVWJ_SECRET_2025";

    // 2) Another intentional hardcoded credential (from old demo)
    private static final String ADMIN_PASSWORD = "admin123";

    public static void main(String[] args) throws Exception {

        // 3) System.out (code smell)
        System.out.println("Starting CVWJ DevSecOps Demo...");

        // 4) Scanner resource leak + wrong string comparison
        Scanner sc = new Scanner(System.in); // resource never closed (intentional)
        System.out.print("Enter username (local simulation): ");
        String userInput = sc.nextLine();

        // WRONG comparison using '==' instead of equals()
        if (userInput == "admin") { // Sonar will flag this as a Bug
            System.out.println("Access granted to admin user.");
        } else {
            System.out.println("Access denied.");
        }

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

            // 5) SQL concatenation vulnerability (simulated)
            String sql = "SELECT * FROM users WHERE name='" + user + "'";

            // 6) Weak MD5 fingerprint (SAST hotspot)
            String fp = fingerprint(user);

            // 7) Predictable random
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
                + "<p>Your one-stop lab for hands-on Cloud and DevOps learning!</p>"
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
        int v = r.nextInt(100); // small, predictable
        return "RND-" + v;
    }
}
