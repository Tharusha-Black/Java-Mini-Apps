package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import com.google.gson.Gson;
import java.io.PrintWriter;
import org.json.JSONObject;

public class CalculatorServlet extends HttpServlet {

    private static class Payload {
        double value1;
        double value2;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JSONObject json = new JSONObject(sb.toString());

            if (!json.has("value1") || !json.has("value2")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Missing value1 or value2\"}");
                return;
            }

            double value1 = json.getDouble("value1");
            double value2 = json.getDouble("value2");

            String path = request.getServletPath();
            double result = 0;

            switch (path) {
                case "/add":
                    result = value1 + value2;
                    break;
                case "/subtract":
                    result = value1 - value2;
                    break;
                case "/multiply":
                    result = value1 * value2;
                    break;
                case "/divide":
                    if (value2 == 0) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.println("{\"error\": \"Division by zero\"}");
                        return;
                    }
                    result = value1 / value2;
                    break;
                default:
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"error\": \"Unknown operation\"}");
                    return;
            }

            JSONObject resultJson = new JSONObject();
            resultJson.put("result", result);
            out.println(resultJson.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Server error: " + e.getMessage());
            out.println(errorJson.toString());
            e.printStackTrace(); // helpful in Tomcat logs
        }
    }

}
