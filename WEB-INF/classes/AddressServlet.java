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

@WebServlet("/AddressServlet")
public class AddressServlet extends HttpServlet {
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

            String address = jsonMap.get("Address");
            String locality = jsonMap.get("Locality");
            String pin= jsonMap.get("Pin");

            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");

            String insertSql="UPDATE  authentication.\"Users\" SET address = ?, locality = ?,pin = ? WHERE id = ?";

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

                insertStatement.setString(1, address);
                insertStatement.setString(2, locality);
                insertStatement.setString(3, pin);
                insertStatement.setInt(4, Integer.parseInt(userId));

                int rowsAffected = insertStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Record inserted successfully");
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().write("Added address");
                } else {
                    System.out.println("Failed to Add address");
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Failed to Add address");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to Add address");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to Add address");
        }
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
