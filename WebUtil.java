import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebUtil {
    public static final String ipb_member_id = "";
    public static final String ipb_pass_hash = "";
    public static final String igneous = "mystery";

    public static String get(String url, boolean isExHentai)
    {
        try
        {
            HttpURLConnection connection = (HttpURLConnection) new URI(url).toURL().openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36 Edg/95.0.1020.30");
            if (!ipb_member_id.isEmpty() && !ipb_pass_hash.isEmpty())
            {
                if (!isExHentai)
                    connection.setRequestProperty("Cookie", "nw=1; nw=1; ipb_member_id=" + ipb_member_id + "; ipb_pass_hash=" + ipb_pass_hash + ";");
                else if (igneous.isEmpty() || igneous.equalsIgnoreCase("mystery"))
                {
                    return ""; // Unable to access EXHentai without permission
                }
                else
                    connection.setRequestProperty("Cookie", "nw=1; nw=1; ipb_member_id=" + ipb_member_id + "; ipb_pass_hash=" + ipb_pass_hash + "; igneous=" + igneous + ";");
            }
            else if (isExHentai)
                return ""; // Unable to access EXHentai without cookie
            else
                connection.setRequestProperty("Cookie", "nw=1; nw=1;");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            String inline;
            StringBuilder response = new StringBuilder();

            while ((inline = in.readLine()) != null) {
                response.append(inline);
                response.append("\n");
            }

            in.close();
            connection.disconnect();
            return response.toString();
        }
        catch (IOException | URISyntaxException e)
        {
            e.printStackTrace();
        }

        return "";
    }

    public static boolean download(String title, String id, String imageUrl, File path, boolean retry, boolean isExHentai) {
        try {
            if (WebUtil.get(imageUrl, isExHentai).toLowerCase().contains("downloading original files of older galleries during peak hours requires gp, and you do not have enough."))
            {
                System.out.println(UI.addConsoleMessage("Insufficient GP to download the original image!"));
                UI.original_checkbox.setSelected(false);
                return false;
            }

            //Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
            //Matcher matcher = pattern.matcher(title);
            //title = matcher.replaceAll(" ").replaceAll("[\\s]+$", "");

            if (!path.exists())
                if (!path.mkdir())
                    return false;

            File savePath = new File(path, title);
            if (!savePath.exists())
                if (!savePath.mkdir())
                    return false;

            File saveFilePath = new File(savePath, id + ".png");
            if (saveFilePath.exists() && !saveFilePath.delete())
            {
                System.out.println(UI.addConsoleMessage("ID: " + id + " has exists and it is occupied!"));
                return false;
            }

            HttpURLConnection connection = (HttpURLConnection) new URI(imageUrl).toURL().openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.54 Safari/537.36 Edg/95.0.1020.30");
            connection.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
            if (!ipb_member_id.isEmpty() && !ipb_pass_hash.isEmpty())
            {
                if (!isExHentai)
                    connection.setRequestProperty("Cookie", "nw=1; nw=1; ipb_member_id=" + ipb_member_id + "; ipb_pass_hash=" + ipb_pass_hash + ";");
                else if (igneous.isEmpty() || igneous.equalsIgnoreCase("mystery"))
                {
                    return false; // Unable to access EXHentai without permission
                }
                else
                    connection.setRequestProperty("Cookie", "nw=1; nw=1; ipb_member_id=" + ipb_member_id + "; ipb_pass_hash=" + ipb_pass_hash + "; igneous=" + igneous + ";");
            }
            else if (isExHentai)
                return false; // Unable to access EXHentai without cookie
            else
                connection.setRequestProperty("Cookie", "nw=1; nw=1;");

            DataInputStream in = new DataInputStream(connection.getInputStream());

            DataOutputStream out = new DataOutputStream(Files.newOutputStream(saveFilePath.toPath()));

            byte[] buffer = new byte[4096];
            int count;

            System.out.println(UI.addConsoleMessage("Downloading... ID: " + id + " Link: " + imageUrl));

            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }

            out.close();
            in.close();
            connection.disconnect();

            System.out.println(UI.addConsoleMessage("ID: " + id + " Download complete."));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(UI.addConsoleMessage("ID: " + id + " Link: " + imageUrl + " Failed!" + (retry ? ", retry now!" : "")));
            if (!retry)
            {
                Core.downloadFailed.add(new Core.DownloadInfo(imageUrl, id));
                return false;
            }
            else return download(title, id, imageUrl, path, false, isExHentai);
        }
    }
}
