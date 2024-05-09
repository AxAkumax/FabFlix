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

@WebServlet(name = "ConfirmationServlet", urlPatterns = "/api/confirm")
public class ConfirmationServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Map<String, Integer> cart_items = (HashMap<String, Integer>) session.getAttribute("cart_items");
        int customerId = (int) session.getAttribute("customerId");



        JsonArray sales_json = getSalesInfo(cart_items, customerId);

        PrintWriter out = response.getWriter();
        out.write(sales_json.toString());
        response.setStatus(200);
        out.close();
    }

    public JsonArray getSalesInfo(Map<String, Integer> cartItems, int customerId) {
        JsonArray jsonArray = new JsonArray();

        try (Connection conn = dataSource.getConnection()) {
            String salesQuery = "SELECT s.salesId AS sale_id, m.id AS movie_id, m.title AS movie_title, s.quantity " +
                    "FROM sales s " +
                    "INNER JOIN movies m ON s.movieId = m.id " +
                    "WHERE s.customerId = ? AND s.movieId = ? AND s.quantity = ? AND DATE(s.saleDate) = ?;";


            PreparedStatement salesStatement = conn.prepareStatement(salesQuery);

            for (Map.Entry<String, Integer> entry : cartItems.entrySet()) {
                salesStatement.setInt(1, customerId);
                salesStatement.setString(2, entry.getKey());
                salesStatement.setInt(3, entry.getValue());
                salesStatement.setDate(4, java.sql.Date.valueOf(LocalDate.now()));


                ResultSet salesRs = salesStatement.executeQuery();

                if (salesRs.next()) { // Move cursor to the first row
                    JsonObject jsonObject = new JsonObject();

                    String saleId = salesRs.getString("sale_id");
                    String movieId = salesRs.getString("movie_id");
                    String movieTitle = salesRs.getString("movie_title");
                    String movieQuantity = salesRs.getString("quantity");

                    jsonObject.addProperty("saleId", saleId);
                    jsonObject.addProperty("movieId", movieId);
                    jsonObject.addProperty("movieTitle", movieTitle);
                    jsonObject.addProperty("movieQuantity", movieQuantity);

                    jsonArray.add(jsonObject);
                }
                salesRs.close();
            }
            salesStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }
}
