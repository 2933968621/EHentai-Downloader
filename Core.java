import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Core {
    public static String[] galleryID = null;
    public static float numberOfPage = 40;
    public static List<DownloadInfo> downloadFailed = new ArrayList<>();

    public static void downloadImageSet(String url, boolean retryFailed)
    {
        if (url.contains("?p="))
            url = url.replace("?p=" + StringUtil.getSubString(url, "?p=", ""), "");

        galleryID = getGalleryID(url);

        String websiteInfo = getPageInfo(0, isExHentai(url));

        if (websiteInfo.isEmpty())
            return;

        if (websiteInfo.contains("Your IP address has been temporarily banned for excessive pageloads which indicates that you are using automated mirroring/harvesting software."))
        {
            System.out.println("Your IP has ben banned, please try to change your IP");
            System.exit(1);
        }
        String title = StringUtil.getSubString(websiteInfo, "</div></div></div><div id=\"gd2\"><h1 id=\"gn\">", "</h1>");
        int imageCount = getMaxImageCount(websiteInfo);
        int progressID = -1;
        if (imageCount > numberOfPage)
        {
            int pageCount = Math.round(imageCount);
            for (int p = 0; p < pageCount; p++)
            {
                String pageInfo = getPageInfo(p, isExHentai(url));
                if (pageInfo.contains("Your IP address has been temporarily banned for excessive pageloads which indicates that you are using automated mirroring/harvesting software."))
                {
                    System.out.println("Your IP has ben banned, please try to change your IP");
                    System.exit(1);
                }
                int pageMaxImageCount = getCurMaxImageCount(pageInfo);
                int pageMinImageCount = getCurMinImageCount(pageInfo);
                for (int i = pageMinImageCount; i <= pageMaxImageCount; i++)
                {
                    String curID = String.format("%0" + (int) (Math.log10(imageCount) + 1) + "d", i);
                    if (i > pageMinImageCount)
                    {
                        String origID = String.format("%0" + (int) (Math.log10(imageCount) + 1) + "d", i - 1);
                        String imageOrig = StringUtil.getSubString(pageInfo, "\"><img alt=\"" + origID + "\"", "\"><img alt=\"" + curID + "\"");
                        String imageID = StringUtil.getSubString(imageOrig, "<a href=\"https://e-hentai.org/s/", "\"><img alt=\"" + curID + "\"");
                        String imagePage = WebUtil.get("https://e-hentai.org/s/" + imageID);
                        String imageLink = StringUtil.getSubString(imagePage, "img id=\"img\" src=\"", "\" ");
                        download(title, curID, imageLink);
                    }
                    else
                    {
                        String imagePage = WebUtil.get("https://e-hentai.org/s/" + StringUtil.getSubString(pageInfo, "<a href=\"https://e-hentai.org/s/", "\"><img alt=\"" + curID + "\""));
                        String imageLink = StringUtil.getSubString(imagePage, "img id=\"img\" src=\"", "\" ");
                        download(title, curID, imageLink);
                    }
                    progressID = i;
                }

                if (progressID >= imageCount)
                    break; // Prevent last page from cycling
            }
        }
        else
        {
            for (int i = 1; i <= imageCount; i++)
            {
                if (i > 1)
                {
                    String imageOrig = StringUtil.getSubString(websiteInfo, "\"><img alt=\"" + (i - 1) + "\"", "\"><img alt=\"" + i + "\"");
                    String imageID = StringUtil.getSubString(imageOrig, "<a href=\"https://e-hentai.org/s/", "\"><img alt=\"" + i + "\"");
                    String imagePage = WebUtil.get("https://e-hentai.org/s/" + imageID);
                    String imageLink = StringUtil.getSubString(imagePage, "img id=\"img\" src=\"", "\" ");
                    download(title, String.valueOf(i), imageLink);
                }
                else
                {
                    String imagePage = WebUtil.get("https://e-hentai.org/s/" + StringUtil.getSubString(websiteInfo, "<a href=\"https://e-hentai.org/s/", "\"><img alt=\"1\""));
                    String imageLink = StringUtil.getSubString(imagePage, "img id=\"img\" src=\"", "\" ");
                    download(title, String.valueOf(i), imageLink);
                }
            }
        }

        if (retryFailed)
            checkFail(title);
    }

    public static void checkFail(String title)
    {
        if (!downloadFailed.isEmpty())
        {
            System.out.println("Some pictures failed to download, retying...");
            downloadFailed.forEach(i -> download(title, i.getID(), i.getURL()));
            System.out.println("Complete. If the download fails, please download by yourself.");
        }
    }

    public static void download(String title, String id, String imageUrl) {
        try {
            URL url = new URL(imageUrl);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");

            DataInputStream in = new DataInputStream(connection.getInputStream());

            Pattern pattern = Pattern.compile("[\\s\\\\/:\\*\\?\\\"<>\\|]");
            Matcher matcher = pattern.matcher(title);
            title = matcher.replaceAll(" ");

            File savePath = new File("C:\\Backup\\" + title);
            if (!savePath.exists())
                if (!savePath.mkdir())
                    return;

            File saveFilePath = new File(savePath, id + ".png");
            if (saveFilePath.exists())
            {
                if (saveFilePath.delete())
                    System.out.println("ID: " + id + " has exists!, Deleted!");
                else
                {
                    System.out.println("ID: " + id + " has exists!");
                    return;
                }
            }

            DataOutputStream out = new DataOutputStream(Files.newOutputStream(saveFilePath.toPath()));

            byte[] buffer = new byte[4096];
            int count;

            System.out.println("Downloading... ID: " + id + " Link: " + imageUrl);

            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }

            out.close();
            in.close();
            connection.disconnect();

            System.out.println("ID: " + id + " Download complete.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ID: " + id + " Link: " + imageUrl + " Failed!");
            downloadFailed.add(new DownloadInfo(imageUrl, id));
        }
    }

    public static boolean isExHentai(String url)
    {
        return url.toLowerCase().contains("exhentai.org");
    }

    public static int getMaxImageCount(String websiteInfo)
    {
        return Integer.parseInt(StringUtil.getSubString(websiteInfo, "Showing " + StringUtil.getSubString(websiteInfo, "Showing ", " of ").trim() + " of ", " images").trim());
    }

    public static int getCurMaxImageCount(String websiteInfo)
    {
        return Integer.parseInt(StringUtil.getSubString(StringUtil.getSubString(websiteInfo, "Showing ", " of ").trim(), " - ", "").trim());
    }

    public static int getCurMinImageCount(String websiteInfo)
    {
        return Integer.parseInt(StringUtil.getSubString(StringUtil.getSubString(websiteInfo, "Showing ", " of ").trim(), "", " - " + getCurMaxImageCount(websiteInfo)).trim());
    }

    public static String getPageInfo(int id, boolean isExHentai)
    {
        if (galleryID == null)
            throw new NullPointerException();

        String ehentai = "https://e-hentai.org/g/";
        String exhentai = "https://exhentai.org/g/";
        return WebUtil.get((isExHentai ? exhentai :ehentai) + galleryID[0] + "/" + galleryID[1] + "/?p=" + id);
    }

    public static String[] getGalleryID(String url)
    {
        String id1 = StringUtil.getSubString(url, ".org/g/", "/");
        String id2 = StringUtil.getSubString(url, ".org/g/" + id1 + "/", "/");
        if (id2.isEmpty())
            id2 = StringUtil.getSubString(url, ".org/g/" + id1 + "/", "");
        return new String[] {id1, id2};
    }

    public static class DownloadInfo
    {
        private final String url;
        private final String id;

        public DownloadInfo(String url, String id)
        {
            this.url = url;
            this.id = id;
        }

        public String getURL() {
            return this.url;
        }

        public String getID() {
            return this.id;
        }
    }
}
