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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "ShoppingServlet", urlPatterns = "/api/cart")
public class ShoppingServlet extends HttpServlet {

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
        HttpSession session = request.getSession();
        Map<String, Integer> cart_items = (HashMap<String, Integer>) session.getAttribute("cart_items");

        PrintWriter out = response.getWriter();

        JsonArray jsonArray = createJsonObject(cart_items);
        out.write(jsonArray.toString());
        response.setStatus(200);

        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        // updates the shopping cart for incrementing/decrementing/deleting

        HttpSession session = request.getSession();

        // get the previous items in a Map
        Map<String, Integer> cart_items = (HashMap<String, Integer>) session.getAttribute("cart_items");

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
            increment(request, cart_items, movieId);
        }
        else if (action.equals("decrement")) {
            System.out.println("Decrementing movie quantity");
            decrement(cart_items, movieId);
        }
        else if (action.equals("delete")) {
            System.out.println("Deleting movie from cart");
            delete(cart_items, movieId);
        }

        doGet(request, response);
    }

    public void delete(Map<String, Integer> cart_items, String movieId) {
        // if cart_items is not null, synchronize the cart and remove the given movieId
        if (cart_items != null) {
            synchronized (cart_items) {
                cart_items.remove(movieId);
            }
        }
    }

    public void increment(HttpServletRequest request, Map<String, Integer> cart_items, String movie_id) {
        // if cart_items is null
            // create a new Map object, add the movieId, and put the map in session
        // if cart_items already exists
            // synchronize it
            // if movieId exists already, increment the quantity. Else put in a new key, value pair

        if (cart_items == null) {
            cart_items = new HashMap<String, Integer>();
            cart_items.put(movie_id, 1);

            HttpSession session = request.getSession();
            session.setAttribute("cart_items", cart_items);
        }
        else {
            synchronized (cart_items) {
                if (cart_items.containsKey(movie_id)) {
                    int prev_quantity = cart_items.get(movie_id);
                    cart_items.put(movie_id, prev_quantity + 1);
                }
                else {
                    cart_items.put(movie_id, 1);
                }
            }
        }
    }

    public void decrement(Map<String, Integer> cart_items, String movie_id) {
        // if cart_items exists
            // synchronize it
            // if movie exists and quantity is 1, remove it
            // if movie exists, and quantity is more than 1, decrement the count

        if (cart_items != null) {
            synchronized (cart_items) {

                if (cart_items.containsKey(movie_id)) {
                    int prev_quantity = cart_items.get(movie_id);

                    if (prev_quantity == 1) {
                        cart_items.remove(movie_id);
                    }
                    else {
                        cart_items.put(movie_id, prev_quantity - 1);
                    }
                }
            }

        }
    }

    public JsonArray createJsonObject(Map<String, Integer> cart_items) {
        JsonArray jsonArray = new JsonArray();

        try (Connection conn = dataSource.getConnection()) {

            for (String item : cart_items.keySet()) {
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
                    String movie_quantity = cart_items.get(item).toString();

                    jsonObject.addProperty("movieId", movieId);
                    jsonObject.addProperty("movieTitle", movieTitle);
                    jsonObject.addProperty("movieQuantity", movie_quantity);

                    jsonArray.add(jsonObject);
                }
                rs.close();
                statement.close();
            }
        } catch (Exception e) {
            System.out.println(e);
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