import java.io.*;
import java.sql.*;
import java.util.HashMap;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/ChangePassword")
public class ChangePassword extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String password = "1234";

        try {
            Class.forName("org.postgresql.Driver");

            StringBuilder requestData = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestData.append(line);
                }
            }

            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, String> jsonMap = objectMapper.readValue(requestData.toString(), new TypeReference<HashMap<String, String>>() {});

            String email = jsonMap.get("email");
            String otp = jsonMap.get("otp");
            String newpassword = jsonMap.get("password");

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                System.out.println(email+" "+"email");
                String sql = "SELECT otpc FROM authentication.otp WHERE email = ?";
                try (PreparedStatement selectStatement = connection.prepareStatement(sql)) {
                    selectStatement.setString(1, email);
                    ResultSet resultSet = selectStatement.executeQuery();

                    if (resultSet.next()) {
                        String dbOtp = resultSet.getString("otpc");

                        if (otp.equals(dbOtp)) {
                            System.out.println(newpassword);
                            sql = "UPDATE authentication.\"Users\" SET password = ? WHERE email = ?";
                            try (PreparedStatement updateStatement = connection.prepareStatement(sql)) {
                                updateStatement.setString(1, newpassword);
                                updateStatement.setString(2, email);
                                updateStatement.executeUpdate();
                            }

                            sql = "DELETE FROM authentication.otp WHERE email = ?";
                            try (PreparedStatement deleteStatement = connection.prepareStatement(sql)) {
                                deleteStatement.setString(1, email);
                                deleteStatement.executeUpdate();
                            }

                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().print("Password changed successfully");
                        } else {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().print("Authentication failed");
                        }
                    } else {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().print("User not found");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to change password");
        }
    }
}
