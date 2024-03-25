
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

@WebServlet("/CartServlet")
public class CartServlet extends HttpServlet {

    String url = "jdbc:postgresql://localhost:5432/ecommerce";
    String user = "postgres";
    String password = "1234";
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("cart working");
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

                String checkSql = "SELECT * FROM cart.usercart WHERE \"ID\" = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                    checkStatement.setInt(1, Integer.parseInt(userId));
                    ResultSet resultSet = checkStatement.executeQuery();

                    if (resultSet.next()) {

                        updateCart(connection, userId, resultSet,pid,price);
                    } else {

                        insertCart(connection, userId,pid,price);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response.sendRedirect("index.html");
        }
    }

    private void updateCart(Connection connection, String userId, ResultSet resultSet, String pid, String priceStr) throws SQLException {

        List<Integer> existingPlist = new ArrayList<>(Arrays.asList((Integer[]) resultSet.getArray("plist").getArray()));
        int existingCost = resultSet.getInt("cost");

        int price = Integer.parseInt(priceStr);

        existingPlist.add(Integer.parseInt(pid));


        int newCost = existingCost + price;


        String updateSql = "UPDATE cart.usercart SET plist = ?, cost = ? WHERE \"ID\" = ?";
        try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
            Array plistArray = connection.createArrayOf("INTEGER", existingPlist.toArray());
            updateStatement.setArray(1, plistArray);
            updateStatement.setInt(2, newCost);
            updateStatement.setInt(3, Integer.parseInt(userId));

            updateStatement.executeUpdate();
        }
    }


    private void insertCart(Connection connection, String userId,String pid,String  priceStr) throws SQLException {


        int price = Integer.parseInt(priceStr);


        List<Integer> newPlist = new ArrayList<>();
        newPlist.add(Integer.parseInt(pid));


        String insertSql = "INSERT INTO cart.usercart (\"ID\", plist, cost) VALUES (?, ?, ?)";
        try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
            Array plistArray = connection.createArrayOf("INTEGER", newPlist.toArray());
            insertStatement.setInt(1, Integer.parseInt(userId));
            insertStatement.setArray(2, plistArray);
            insertStatement.setInt(3, price);

            insertStatement.executeUpdate();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");



        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        if (userId != null) {

            try {
                Class.forName("org.postgresql.Driver");
                Connection connection = DriverManager.getConnection(url, user, password);





                String checkSql = "SELECT * FROM cart.usercart WHERE \"ID\" = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                    checkStatement.setInt(1, Integer.parseInt(userId));
                    ResultSet resultSet = checkStatement.executeQuery();
                    List<Integer> plistList = new ArrayList<>();
                    if(resultSet.next()) {
                        Array plistArray = resultSet.getArray("plist");
                        Integer[] plist = (Integer[]) plistArray.getArray();

                        plistList = Arrays.asList(plist);

                    }
                    else{
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    getProducts(response,plistList,connection);
                }catch (Exception e){
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            response.setStatus(HttpServletResponse.SC_OK);


        }
        else{
            response.sendRedirect("index.html");

        }


    }

    public void getProducts(HttpServletResponse response, List<Integer> arr, Connection connection) {
        String sql = "SELECT * FROM products.\"productDetails\" WHERE \"ID\" IN (";
        for (int i = 0; i < arr.size(); i++) {
            sql += "?";
            if (i < arr.size() - 1) {
                sql += ",";
            }
        }
        sql += ")";

        try (PreparedStatement checkStatement = connection.prepareStatement(sql)) {
            for (int i = 0; i < arr.size(); i++) {
                checkStatement.setInt(i + 1, arr.get(i));
            }

            try (ResultSet resultSet = checkStatement.executeQuery()) {
                List<HashMap<String, String>> dataList = new ArrayList<>();

                while (resultSet.next()) {
                    HashMap<String, String> dataMap = new HashMap<>();
                    dataMap.put("id", resultSet.getString("ID"));
                    dataMap.put("pname", resultSet.getString("pname"));
                    dataMap.put("pdetails", resultSet.getString("pdetails"));
                    dataMap.put("price", resultSet.getString("price"));
                    dataMap.put("discount", resultSet.getString("discount"));
                    dataMap.put("type", resultSet.getString("type"));
                    dataMap.put("img", resultSet.getString("img"));

                    dataList.add(dataMap);
                }

                ObjectMapper objectMapper = new ObjectMapper();
                String jsonData = objectMapper.writeValueAsString(dataList);

                try (PrintWriter writer = response.getWriter()) {
                    writer.write(jsonData);
                }
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

