import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.*;
import java.util.*;
import java.sql.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

@WebServlet("/SendMail")
public class SendMail extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setContentType("text/html");

        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String dbpassword = "1234";
        HttpSession usession = request.getSession();
        String userId = (String) usession.getAttribute("userId");

        try {
            System.out.println("send mail");
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(url, user, dbpassword);
            String sql = "SELECT * FROM authentication.\"Users\" where id=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, Integer.parseInt(userId));
            ResultSet resultSet = statement.executeQuery();
            String email="";
            while(resultSet.next()){
                email=resultSet.getString("email");}

            String from = "gsatoru0373@gmail.com";

            String to = email;


            String username = "gsatoru0373@gmail.com";
            String password = "xkdh vdzp oqfw xrnd";

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Order successful");
            message.setText("Hello, this is a automated mail sent from mistyles your orders confirmed");
            Transport.send(message);

            out.println("<html><body>");
            out.println("<h2>Email sent successfully</h2>");
            out.println("</body></html>");
        } catch (MessagingException e) {
            throw new ServletException("Could not send email.", e);
        }
    }catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        }
}}