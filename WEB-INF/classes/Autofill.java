import java.util.*;
import java.lang.*;
import java.sql.*;
import java.io.*;


import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebServlet("/Autofill")

public class Autofill extends HttpServlet {
    static class TNode {
        Map<Character, TNode> child;
        boolean end;

        public TNode() {
            this.child = new HashMap<>();
            this.end = false;
        }
    }

    static class Trie {
        private TNode root;

        public Trie() {
            this.root = new TNode();
        }

        public void insert(String word) {
            TNode node = root;
            for (char i : word.toCharArray()) {
                if (!node.child.containsKey(i)) {
                    node.child.put(i, new TNode());
                }
                node = node.child.get(i);
            }
            node.end = true;
        }

        public List<String> autofill(String prefix) {
            TNode node = root;
            for (char i : prefix.toCharArray()) {
                if (!node.child.containsKey(i)) {
                    return new ArrayList<>();
                }
                node = node.child.get(i);
            }

            List<String> suggestions = new ArrayList<>();
            collectSuggestions(node, prefix, suggestions);
            return suggestions;
        }

        private void collectSuggestions(TNode node, String currentPrefix, List<String> suggestions) {
            if (node.end) {
                suggestions.add(currentPrefix);
            }

            for (Map.Entry<Character, TNode> entry : node.child.entrySet()) {
                collectSuggestions(entry.getValue(), currentPrefix + entry.getKey(), suggestions);
            }
        }
    }
    public void doGet(HttpServletRequest request,HttpServletResponse response){
        response.setContentType("application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST, PUT, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");

        String word= request.getParameter("word");
        System.out.println(word);
        func(word,response);
    }
    public void func(String word,HttpServletResponse response){
        Trie trie = new Trie();
        trie.insert("shirt");
        trie.insert("watch");
        trie.insert("pant");
        trie.insert("blazer");
        trie.insert("sweatshirt");
        trie.insert("suits");
        trie.insert("shoes");
        trie.insert("sneakers");
        trie.insert("socks");
        trie.insert("wallet");
        trie.insert("belt");
        trie.insert("tracksuits");
        trie.insert("headphone");
        trie.insert("speaker");
        trie.insert("tie");
        trie.insert("briefs");
        trie.insert("boxers");
        trie.insert("bedsheets");
        trie.insert("pillow");
        trie.insert("carpets");
        trie.insert("towels");
        trie.insert("mirrors");


        List<String> autoCompleteResults = trie.autofill(word);
        String jsonResult = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            jsonResult = objectMapper.writeValueAsString(autoCompleteResults);
        } catch(Exception e) {
            System.out.println(e);
        }
        try (PrintWriter out = response.getWriter()) {
            out.println(jsonResult);
        } catch (Exception e) {
            System.out.println(e);
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