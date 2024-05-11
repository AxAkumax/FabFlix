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
import java.sql.*;

@WebServlet(name = "GenreServlet", urlPatterns = "/api/genre")
public class MetadataServlet extends HttpServlet {
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
        JsonObject jsonResponse = new JsonObject();
        try (Connection conn = dataSource.getConnection()) {
            Statement statement = conn.createStatement();
            ResultSet tablesResultSet = statement.executeQuery("SHOW TABLES");
            JsonArray tablesArray = new JsonArray();
            while (tablesResultSet.next()) {
                String tableName = tablesResultSet.getString(1);
                JsonObject tableObject = new JsonObject();
                tableObject.addProperty("table_name", tableName);
                JsonArray columnsArray = new JsonArray();

                // Use a prepared statement for SHOW COLUMNS query
                String columnQuery = "SHOW COLUMNS FROM ?";
                try (PreparedStatement preparedStatement = conn.prepareStatement(columnQuery)) {
                    preparedStatement.setString(1, tableName);
                    try (ResultSet columnsResultSet = preparedStatement.executeQuery()) {
                        while (columnsResultSet.next()) {
                            JsonObject columnObject = new JsonObject();
                            columnObject.addProperty("column_name", columnsResultSet.getString("Field"));
                            columnObject.addProperty("column_type", columnsResultSet.getString("Type"));
                            columnsArray.add(columnObject);
                        }
                    }
                }

                tableObject.add("columns", columnsArray);
                tablesArray.add(tableObject);
            }
            jsonResponse.add("tables", tablesArray);
            response.setStatus(HttpServletResponse.SC_OK);
            tablesResultSet.close();
        } catch (Exception e) {
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
