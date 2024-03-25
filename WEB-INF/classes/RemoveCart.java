
import java.io.*;
import java.util.*;
import java.sql.*;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;  // Import HttpSession
import jakarta.servlet.ServletException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/RemoveCart")
public class RemoveCart extends HttpServlet {
    String url = "jdbc:postgresql://localhost:5432/ecommerce";
    String user = "postgres";
    String password = "1234";

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("removecart working");
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");


        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        if (userId != null) {
            response.getWriter().println("User ID from session: " + userId);

            try {
                Class.forName("org.postgresql.Driver");
                Connection connection = DriverManager.getConnection(url, user, password);
                StringBuilder requestData = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        requestData.append(line);
                    }
                }

                ObjectMapper objectMapper = new ObjectMapper();
                HashMap<String, String> jsonMap = objectMapper.readValue(requestData.toString(), new TypeReference<HashMap<String, String>>() {});


                String pid = jsonMap.get("id");
                String price = jsonMap.get("price");
                System.out.println(pid+" "+price);
                String checkSql = "SELECT * FROM cart.usercart WHERE \"ID\" = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                    checkStatement.setInt(1, Integer.parseInt(userId));
                    ResultSet resultSet = checkStatement.executeQuery();

                    if (resultSet.next()) {

                        updateCart(connection, userId, resultSet,pid,price,response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response.sendRedirect("index.html");
        }
    }



    private void updateCart(Connection connection, String userId, ResultSet resultSet, String pid, String priceStr,HttpServletResponse response) throws SQLException {

        List<Integer> existingPlist = new ArrayList<>(Arrays.asList((Integer[]) resultSet.getArray("plist").getArray()));
        int existingCost = resultSet.getInt("cost");

        int price = Integer.parseInt(priceStr);

        existingPlist.remove(existingPlist.indexOf(Integer.parseInt(pid)));


        int newCost = existingCost - price;


        String updateSql = "UPDATE cart.usercart SET plist = ?, cost = ? WHERE \"ID\" = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            Array plistArray = connection.createArrayOf("INTEGER", existingPlist.toArray());
            updateStatement.setArray(1, plistArray);
            updateStatement.setInt(2, newCost);
            updateStatement.setInt(3, Integer.parseInt(userId));

            updateStatement.executeUpdate();


        }


        catch (Exception e){
            e.printStackTrace();

        }
        if(existingPlist.size()==0){
            deleteRow(connection, userId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        }
        else{
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private void deleteRow(Connection connection, String userId) throws SQLException {
        String deleteSql = "DELETE FROM cart.usercart WHERE \"ID\" = ?";
        try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
            deleteStatement.setInt(1, Integer.parseInt(userId));
            deleteStatement.executeUpdate();
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

