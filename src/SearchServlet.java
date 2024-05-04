
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
import java.util.ArrayList;
import java.util.List;

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

            String sortAttribute = request.getParameter("sortAttribute"); // Get sort attribute
            int currentPage = Integer.parseInt(request.getParameter("page"));
            int recordsPerPage = Integer.parseInt(request.getParameter("recordsPerPage"));
            // Calculate offset for pagination
            int offset = (currentPage - 1) * recordsPerPage;

            //BUILD QUERY
            String query = "SELECT " +
                    "m.id AS movie_id, " +
                    "m.title AS movie_title, " +
                    "m.year AS movie_year, " +
                    "m.director AS movie_director, " +

                    "(SELECT GROUP_CONCAT(DISTINCT CONCAT(s.id, ';', s.name, ';', total_movies) ORDER BY total_movies DESC, s.name ASC SEPARATOR ';') " +
                    " FROM (SELECT starId, COUNT(*) AS total_movies " +
                    "       FROM stars_in_movies " +
                    "       GROUP BY starId " +
                    "       ) AS star_movies " +
                    " JOIN stars s ON star_movies.starId = s.id " +
                    " JOIN stars_in_movies sm ON s.id = sm.starId " +
                    " WHERE sm.movieId = m.id " +
                    ") AS star_ids_and_names, " +

                    "(SELECT GROUP_CONCAT(DISTINCT CONCAT(g.id, ';', g.name) SEPARATOR ';') " +
                    "FROM genres_in_movies gm JOIN genres g ON gm.genreId = g.id " +
                    "WHERE gm.movieId = m.id) AS movie_id_genres, " +
                    "AVG(r.rating) AS average_rating " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON m.id = r.movieId " +
                    "LEFT JOIN stars_in_movies sm ON m.id = sm.movieId " +
                    "LEFT JOIN stars s ON sm.starId = s.id ";

// Condition for WHERE clause
            String conditions = "";
            List<String> parameters = new ArrayList<>();

            if (title != null) {
                conditions += "LOWER(m.title) LIKE ? ";
                parameters.add("%" + title.toLowerCase() + "%");
            }
            if (year != null) {
                if (!conditions.isEmpty()) conditions += "AND ";
                conditions += "m.year = ? ";
                parameters.add(year);
            }
            if (director != null) {
                if (!conditions.isEmpty()) conditions += "AND ";
                conditions += "LOWER(m.director) LIKE ? ";
                parameters.add("%" + director.toLowerCase() + "%");
            }
            if (starName != null) {
                if (!conditions.isEmpty()) conditions += "AND ";
                conditions += "s.name LIKE ? ";
                parameters.add("%" + starName.toLowerCase() + "%");
            }

            if (!conditions.isEmpty()) {
                query += "WHERE " + conditions;
            }

// Group by and order by
            query += "GROUP BY m.id, m.title, m.year, m.director " +
                    "ORDER BY " + sortAttribute +
                    " LIMIT ? OFFSET ?";

            PreparedStatement statement = conn.prepareStatement(query);

// Set parameters
            int parameterIndex = 1;
            for (String param : parameters) {
                statement.setString(parameterIndex++, param);
            }

// Set limit and offset parameters for pagination
            statement.setInt(parameterIndex++, recordsPerPage);
            statement.setInt(parameterIndex, (currentPage - 1) * recordsPerPage);

            ResultSet rs = statement.executeQuery();

            JsonArray movieArray = new JsonArray();

            // Iterate through result set and add movie objects to the array
            while (rs.next()) {
                JsonObject movieObject = new JsonObject();
                movieObject.addProperty("id", rs.getString("movie_id"));
                movieObject.addProperty("title", rs.getString("movie_title"));
                movieObject.addProperty("year", rs.getString("movie_year"));
                movieObject.addProperty("director", rs.getString("movie_director"));

                movieObject.addProperty("genres", rs.getString("movie_id_genres"));
                movieObject.addProperty("stars", rs.getString("star_ids_and_names"));

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