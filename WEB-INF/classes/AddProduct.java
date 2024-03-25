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

@WebServlet("/AddProduct")
public class AddProduct extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String password = "1234";

        try {
            System.out.println("add product");
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

            String id = jsonMap.get("id");
            String pname = jsonMap.get("pname");
            String pdetails = jsonMap.get("pdetails");
            String price = jsonMap.get("price");
            String discount = jsonMap.get("discount");
            String type = jsonMap.get("type");
            String img = jsonMap.get("img");

            // Data validation
            if (!isValidInteger(id) || !isValidInteger(price) || !isValidInteger(discount)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid data format");
                return;
            }

            String insertSql = "INSERT INTO products.\"productDetails\" (\"ID\", pname, pdetails, price, discount, type, img) VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection connection = DriverManager.getConnection(url, user, password);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {

                insertStatement.setInt(1, Integer.parseInt(id));
                insertStatement.setString(2, pname);
                insertStatement.setString(3, pdetails);
                insertStatement.setInt(4, Integer.parseInt(price));
                insertStatement.setInt(5, Integer.parseInt(discount));
                insertStatement.setString(6, type);
                insertStatement.setString(7, img);

                int rowsAffected = insertStatement.executeUpdate();

                if (rowsAffected > 0) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().write("Product added successfully");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Failed to add product");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Failed to add product");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Failed to add product");
        }
    }

    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private boolean isValidInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
