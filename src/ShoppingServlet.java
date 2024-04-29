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

@WebServlet(name = "ShoppingServlet", urlPatterns = "/api/cart")
public class ShoppingServlet extends HttpServlet {
    ArrayList<String> all_items = new ArrayList<>();
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonArray jsonArray = new JsonArray();
        PrintWriter out = response.getWriter();
        System.out.println("line 42 inside get");
        try (Connection conn = dataSource.getConnection()) {
            for (String item : all_items) {
                JsonObject jsonObject = new JsonObject();

                String query = "SELECT m.id AS movie_id, m.title AS movie_title " +
                        "FROM movies m " +
                        "WHERE m.id = ?;";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setString(1, item);
                ResultSet rs = statement.executeQuery();
                System.out.println(rs);

                if (rs.next()) { // Move cursor to the first row
                    String movieId = rs.getString("movie_id");
                    String movieTitle = rs.getString("movie_title");

                    jsonObject.addProperty("movie_id", movieId);
                    jsonObject.addProperty("movie_title", movieTitle);

                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();
            }

            System.out.println(jsonArray);
            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Retrieve the JSON data sent from the client-side
        JsonObject jsonData = new JsonObject();
        try {
            jsonData = parseRequestToJson(request);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Extract the movie ID from the JSON data
        String movieId = jsonData.get("movieId").getAsString();
        all_items.add(movieId);

        // Perform any necessary operations with the movie ID (e.g., add it to the shopping cart)
        // For demonstration purposes, let's just print the movie ID
        System.out.println("Movie ID added to cart: " + movieId);

        // Construct the response JSON object
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", "success");
        responseObject.addProperty("message", "Movie successfully added to cart");

        // Set the response content type and write the response JSON object
        response.setContentType("application/json");
        response.getWriter().write(responseObject.toString());
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