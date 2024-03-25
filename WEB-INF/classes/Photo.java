
import java.io.*;
import java.util.*;
import java.sql.*;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Part;

@WebServlet("/Photo")
@MultipartConfig(
        maxFileSize = 1024 * 1024 * 5, // 5 MB
        maxRequestSize = 1024 * 1024 * 10 // 10 MB
)
public class Photo extends HttpServlet {


    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");


        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String password = "1234";
        System.out.println("photo working");
        try {
            Class.forName("org.postgresql.Driver");
            Part filePart = request.getPart("photo");

            System.out.println(filePart);
            InputStream inputStream = filePart.getInputStream();
            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");

            System.out.println("photo working 2");

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                String sql = "update  authentication.\"Users\" set profilepic=? where id=?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setBinaryStream(1, inputStream);
                    preparedStatement.setInt(2,Integer.parseInt(userId));


                    preparedStatement.executeUpdate();


                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("image/jpeg");

        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String password = "1234";

        try {
            Class.forName("org.postgresql.Driver");

            HttpSession session = request.getSession();
            String userId = (String) session.getAttribute("userId");

            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                String sql = "SELECT profilepic FROM authentication.\"Users\" WHERE id=?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                    preparedStatement.setInt(1, Integer.parseInt(userId));

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            InputStream inputStream = resultSet.getBinaryStream("profilepic");
                            if (inputStream != null) {
                                try (OutputStream outputStream = response.getOutputStream()) {
                                    byte[] buffer = new byte[4096];
                                    int bytesRead;
                                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                                        outputStream.write(buffer, 0, bytesRead);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
