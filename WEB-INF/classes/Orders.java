
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

@WebServlet("/Orders")
public class Orders extends HttpServlet {

    String url = "jdbc:postgresql://localhost:5432/ecommerce";
    String user = "postgres";
    String password = "1234";

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
                    int cost=0;
                    if(resultSet.next()) {
                        Array plistArray = resultSet.getArray("plist");
                        Integer[] plist = (Integer[]) plistArray.getArray();
                        cost=resultSet.getInt("cost");

                        plistList = Arrays.asList(plist);

                    }
                    else{
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }
                    Random random = new Random();
                    String sql ="insert INTO cart.orders (id, pid, cost, delivery) VALUES (?, ?, ?, ?)";
                    for (int i=0;i<plistList.size();i++){
                        try (PreparedStatement statement = connection.prepareStatement(sql)) {
                            int randomNumber = random.nextInt(10) + 1;
                            statement.setInt(1, Integer.parseInt(userId));
                            statement.setInt(2, plistList.get(i));
                            statement.setInt(3, cost);
                            statement.setInt(4, randomNumber);
                            statement.executeUpdate();

                        }


                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                String deleteSql = "DELETE FROM cart.usercart WHERE \"ID\" = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setInt(1, Integer.parseInt(userId));
                    deleteStatement.executeUpdate();
                } catch (Exception e) {
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

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Set content type to JSON
        response.setCharacterEncoding("UTF-8");

        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String password = "1234";
        HttpSession session = request.getSession();
        String userId = (String) session.getAttribute("userId");

        if (userId != null) {
            try {
                Class.forName("org.postgresql.Driver");
                Connection connection = DriverManager.getConnection(url, user, password);

                String checkSql = "SELECT * FROM cart.orders WHERE id = ?";
                try (PreparedStatement checkStatement = connection.prepareStatement(checkSql)) {
                    checkStatement.setInt(1, Integer.parseInt(userId));
                    ResultSet resultSet = checkStatement.executeQuery();

                    ArrayList<Integer> arr = new ArrayList<>();
                    ArrayList<Integer> arrdel = new ArrayList<>();

                    while (resultSet.next()) {
                        arr.add(resultSet.getInt("pid"));
                        arrdel.add(resultSet.getInt("delivery"));
                    }

                    // Fetch product details from the "productDetails" table
                    String productDetailsSql = "SELECT * FROM products.\"productDetails\" WHERE \"ID\" IN (" + String.join(",", Collections.nCopies(arr.size(), "?")) + ")";
                    try (PreparedStatement productDetailsStatement = connection.prepareStatement(productDetailsSql)) {
                        for (int i = 0; i < arr.size(); i++) {
                            productDetailsStatement.setInt(i + 1, arr.get(i));
                        }

                        ResultSet productDetailsResultSet = productDetailsStatement.executeQuery();

                        // Convert product details to a list of maps
                        List<Map<String, Object>> productList = new ArrayList<>();
                        int i=0;
                        while (productDetailsResultSet.next()) {
                            Map<String, Object> productMap = new HashMap<>();
                            productMap.put("id", productDetailsResultSet.getInt("ID"));
                            productMap.put("pname", productDetailsResultSet.getString("pname"));
                            productMap.put("pdetails", productDetailsResultSet.getString("pdetails"));
                            productMap.put("img", productDetailsResultSet.getString("img"));
                            productMap.put("delivery", arrdel.get(i));
                            productMap.put("price", productDetailsResultSet.getInt("price"));
                            productMap.put("discount", productDetailsResultSet.getInt("discount"));
                            productList.add(productMap);
                            i++;
                        }

                        // Convert product list to JSON string
                        ObjectMapper objectMapper = new ObjectMapper();
                        String jsonProductList = objectMapper.writeValueAsString(productList);

                        // Send JSON response to the client
                        response.getWriter().write(jsonProductList);
                    }
                }
                } catch (Exception e) {
                    e.printStackTrace();
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
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

