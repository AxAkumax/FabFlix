import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonParser;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    int total_amount = 0;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject jsonData = new JsonObject();

//        try {
//            jsonData = parseRequestToJson(request);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        String action = jsonData.get("action").getAsString();
        System.out.println(request);
        String action = request.getParameter("action");

        if (action.equals("get_cart_total")) {
            JsonObject totalDataJson = new JsonObject();

            PrintWriter out = response.getWriter();
            totalDataJson.addProperty("total_amount", total_amount);
            out.write(totalDataJson.toString());
            response.setStatus(200);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject jsonData = new JsonObject();

        try {
            jsonData = parseRequestToJson(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String action = jsonData.get("action").getAsString();

        if (action.equals("total")) {
            total_amount = Integer.parseInt(jsonData.get("total").getAsString());
        }
    }

    // Utility function to parse request body to JSON
    public static JsonObject parseRequestToJson(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Parse the request body to JSON
        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }
}