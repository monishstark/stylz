import java.io.*;
import java.util.*;
import java.sql.*;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/SignUp")
public class SignUp extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
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
            String in_pass = jsonMap.get("password");
            String name= jsonMap.get("name");
            String phone= jsonMap.get("phone");


            if (isEmailExists(email)) {

                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("Email already exists");
                return;
            }

            String insertSql = "INSERT INTO authentication.\"Users\" (email, password, name, phone) VALUES (?, ?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

                insertStatement.setString(1, email);
                insertStatement.setString(2, in_pass);
                insertStatement.setString(3, name);
                insertStatement.setString(4, phone);

                int rowsAffected = insertStatement.executeUpdate();


                if (rowsAffected > 0) {
                    System.out.println("Record inserted successfully");
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().write("User registered successfully");
                } else {
                    System.out.println("Failed to insert record");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Failed to register user");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to register user");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to register user");
        }
    }

    private boolean isEmailExists(String email) {
        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String password = "1234";

        String selectSql = "SELECT COUNT(*) FROM authentication.\"Users\" WHERE email = ?";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {

            selectStatement.setString(1, email);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String password = "1234";
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, password);
            String sql = "SELECT * FROM authentication.\"Users\" where id=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, Integer.parseInt(userId));
            ResultSet resultSet = statement.executeQuery();

            List<HashMap<String, String>> dataList = new ArrayList<>();

            while (resultSet.next()) {
                HashMap<String, String> dataMap = new HashMap<>();
                dataMap.put("email", resultSet.getString("email"));
                dataMap.put("name", resultSet.getString("name"));
                dataMap.put("phone", resultSet.getString("phone"));
                dataMap.put("address", resultSet.getString("address"));
                dataMap.put("locality", resultSet.getString("locality"));
                dataMap.put("pin", resultSet.getString("pin"));

                dataList.add(dataMap);
            }


            ObjectMapper objectMapper = new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(dataList);

            try (PrintWriter writer = response.getWriter()) {
                writer.write(jsonData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
