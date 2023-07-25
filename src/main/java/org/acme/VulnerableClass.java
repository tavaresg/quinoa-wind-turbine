import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VulnerableClass {

    public void fetchUserDetails(String username) {
        try {
            // Establish database connection
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/mydatabase", "username", "password");

            // Create SQL query
            String query = "SELECT * FROM users WHERE username = '" + username + "'";

            // Execute SQL query
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            // Process the results
            if (resultSet.next()) {
                String fetchedUsername = resultSet.getString("username");
                String fetchedPassword = resultSet.getString("password");
                System.out.println("Username: " + fetchedUsername + ", Password: " + fetchedPassword);
            } else {
                System.out.println("User not found!");
            }

            // Close the database connection
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        VulnerableClass vulnerable = new VulnerableClass();
        String inputUsername = "admin'; DROP TABLE users;--";
        vulnerable.fetchUserDetails(inputUsername);
    }
}
