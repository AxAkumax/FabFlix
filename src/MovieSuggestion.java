import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/movie-suggestion")
public class MovieSuggestion extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) throws ServletException {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            System.out.println("DataSource initialized successfully");
        } catch (NamingException e) {
            e.printStackTrace();
            throw new ServletException("Cannot initialize data source", e); // Throwing ServletException for robustness
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();

        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to obtain a database connection");
            }

            // Get the query string from parameter
            String query = request.getParameter("query");

            // Return the empty JSON array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                out.write(jsonArray.toString());
                return;
            }

            // Process the query string to build a full-text search query
            String[] tokens = query.split(" ");
            String booleanQuery = "";
            for (String token : tokens) {
                booleanQuery += "+" + token + "* ";
            }

            String query_string = "SELECT " +
                    "m.id AS movie_id, " +
                    "m.title AS movie_title " +
                    "FROM movies m " +
                    "WHERE (m.title = ? " + // Exact match
                    "OR (MATCH(m.title) AGAINST(? IN BOOLEAN MODE) " + // Tokenized match
                    "AND m.title LIKE ?))"; // Partial match

            PreparedStatement statement = conn.prepareStatement(query_string);
            statement.setString(1, query);
            statement.setString(2, booleanQuery);
            statement.setString(3, "%" + query + "%");

            ResultSet rs = statement.executeQuery();

            // Populate the JSON array with movie suggestions
            while (rs.next()) {
                jsonArray.add(generateJsonObject(rs.getString("movie_id"), rs.getString("movie_title")));
            }

            response.getWriter().write(jsonArray.toString());
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("error", "Internal server error occurred");
            out.write(jsonResponse.toString());
        } finally {
            out.flush(); // Ensure data is written to the response
            out.close(); // Close PrintWriter to prevent resource leaks
        }
    }

    private static JsonObject generateJsonObject(String movieID, String movieTitle) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieTitle);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieID", movieID);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}
