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
            String search = request.getParameter("search");
            String sortAttribute = request.getParameter("sortAttribute"); // Get sort attribute
            int currentPage = Integer.parseInt(request.getParameter("page"));
            int recordsPerPage = Integer.parseInt(request.getParameter("recordsPerPage"));

            // Calculate offset for pagination
            int offset = (currentPage - 1) * recordsPerPage;

            // Prepare the query tokens for BOOLEAN MODE
//            String[] tokens = search.split("\\s+");
//            StringBuilder booleanQuery = new StringBuilder();
//            for (String token : tokens) {
//                if (booleanQuery.length() > 0) {
//                    booleanQuery.append(" ");
//                }
//                booleanQuery.append("+").append(token).append("*");
//            }
//            System.out.println(booleanQuery.toString());
            // Process the query string to build a full-text search query
            String[] tokens = search.split(" ");
            String booleanQuery = "";
            for (String token : tokens) {
                booleanQuery += "+" + token + "* ";
            }

            // Build the SQL query
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
                    "LEFT JOIN stars s ON sm.starId = s.id " +

                    "WHERE (m.title = ? " + // Exact match
                    "OR (MATCH(m.title) AGAINST(? IN BOOLEAN MODE) " + // Tokenized match
                    "AND m.title LIKE ?)) "+

                    "GROUP BY m.id, m.title, m.year, m.director " +
                    "ORDER BY ? "+
                    "LIMIT ? OFFSET ?";

            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, search);
            statement.setString(2, booleanQuery);
            statement.setString(3, "%" + search + "%");
            statement.setString(4, sortAttribute);
            statement.setInt(5, recordsPerPage + 1); // Fetch one extra record to check for the next page
            statement.setInt(6, offset);

            ResultSet rs = statement.executeQuery();

            JsonArray movieArray = new JsonArray();
            boolean hasNextPage = false; // Flag to track if there are more pages available

            // Iterate through result set and add movie objects to the array
            while (rs.next()) {
                if (movieArray.size() < recordsPerPage) {
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
                } else {
                    hasNextPage = true; // Set the flag indicating more pages are available
                    break; // Exit the loop as we fetched more records than the recordsPerPage
                }
            }

            jsonResponse.add("movies", movieArray);
            jsonResponse.addProperty("hasNextPage", hasNextPage);
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
