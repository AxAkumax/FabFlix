import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
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

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/movies")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {

            String query = "SELECT " +
                    "    m.id AS movie_id," +
                    "    m.title AS movie_title," +
                    "    m.year AS movie_year," +
                    "    m.director AS movie_director," +
                    "    GROUP_CONCAT(DISTINCT g.name) AS movie_genres," +
                    "    GROUP_CONCAT(DISTINCT s.name) AS movie_stars," +
                    "    AVG(r.rating) AS average_rating " +
                    "FROM " +
                    "    movies m " +
                    "LEFT JOIN " +
                    "    genres_in_movies gm ON m.id = gm.movieId " +
                    "LEFT JOIN " +
                    "    genres g ON gm.genreId = g.id " +
                    "LEFT JOIN " +
                    "    stars_in_movies sm ON m.id = sm.movieId " +
                    "LEFT JOIN " +
                    "    stars s ON sm.starId = s.id " +
                    "LEFT JOIN " +
                    "    ratings r ON m.id = r.movieId " +
                    "GROUP BY " +
                    "    m.id, m.title, m.year, m.director " +
                    "ORDER BY " +
                    "    average_rating DESC " +
                    "LIMIT " +
                    "    20;";

            PreparedStatement statement = conn.prepareStatement(query);

            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                String movieId = rs.getString("movie_id");
                String movieTitle = rs.getString("movie_title");
                String movieYear = rs.getString("movie_year");
                String movieDirector = rs.getString("movie_director");
                String movieGenres = rs.getString("movie_genres");
                String movieStars = rs.getString("movie_stars");
                double averageRating = Math.round(rs.getDouble("average_rating") * 10.0) / 10.0;

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movieId);
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_genres", movieGenres);
                jsonObject.addProperty("movie_stars", movieStars);
                jsonObject.addProperty("average_rating", averageRating);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
