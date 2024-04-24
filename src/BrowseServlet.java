import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("inside doget");
        System.out.println(request.getQueryString());

        response.setContentType("application/json"); // Response mime type
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            String genreIdParameter = request.getParameter("genreId");
            String titleStartParameter = request.getParameter("titleStart");

            if (genreIdParameter != null) {
                System.out.println("line 45 " + genreIdParameter);
                int genreId = Integer.parseInt(genreIdParameter);
                // Execute SQL query to get movies for the given genreId
                String query = "SELECT genres.id, movies.title " +
                                "FROM genres_in_movies " +
                                "JOIN movies ON genres_in_movies.movieId = movies.id " +
                                "JOIN genres ON genres_in_movies.genreId = genres.id " +
                                "WHERE genres_in_movies.genreId = ?";

                PreparedStatement statement = conn.prepareStatement(query);
                statement.setInt(1, genreId);
                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();
                System.out.println(rs);
                while (rs.next()) {
                    String title = rs.getString("title");
                    System.out.println(title);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("title", title);
                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
            else if (titleStartParameter != null) {
                System.out.println("line 77 " + titleStartParameter);
                String query;
                PreparedStatement statement;
                if (titleStartParameter.equals("*")) {
                    System.out.println("line 88 " + titleStartParameter);
                    query = "SELECT title FROM movies WHERE title REGEXP '^[^a-zA-Z0-9]'";
                    statement = conn.prepareStatement(query);
                }
                else {
                    // Execute SQL query to get movies for the given genreId
                    query = "SELECT title FROM movies WHERE LOWER(title) LIKE LOWER(?)";
                    statement = conn.prepareStatement(query);
                    statement.setString(1, titleStartParameter + "%");
                }

                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();
                System.out.println(rs);
                while (rs.next()) {
                    String title = rs.getString("title");
                    System.out.println(title);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("title", title);
                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
            else {
                String query = "SELECT id, name FROM genres ORDER BY name ASC;";

                PreparedStatement statement = conn.prepareStatement(query);
                ResultSet rs = statement.executeQuery();
                JsonArray jsonArray = new JsonArray();

                while (rs.next()) {
                    String genreId = rs.getString("id");
                    String genreName = rs.getString("name");

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("genreId", genreId);
                    jsonObject.addProperty("genreName", genreName);

                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();

                // Write JSON string to output
                out.write(jsonArray.toString());
                // Set response status to 200 (OK)
                response.setStatus(200);
            }
        }
        catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        finally {
            out.close();
        }
    }


}
