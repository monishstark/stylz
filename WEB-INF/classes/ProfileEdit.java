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

@WebServlet("/ProfileEdit")
public class ProfileEdit extends HttpServlet {
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

            String name = jsonMap.get("name");
            String phone = jsonMap.get("phone");
            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");
            System.out.println(name);
            System.out.println(phone);

            if (!name.isEmpty()) {
                String updateNameSql = "UPDATE authentication.\"Users\" SET name=? WHERE id = ?";
                try (Connection connection = DriverManager.getConnection(url, user, password);
                     PreparedStatement updateNameStatement = connection.prepareStatement(updateNameSql)) {
                    updateNameStatement.setString(1, name);
                    updateNameStatement.setInt(2, Integer.parseInt(userId));
                    updateNameStatement.executeUpdate();
                }
            }

            if (!phone.isEmpty()) {
                String updatePhoneSql = "UPDATE authentication.\"Users\" SET phone=? WHERE id = ?";
                try (Connection connection = DriverManager.getConnection(url, user, password);
                     PreparedStatement updatePhoneStatement = connection.prepareStatement(updatePhoneSql)) {
                    updatePhoneStatement.setString(1, phone);
                    updatePhoneStatement.setInt(2, Integer.parseInt(userId));
                    updatePhoneStatement.executeUpdate();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
