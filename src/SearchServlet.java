
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
import java.sql.SQLException;

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            System.out.println("DataSource initialized successfully");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject jsonResponse = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            if (conn != null) {
                System.out.println("Connected to database successfully");
            }

            // Retrieve form parameters
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String starName = request.getParameter("starName");

            //BUILD QUERY
            String query = "SELECT " +
                    "m.id AS movie_id, " +
                    "m.title AS movie_title, " +
                    "m.year AS movie_year, " +
                    "m.director AS movie_director, " +
                    "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ';', s.name) SEPARATOR ';') " +
                    "FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id " +
                    "WHERE sm.movieId = m.id) AS star_ids_and_names, " +
                    "(SELECT GROUP_CONCAT(DISTINCT g.name) " +
                    "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id " +
                    "WHERE gm.movieId = m.id) AS movie_genres, " +
                    "AVG(r.rating) AS average_rating " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId ";

            if (title != null || year != null || director != null || starName != null) {
                query += "WHERE ";
                boolean isFirstCondition = true;
                if (title != null) {
                    query += "LOWER(m.title) LIKE ?";
                    isFirstCondition = false;
                }
                if (year != null) {
                    if (!isFirstCondition) query += " AND ";
                    query += "m.year = ?";
                    isFirstCondition = false;
                }
                if (director != null) {
                    if (!isFirstCondition) query += " AND ";
                    query += "LOWER(m.director) LIKE ?";
                    isFirstCondition = false;
                }
                if (starName != null) {
                    if (!isFirstCondition) query += " AND ";
                    // Use a subquery to check if any star in the movie matches the provided name
                    query += "m.id IN (SELECT sm.movieId FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id WHERE LOWER(s.name) LIKE ?)";
                }
            }

            query += " GROUP BY m.id, m.title, m.year, m.director " +
                    "ORDER BY average_rating DESC ";

            //case-insensitive and substring matching
            assert conn != null;

            PreparedStatement statement = conn.prepareStatement(query);
            int parameterIndex = 1;
            if (title != null) {
                statement.setString(parameterIndex++, "%" + title.toLowerCase() + "%");
            }
            if (year != null) {
                statement.setString(parameterIndex++, year);
            }
            if (director != null) {
                statement.setString(parameterIndex++, "%" + director.toLowerCase() + "%");
            }
            if (starName != null) {
                statement.setString(parameterIndex, "%" + starName.toLowerCase() + "%");
            }

            ResultSet rs = statement.executeQuery();

            JsonArray movieArray = new JsonArray();

            // Iterate through result set and add movie objects to the array
            while (rs.next()) {
                JsonObject movieObject = new JsonObject();
                movieObject.addProperty("title", rs.getString("movie_title"));
                movieObject.addProperty("year", rs.getString("movie_year"));
                movieObject.addProperty("director", rs.getString("movie_director"));
                movieObject.addProperty("genres", rs.getString("movie_genres"));
                movieObject.addProperty("stars", rs.getString("star_ids_and_names"));
                //movieObject.addProperty("rating", rs.getDouble("average_rating"));
                double averageRating = Math.round(rs.getDouble("average_rating") * 10.0) / 10.0;
                movieObject.addProperty("rating", averageRating);

                movieArray.add(movieObject);
            }


            jsonResponse.add("movies", movieArray);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            jsonResponse.addProperty("error", "Internal server error occurred");
        } finally {
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
        }
    }
}
