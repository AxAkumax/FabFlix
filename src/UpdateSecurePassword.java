import java.sql.*;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePassword {

    /*
     *
     * This program updates your existing moviedb customers table to change the
     * plain text passwords to encrypted passwords.
     *
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     *
     */
    public static void main(String[] args) throws Exception {

        String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String loginUrl = "jdbc:mysql://172.31.32.61:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);

        update_employee_password(connection);


        Statement statement = connection.createStatement();

        // change the customers table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE customers MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering customers table schema completed, " + alterResult + " rows affected");

        // get the ID and password for each customer
        String query = "SELECT customerId, password from customers";

        ResultSet rs = statement.executeQuery(query);

        // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
        //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {

            System.out.println("encrypting password (this might take a while)");
            while (rs.next()) {
                // get the ID and plain text password from current table
                int id = rs.getInt("customerId");
                String password = rs.getString("password");

                // encrypt the password using StrongPasswordEncryptor
                String encryptedPassword = passwordEncryptor.encryptPassword(password);

                // generate the update query
                String updateQuery = "UPDATE customers SET password=? WHERE customerId=?;";
                preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setString(1, encryptedPassword);
                preparedStatement.setInt(2, id);
                updateQueryList.add(updateQuery);
                preparedStatement.executeUpdate();
            }
            rs.close();

//            // execute the update queries to update the password
//            System.out.println("updating password");
//            int count = 0;
//            for (String updateQuery : updateQueryList) {
//                System.out.println(updateQuery);
//                int updateResult = preparedStatement.executeUpdate(updateQuery);
//                count += updateResult;
//            }
//            System.out.println("updating password completed, " + count + " rows affected");

            statement.close();
            connection.close();

            System.out.println("finished");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    private static void update_employee_password(Connection connection) throws Exception {
        Statement statement = connection.createStatement();
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128);";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("altering employees table schema completed, " + alterResult + " rows affected");
        String query = "SELECT email, password from employees;";
        ResultSet rs = statement.executeQuery(query);

        PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

        ArrayList<String> updateQueryList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        try {

            System.out.println("encrypting password (this might take a while)");
            while (rs.next()) {
                // get the ID and plain text password from current table
                String email = rs.getString("email");
                String password = rs.getString("password");

                // encrypt the password using StrongPasswordEncryptor
                String encryptedPassword = passwordEncryptor.encryptPassword(password);

                // generate the update query
                System.out.println("email: " + email + ", password: " + encryptedPassword);

                String updateQuery = "UPDATE employees SET password=? WHERE email=?";
                preparedStatement = connection.prepareStatement(updateQuery);
                preparedStatement.setString(1, encryptedPassword);
                preparedStatement.setString(2, email);

                updateQueryList.add(updateQuery);
            }
            rs.close();

            // execute the update queries to update the password
            System.out.println("updating password");
            int count = 0;
            for (String updateQuery : updateQueryList) {
                int updateResult = preparedStatement.executeUpdate();
                count += updateResult;
            }
            System.out.println("updating password completed, " + count + " rows affected");
            statement.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
