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

@WebServlet("/Sendotp")
public class Sendotp extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
        response.setContentType("text/html");

        String url = "jdbc:postgresql://localhost:5432/ecommerce";
        String user = "postgres";
        String dbpassword = "1234";

        try {
            System.out.println("send mail");
            Class.forName("org.postgresql.Driver");

            String email = request.getParameter("email");

            String insertSql = "INSERT INTO authentication.otp (email, otpc) VALUES (?, ?)";
            Random random = new Random();
            int onetime = random.nextInt(9000) + 1000;

            try (Connection connection = DriverManager.getConnection(url, user, dbpassword);
                 PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                System.out.println(onetime);
                insertStatement.setString(1, email);
                insertStatement.setString(2, String.valueOf(onetime));
                insertStatement.executeUpdate();
            }

            String from = "gsatoru0373@gmail.com";

            String to = email;
            System.out.println(to);

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
                message.setSubject("OTP");
                message.setText("Hello, this is a automated mail sent otp "+String.valueOf(onetime));
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