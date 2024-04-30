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
@WebServlet(name = "GenreServlet", urlPatterns = "/api/genre")
public class GenreServlet extends HttpServlet {
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
        try (Connection conn = dataSource.getConnection()) {
            String query = "SELECT " +
                    "g.id AS genre_id, " +
                    "g.name AS genre_name " +
                    "FROM genres g " +
                    "ORDER BY g.name ASC;";
            PreparedStatement statement = conn.prepareStatement(query);

            ResultSet rs = statement.executeQuery();
            JsonObject responseObject = new JsonObject();
            JsonArray jsonArray = new JsonArray();

            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genre_id", rs.getString("genre_id"));
                jsonObject.addProperty("genre_name", rs.getString("genre_name"));
                jsonArray.add(jsonObject);
            }

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            rs.close();
            statement.close();
            response.setStatus(200);

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            out.close();
        }
    }
}