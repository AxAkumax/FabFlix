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

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/master");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        String starName = request.getParameter("starName");
        Integer birthYear = null; // Initialize birthYear as null

        if (request.getParameter("birth_year") != null && !request.getParameter("birth_year").trim().isEmpty()) {
            birthYear = Integer.parseInt(request.getParameter("birth_year")); // Parse birth year if not null or empty
        }

        JsonObject jsonResponse = new JsonObject();

        try (Connection conn = dataSource.getConnection()) {
            String storedProcCall = "{CALL add_star(?, ?, ?)}";
            CallableStatement statement = conn.prepareCall(storedProcCall);
            statement.setString(1, starName);
            if (birthYear != null) {
                statement.setInt(2, birthYear); // Set birth year if not null
            } else {
                statement.setNull(2, Types.INTEGER); // Set birth year as null in database if birthYear is null
            }

            statement.registerOutParameter(3, Types.VARCHAR); // Output parameter for star ID

            boolean hasResults = statement.execute();

            String starId = (String) statement.getObject(3); // Retrieve the output parameter value

            if (starId != null) {
                // Star added successfully
                jsonResponse.addProperty("message", "Star added successfully!");
                jsonResponse.addProperty("starId", starId);
            } else {
                // Unexpected result, possibly an error
                jsonResponse.addProperty("error", "An error occurred while adding the star.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.addProperty("error", "An error occurred while adding the movie.");
        } finally {
            out.println(jsonResponse.toString());
            out.close(); // close PrintWriter
        }
    }
}
