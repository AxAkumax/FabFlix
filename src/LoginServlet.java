import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        PrintWriter out = response.getWriter();
        JsonObject responseJsonObject = new JsonObject();

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        try {
            Connection dbCon = dataSource.getConnection();
            String query = "SELECT * FROM customers WHERE email = ?";

            PreparedStatement emailCheckStatement = dbCon.prepareStatement(query);
            emailCheckStatement.setString(1, email);

            ResultSet emailResultSet = emailCheckStatement.executeQuery();


            if (emailResultSet.next()) {
                // Email exists in the database, now check if the password matches
                String storedPassword = emailResultSet.getString("password");

                if (password.equals(storedPassword)) {
                    int customerId = emailResultSet.getInt("customerId");

                    request.getSession().setAttribute("user", email);
                    request.getSession().setAttribute("customerId", customerId);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    // Password is incorrect
                    // Redirect to login.html with error parameter
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "*Incorrect password");
                }
            }
            else {
                responseJsonObject.addProperty("status", "fail");
                request.getServletContext().log("Login failed");
                responseJsonObject.addProperty("message", "*User " + email + " does not exist");
            }

            emailResultSet.close();
            emailCheckStatement.close();
            response.getWriter().write(responseJsonObject.toString());
        }
        catch (Exception e) {
            responseJsonObject.addProperty("errorMessage", e.getMessage());
            e.printStackTrace();
        }
    }
}
