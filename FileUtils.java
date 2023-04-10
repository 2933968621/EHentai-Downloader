import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean keepDirStructure) throws Exception
    {
        byte[] buf = new byte[2048];
        if (sourceFile.isFile())
        {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1)
                zos.write(buf, 0, len);
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0)
            {
                if (keepDirStructure)
                {
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    zos.closeEntry();
                }
            }
            else
                for (File file : listFiles)
                    compress(file, zos, (keepDirStructure ? (name + "/") : "") + file.getName(), keepDirStructure);
        }
    }

    public static void toZip(String srcDir, File outputPath, boolean keepDirStructure) throws RuntimeException
    {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(Files.newOutputStream(outputPath.toPath()));
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, sourceFile.getName(), keepDirStructure);
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        }
        finally
        {
            if (zos != null)
            {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void toZip(List<File> srcFiles, File outputPath) throws RuntimeException
    {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(Files.newOutputStream(outputPath.toPath()));
            for (File srcFile : srcFiles) {
                byte[] buf = new byte[2048];
                zos.putNextEntry(new ZipEntry(srcFile.getName()));
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                while ((len = in.read(buf)) != -1)
                    zos.write(buf, 0, len);
                zos.closeEntry();
                in.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        }
        finally
        {
            if (zos != null)
            {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void toZip(String srcDir, File outputPath) throws RuntimeException
    {
        toZip(srcDir, outputPath, true);
    }
}
