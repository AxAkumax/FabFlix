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

@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try (Connection conn = dataSource.getConnection()) {
            // Retrieve form parameters
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String starName = request.getParameter("starName");

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
                    query += "m.title LIKE '%" + title + "%'";
                    isFirstCondition = false;
                }
                if (year != null) {
                    if (!isFirstCondition) query += " AND ";
                    query += "m.year = '" + year + "'";
                    isFirstCondition = false;
                }
                if (director != null) {
                    if (!isFirstCondition) query += " AND ";
                    query += "m.director LIKE '%" + director + "%'";
                    isFirstCondition = false;
                }
                if (starName != null) {
                    if (!isFirstCondition) query += " AND ";
                    // Use a subquery to check if any star in the movie matches the provided name
                    query += "m.id IN (SELECT sm.movieId FROM stars_in_movies sm JOIN stars s ON sm.starId = s.id WHERE s.name LIKE '%" + starName + "%')";
                }
            }

            query += " GROUP BY m.id, m.title, m.year, m.director " +
                    "ORDER BY average_rating DESC ";

            PreparedStatement statement = conn.prepareStatement(query);
            ResultSet rs = statement.executeQuery();

            out.println("<table id=\"movie_table\" class=\"table table-hover table-dark\">");
            out.println("<thead><tr><th>Title</th><th>Year</th><th>Director</th><th>Genres</th><th>Stars</th><th>Rating</th></tr></thead>");
            out.println("<tbody id=\"movie_table_body\">");

            // Iterate through result set and write table rows
            while (rs.next()) {
                out.println("<tr>");
                // Write table cell data
                // Use rs.getString("column_name") to retrieve values from result set
                out.println("</tr>");
            }

            out.println("</tbody></table>");

            response.setStatus(200);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        } finally {
            out.close();
        }
    }

}
