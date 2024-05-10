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

@WebServlet(name = "GenreServlet", urlPatterns = "/api/metadata")
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
            JsonObject tables = new JsonObject();
            JsonArray tablesArray = new JsonArray();
            while (tablesResultSet.next()) {
                String tableName = tablesResultSet.getString(1);
                tables.addProperty("table_name", tableName);
                String columnQuery = "SELECT COLUMN_NAME, DATA_TYPE " +
                        "FROM INFORMATION_SCHEMA.COLUMNS " +
                        "WHERE TABLE_NAME = ?";
                PreparedStatement preparedStatement = conn.prepareStatement(columnQuery);
                preparedStatement.setString(1, tableName);

                ResultSet columnsResultSet = preparedStatement.executeQuery();
                JsonArray tableArray = new JsonArray();
                while (columnsResultSet.next()) {
                    JsonObject column = new JsonObject();
                    String columnName = columnsResultSet.getString(1);
                    String columnType = columnsResultSet.getString(2);
                    column.addProperty("column_name", columnName);
                    column.addProperty("column_type", columnType);
                    tableArray.add(column);
                }
                tablesArray.add(tableArray);
                // Show Columns from Each Table
            }
            jsonResponse.add("tables", tables);
            jsonResponse.add("columns", tablesArray);
            response.setStatus(HttpServletResponse.SC_OK);
            tablesResultSet.close();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
            jsonResponse.addProperty("error", "Internal server error occurred");
        }
        finally {
            out.print(jsonResponse.toString());
            out.flush();
            out.close();
        }
    }
    }