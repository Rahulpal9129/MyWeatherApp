package MyPackage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


/**
 * Servlet implementation class MyServlet
 */
@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public MyServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().append("Served at: ").append(request.getContextPath());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String inputData = request.getParameter("userInput");
        String city = request.getParameter("city");

        System.out.println("User input: " + inputData);
        System.out.println("City: " + city);

        if (city == null || city.isEmpty()) {
            response.getWriter().write("City parameter is missing.");
            return;
        }

        String apiKey = "72cf08f5c7cea25f3d5da5edc4114169";
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;

        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            InputStream inputStream = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(inputStream);

            StringBuilder responseContent = new StringBuilder();
            Scanner scanner = new Scanner(reader);

            while (scanner.hasNextLine()) {
                responseContent.append(scanner.nextLine());
            }

            scanner.close();
            
            // typeCasting = Parsing the data into JSON
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);
            /*System.out.println(jsonObject); */
            
            // date and time
            long dateTimestamp=jsonObject.get("dt").getAsLong()*1000;
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateTimestamp));

            
            //Temperature
            double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
            int temperatureCelsius = (int) (temperatureKelvin-273.15);
            
            //humidity
            int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
           
            //Wind speed
            double WindSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
            
            // Weather condition
            String weatherCondition= jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
            
            // set the data aa request attributes(for sending to the jsp pages)
            request.setAttribute("date", date);
            request.setAttribute("city", city);
            request.setAttribute("temperature", temperatureCelsius);
            request.setAttribute("weatherCondition", weatherCondition);
            request.setAttribute("humidity", humidity);
            request.setAttribute("windSpeed", WindSpeed);
            request.setAttribute("weatherData", responseContent.toString());
            
            connection.disconnect();
            // forward the request to the weather.jsp page for rendering
            request.getRequestDispatcher("index.jsp").forward(request, response);
            
            response.setContentType("application/json");
            response.getWriter().write(responseContent.toString());

        } catch (IOException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Error fetching weather data.");
        }
    }
} 
