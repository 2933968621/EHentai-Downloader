import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebUtil {
    public static String get(String url) {
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36 Edg/95.0.1020.30");
            connection.setRequestProperty("Cookie", "nw=1; nw=1;");
            
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            String inputline;
            StringBuilder response = new StringBuilder();

            while ((inputline = in.readLine()) != null) {
                response.append(inputline);
                response.append("\n");
            }

            in.close();
            connection.disconnect();
            return response.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "";
    }
}
