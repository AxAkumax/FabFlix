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
import org.jasypt.util.password.StrongPasswordEncryptor;
import java.sql.ResultSet;

@WebServlet(name = "LoginServlet", urlPatterns = "/user/api/login")
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
        System.out.println("inside /user/api/login doPost");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        JsonObject responseJsonObject = new JsonObject();
        PrintWriter out = response.getWriter();

        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "reCAPTCHA verification failed");
            out.write(responseJsonObject.toString());
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        try {
            Connection dbCon = dataSource.getConnection();
            String query = "SELECT * FROM customers WHERE email = ?";

            PreparedStatement emailCheckStatement = dbCon.prepareStatement(query);
            emailCheckStatement.setString(1, email);

            ResultSet emailResultSet = emailCheckStatement.executeQuery();


            if (emailResultSet.next()) {
                // Email exists in the database, now check if the password matches
                String storedPassword = emailResultSet.getString("password");

//
//                if (password.equals(storedPassword)) {
//                    int customerId = emailResultSet.getInt("customerId");
//
//                    request.getSession().setAttribute("user", email);
//                    request.getSession().setAttribute("customerId", customerId);
//                    responseJsonObject.addProperty("status", "success");
//                    responseJsonObject.addProperty("message", "success");
//                } else {
//                    // Password is incorrect
//                    // Redirect to login.html with error parameter
//                    responseJsonObject.addProperty("status", "fail");
//                    request.getServletContext().log("Login failed");
//                    responseJsonObject.addProperty("message", "*Incorrect password");
//                }
                boolean exists = new StrongPasswordEncryptor().checkPassword(password, storedPassword);
                if (exists) {
                    int customerId = emailResultSet.getInt("customerId");

                    request.getSession().setAttribute("user", email);
                    request.getSession().setAttribute("customerId", customerId);
                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                }
                else{
                    System.out.println("here");
                    // Password is incorrect
                    // Redirect to login.html with error parameter
                    responseJsonObject.addProperty("status", "fail");
                    request.getServletContext().log("Login failed");
                    responseJsonObject.addProperty("message", "*Incorrect password");
                }

            }
            else {
                System.out.println("here2");
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
