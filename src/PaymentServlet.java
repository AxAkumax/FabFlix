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
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "PaymentServlet", urlPatterns = "/api/payment")
public class PaymentServlet extends HttpServlet {
    int total_amount = 0;

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject jsonData = new JsonObject();

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

        HttpSession session = request.getSession();
        Map<String, Integer> cart_items = (HashMap<String, Integer>) session.getAttribute("cart_items");
        int customerId = (int) session.getAttribute("customerId");

        try {
            jsonData = parseRequestToJson(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String action = jsonData.get("action").getAsString();

        if (action.equals("total")) {
            total_amount = Integer.parseInt(jsonData.get("total").getAsString());
        }
        else if (action.equals("process_payment")) {

            PrintWriter out = response.getWriter();
            JsonObject jsonResult = new JsonObject();

            try {
                if (checkCreditCardRecords(jsonData)) {
                    jsonResult.addProperty("result", "success");

                    insertSalesRecord(cart_items, customerId);
                    response.setStatus(200);
                }
                else {
                    jsonResult.addProperty("result", "fail");
                }
            }
            catch (SQLException e) {
                System.out.println(e);
                jsonResult.addProperty("result", "fail");
            }
            finally {
                out.write(jsonResult.toString());
            }

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

    public boolean checkCreditCardRecords(JsonObject jsonData) throws SQLException {
        String firstName = jsonData.get("firstName").getAsString();
        String lastName = jsonData.get("lastName").getAsString();
        String creditCardNumber = jsonData.get("creditCardNumber").getAsString();
        int expirationDate = jsonData.get("expirationDate").getAsInt();
        int expirationMonth = jsonData.get("expirationMonth").getAsInt();
        int expirationYear = jsonData.get("expirationYear").getAsInt();

        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT ccId, firstName, lastName, expiration " +
                    "FROM creditcards cc " +
                    "WHERE ccId = ? AND firstName = ? AND lastName = ? AND expiration = ?";

            LocalDate localDate = LocalDate.of(expirationYear, expirationMonth, expirationDate);
            Date date = Date.valueOf(localDate);

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, creditCardNumber);
            statement.setString(2, firstName);
            statement.setString(3, lastName);
            statement.setDate(4, date);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) { // Move cursor to the first row
                System.out.println(rs.getString("ccId"));
                System.out.println(rs.getString("firstName"));
                System.out.println(rs.getString("lastName"));
                System.out.println(rs.getString("expiration"));
                return true;
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }

        return false;
    }

    public void insertSalesRecord(Map<String, Integer> cart_items, int customerId) throws SQLException {

        try (Connection conn = dataSource.getConnection()) {
            // INSERT INTO sales VALUES(4,490012,'tt0386486', '2005/01/01');

            String query = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES(?,?,?,?);";

            PreparedStatement statement = conn.prepareStatement(query);

            LocalDate localDate = LocalDate.now();
            Date date = Date.valueOf(localDate);

            for (Map.Entry<String, Integer> entry : cart_items.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();

                // logged in customer's id
                statement.setInt(1, customerId);
                statement.setString(2, key);
                statement.setDate(3, date);
                statement.setInt(4, value);

                int rowsAffected = statement.executeUpdate(); // Use executeUpdate() instead of executeQuery()

                // Check if the insertion was successful
                if (rowsAffected > 0) {
                    System.out.println("Data inserted successfully.");
                } else {
                    System.out.println("Failed to insert data.");
                }
            }
        }
    }


}