import javax.naming.InitialContext;
import javax.naming.NamingException;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String title = request.getParameter("title");
        int year = Integer.parseInt(request.getParameter("year"));
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String genre = request.getParameter("genre");

        Integer birthYear = null; // Initialize birthYear as null

        if (request.getParameter("birth_year") != null && !request.getParameter("birth_year").trim().isEmpty()) {
            birthYear = Integer.parseInt(request.getParameter("birth_year")); // Parse birth year if not null or empty
        }

        JsonObject jsonResponse = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            String storedProcCall = "{CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            CallableStatement statement = conn.prepareCall(storedProcCall);
            statement.setString(1, title);
            statement.setInt(2, year);
            statement.setString(3, director);
            statement.setString(4, starName);
            statement.setString(5, genre);
            if (birthYear != null) {
                statement.setInt(6, birthYear); // Set birth year if not null
            } else {
                statement.setNull(6, Types.INTEGER); // Set birth year as null in database if birthYear is null
            }

            statement.registerOutParameter(7, Types.VARCHAR); // Output parameter for movie ID
            statement.registerOutParameter(8, Types.VARCHAR); // Output parameter for star ID
            statement.registerOutParameter(9, Types.INTEGER); // Output parameter for genre ID

            boolean hasResults = statement.execute();

            if (!hasResults) { // Check if there are no results (i.e., no result sets)
                // Movie added successfully
                String movieId = statement.getString(7);
                String starId = statement.getString(8);
                int genreId = statement.getInt(9);

                jsonResponse.addProperty("message", "Movie added successfully!");
                jsonResponse.addProperty("movieId", movieId);
                jsonResponse.addProperty("starId", starId);
                jsonResponse.addProperty("genreId", genreId);
                out.print(jsonResponse.toString());
            } else {
                // Unexpected result, possibly an error
                jsonResponse.addProperty("error", "An error occurred while adding the movie.");
                out.print(jsonResponse.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.addProperty("error", "An error occurred while adding the movie.");
            out.print(jsonResponse.toString());
        } finally {
            out.close(); // close PrintWriter
        }
    }
}
