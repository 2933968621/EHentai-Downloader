import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Core {
    public static String[] galleryID = null;
    public static float numberOfPage = 40;
    public static List<DownloadInfo> downloadFailed = new ArrayList<>();

    public static void downloadImageSet(String url, File path, boolean retryFailed, boolean overwrite, boolean original)
    {
        if (original)
        {
            String home = WebUtil.get("https://e-hentai.org/home.php", false);
            if (home.contains("This page requires you to log on."))
            {
                System.err.println(UI.addConsoleMessage("No cookie entered or cookie error, unable to download the original image."));
                return;
            }

            long current = Long.parseLong(StringUtil.getSubString(home, "You are currently at <strong>", "</strong> towards a limit of <strong>").trim());
            long max = Long.parseLong(StringUtil.getSubString(home, "</strong> towards a limit of <strong>", "</strong>.</p>").trim());

            if (current >= max)
            {
                System.err.println(UI.addConsoleMessage("The number of downloads exceeds the limit!"));
                return;
            }
        }

        if (url.contains("?p="))
            url = url.replace("?p=" + StringUtil.getSubString(url, "?p=", ""), "");

        galleryID = getGalleryID(url);

        boolean isExHentai = isExHentai(url);

        String websiteInfo = getPageInfo(0, isExHentai);

        if (websiteInfo.isEmpty())
        {
            if (isExHentai)
                System.out.println(UI.addConsoleMessage("Unable to access ExHentai, possibly because you do not have permission or cookies"));
            return;
        }

        if (websiteInfo.contains("Your IP address has been temporarily banned for excessive pageloads which indicates that you are using automated mirroring/harvesting software."))
        {
            System.out.println(UI.addConsoleMessage("Your IP has ben banned, please try to change your IP"));
            return;
        }

        if (websiteInfo.contains("This gallery has been flagged as <strong>Offensive For Everyone</strong>."))
        {
            System.err.println(UI.addConsoleMessage("Error."));
            return;
        }

        String title = StringUtil.getSubString(websiteInfo, "</div></div></div><div id=\"gd2\"><h1 id=\"gn\">", "</h1>");
        int imageCount = getMaxImageCount(websiteInfo);
        UI.progressBar.setMaximum(imageCount);
        int progressID = -1;

        String hentaiPage = isExHentai ? "https://exhentai.org/s/" : "https://e-hentai.org/s/";
        String hentaiFImg = isExHentai ? "https://exhentai.org/fullimg.php?" : "https://e-hentai.org/fullimg.php?";

        if (imageCount > numberOfPage)
        {
            //int pageCount = Math.round(imageCount);
            for (int p = 0; p < imageCount; p++)
            {
                String pageInfo = getPageInfo(p, isExHentai(url));
                if (pageInfo.contains("Your IP address has been temporarily banned for excessive pageloads which indicates that you are using automated mirroring/harvesting software."))
                {
                    System.out.println(UI.addConsoleMessage("Your IP has ben banned, please try to change your IP"));
                    return;
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
                        String imageID = StringUtil.getSubString(imageOrig, "<a href=\"" + hentaiPage, "\"><img alt=\"" + curID + "\"");
                        String imagePage = WebUtil.get(hentaiPage + imageID, isExHentai);
                        String imageLink = (original && imagePage.contains("Download original")) ? (hentaiFImg + StringUtil.getSubString(imagePage, "a href=\"" + hentaiFImg, "\">Download original ").replaceAll("amp;", "")) : StringUtil.getSubString(imagePage, "img id=\"img\" src=\"", "\" ");
                        if (!WebUtil.download(title, curID, imageLink, path, retryFailed, overwrite, isExHentai))
                            System.err.println("ID: " + i + " No download!");
                    }
                    else
                    {
                        String imagePage = WebUtil.get(hentaiPage + StringUtil.getSubString(pageInfo, "<a href=\"" + hentaiPage, "\"><img alt=\"" + curID + "\""), isExHentai);
                        String imageLink = (original && imagePage.contains("Download original")) ? (hentaiFImg + StringUtil.getSubString(imagePage, "a href=\"" + hentaiFImg, "\">Download original ").replaceAll("amp;", "")) : StringUtil.getSubString(imagePage, "img id=\"img\" src=\"", "\" ");
                        if (!WebUtil.download(title, curID, imageLink, path, retryFailed, overwrite, isExHentai))
                            System.err.println("ID: " + i + " No download!");
                    }

                    progressID = i;
                    UI.progressBar.setValue(i);
                    if (Thread.currentThread().isInterrupted())
                        break;
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }

                if (progressID >= imageCount)
                    break; // Prevent last page from cycling
            }
        }
        else
        {
            for (int i = 1; i <= imageCount; i++)
            {
                String curID = String.format("%0" + (int) (Math.log10(imageCount) + 1) + "d", i);
                if (i > 1)
                {
                    String origID = String.format("%0" + (int) (Math.log10(imageCount) + 1) + "d", i - 1);
                    String imageOrig = StringUtil.getSubString(websiteInfo, "\"><img alt=\"" + origID + "\"", "\"><img alt=\"" + curID + "\"");
                    String imageID = StringUtil.getSubString(imageOrig, "<a href=\"" + hentaiPage, "\"><img alt=\"" + curID + "\"");
                    String imagePage = WebUtil.get(hentaiPage + imageID, isExHentai);
                    String imageLink = (original && imagePage.contains("Download original")) ? (hentaiFImg + StringUtil.getSubString(imagePage, "a href=\"" + hentaiFImg, "\">Download original ").replaceAll("amp;", "")) : StringUtil.getSubString(imagePage, "img id=\"img\" src=\"", "\" ");
                    if (!WebUtil.download(title, String.valueOf(i), imageLink, path, retryFailed, overwrite, isExHentai))
                        System.err.println("ID: " + i + " No download!");
                }
                else
                {
                    String imagePage = WebUtil.get(hentaiPage + StringUtil.getSubString(websiteInfo, "<a href=\"" + hentaiPage, "\"><img alt=\"" + curID + "\""), isExHentai);
                    String imageLink = (original && imagePage.contains("Download original")) ? (hentaiFImg + StringUtil.getSubString(imagePage, "a href=\"" + hentaiFImg, "\">Download original ").replaceAll("amp;", "")) : StringUtil.getSubString(imagePage, "img id=\"img\" src=\"", "\" ");
                    if (!WebUtil.download(title, String.valueOf(i), imageLink, path, retryFailed, overwrite, isExHentai))
                        System.err.println("ID: " + i + " No download!");
                }

                UI.progressBar.setValue(i);
                if (Thread.currentThread().isInterrupted())
                    break;
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if (!retryFailed)
            checkFail(title, path, overwrite, isExHentai);
    }

    public static void checkFail(String title, File path, boolean overwrite, boolean isExHentai)
    {
        if (!downloadFailed.isEmpty())
        {
            System.out.println(UI.addConsoleMessage("Some pictures failed to download, retying..."));
            downloadFailed.forEach(i -> WebUtil.download(title, i.getID(), i.getURL(), path, false, overwrite, isExHentai));
            System.out.println(UI.addConsoleMessage("Complete. If the download fails, please download by yourself."));
        }
    }

    public static boolean isExHentai(String url)
    {
        return url.toLowerCase().contains("exhentai.org");
    }

    public static int getMaxImageCount(String websiteInfo)
    {
        return Integer.parseInt(StringUtil.getSubString(websiteInfo, "Showing " + StringUtil.getSubString(websiteInfo, "Showing ", " of ").trim() + " of ", " images").replaceAll(",", "").trim());
    }

    public static int getCurMaxImageCount(String websiteInfo)
    {
        return Integer.parseInt(StringUtil.getSubString(StringUtil.getSubString(websiteInfo, "Showing ", " of ").trim(), " - ", "").replaceAll(",", "").trim());
    }

    public static int getCurMinImageCount(String websiteInfo)
    {
        return Integer.parseInt(StringUtil.getSubString(StringUtil.getSubString(websiteInfo, "Showing ", " of ").trim(), "", " - " + getCurMaxImageCount(websiteInfo)).replaceAll(",", "").trim());
    }

    public static String getPageInfo(int id, boolean isExHentai)
    {
        if (galleryID == null)
            throw new NullPointerException();

        String ehentai = "https://e-hentai.org/g/";
        String exhentai = "https://exhentai.org/g/";
        return WebUtil.get((isExHentai ? exhentai : ehentai) + galleryID[0] + "/" + galleryID[1] + "/?p=" + id, isExHentai);
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
