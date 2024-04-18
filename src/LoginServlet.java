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
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * A servlet that takes input from a html <form> and talks to MySQL moviedbexample,
 * generates output as a html <table>
 */

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Content type and output stream remain the same

        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Login</title></head>");
        out.println("<body><h1>Login</h1>");

        // Display the login form
        out.println("<form action='/api/login' method='post'>");
        out.println("Email: <input type='text' name='email'><br>");
        out.println("Password: <input type='password' name='password'><br>");
        out.println("<input type='submit' value='Login'>");
        out.println("</form>");

        out.println("</body></html>");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            Connection dbCon = dataSource.getConnection();
            Statement statement = dbCon.createStatement();

            String query = String.format("SELECT * FROM customers WHERE email='%s' AND password='%s'", email, password);
            ResultSet rs = statement.executeQuery(query);

            if (rs.next()) {
                // Authentication successful
                // Use request.getContextPath() to prepend the context path dynamically
                response.sendRedirect(request.getContextPath() + "/webcontent/index.html");
            } else {
                // Authentication failed
                response.sendRedirect(request.getContextPath() + "/webcontent/form.html");
                //response.sendRedirect("login.html");
            }

            rs.close();
            statement.close();
            dbCon.close();

        } catch (Exception e) {
            // Error handling
            e.printStackTrace();
        }
    }
}