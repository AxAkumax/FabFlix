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

@WebServlet(name = "ShoppingServlet", urlPatterns = "/api/cart")
public class ShoppingServlet extends HttpServlet {
    // movieId, quantity
    private Map<String, Integer> all_items = new HashMap<>();

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // gets the contents of shopping cart
        PrintWriter out = response.getWriter();

        JsonArray jsonArray = createJsonObject();
        out.write(jsonArray.toString());
        response.setStatus(200);

        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // updates the shopping cart for incrementing/decrementing

        JsonObject jsonData = new JsonObject();
        try {
            jsonData = parseRequestToJson(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String movieId = jsonData.get("movieId").getAsString();
        String action = jsonData.get("action").getAsString();  // increment/decrement

        if (action.equals("increment")) {
            System.out.println("Incrementing movie quantity");
            increment(movieId);
        }
        else if (action.equals("decrement")) {
            System.out.println("Decrementing movie quantity");
            decrement(movieId);
        }
        else if (action.equals("delete")) {
            System.out.println("Deleting movie from cart");
            all_items.remove(movieId);
        }

        doGet(request, response);
    }


    public void increment(String movie_id) {
        // Check if the movieId is already in the cart
        if (all_items.containsKey(movie_id)) {
            // If yes, increment its quantity
            int quantity = all_items.get(movie_id);
            all_items.put(movie_id, quantity + 1);
        }
        else {
            // If not, add it to the cart with quantity 1
            all_items.put(movie_id, 1);
        }
    }

    public void decrement(String movieId) {
        // check if the movieId is already in the car
        if (all_items.containsKey(movieId)) {
            // if it is, then decrement the quantity
            int quantity = all_items.get(movieId) - 1;

            // if quantity is 0, remove from list, otherwise update quantity
            if (quantity == 0) {
                all_items.remove(movieId);
            } else {
                all_items.put(movieId, quantity);
            }
        }
        // if movieId is not in the map, do nothing for decrementing
    }

    public JsonArray createJsonObject() {
        JsonArray jsonArray = new JsonArray();

        try (Connection conn = dataSource.getConnection()) {

            for (String item : all_items.keySet()) {
                JsonObject jsonObject = new JsonObject();

                String query = "SELECT m.id AS movie_id, m.title AS movie_title " +
                        "FROM movies m " +
                        "WHERE m.id = ?;";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, item);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) { // Move cursor to the first row
                    String movieId = rs.getString("movie_id");
                    String movieTitle = rs.getString("movie_title");
                    String movie_quantity = all_items.get(item).toString();

                    jsonObject.addProperty("movieId", movieId);
                    jsonObject.addProperty("movieTitle", movieTitle);
                    jsonObject.addProperty("movieQuantity", movie_quantity);

                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();
            }

        } catch (Exception e) {
            // Handle exceptions
        }

        return jsonArray;
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