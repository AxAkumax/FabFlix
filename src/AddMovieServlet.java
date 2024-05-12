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
        int birthYear = Integer.parseInt(request.getParameter("birth_year"));

        JsonObject jsonResponse = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            String storedProcCall = "{CALL add_movie(?, ?, ?, ?, ?, ?)}";
            CallableStatement statement = conn.prepareCall(storedProcCall);
            statement.setString(1, title);
            statement.setInt(2, year);
            statement.setString(3, director);
            statement.setString(4, starName);
            statement.setString(5, genre);
            statement.setInt(6, birthYear);

            boolean hasResults = statement.execute();

            if (hasResults) {
                // Movie exists, send response back to client
                ResultSet rs = statement.getResultSet();
                if (rs != null && rs.next()) {
                    String message = rs.getString("message");
                    jsonResponse.addProperty("message", message);
                    out.print(jsonResponse.toString());
                }
                rs.close();
            } else {
                // Movie added successfully
                jsonResponse.addProperty("message", "Movie added successfully!");
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
