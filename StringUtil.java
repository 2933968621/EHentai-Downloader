import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil
{
    public static boolean isNumber(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static String intsToString(int[] ints) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i : ints)
            stringBuilder.append(i);
        return stringBuilder.toString();
    }

    public static String getSubString(String text, String left, String right)
    {
        if (text.isEmpty())
            return "";

        String result;
        int zLen;

        if (left == null || left.isEmpty())
            zLen = 0;
        else
        {
            zLen = text.indexOf(left);

            if (zLen > -1)
                zLen += left.length();
            else
                zLen = 0;
        }

        int yLen = text.indexOf(right, zLen);

        if (yLen < 0 || right.isEmpty())
            yLen = text.length();

        result = text.substring(zLen, yLen);
        return result;
    }

    public static String StringsToString(String[] strings)
    {
        StringBuilder sb = new StringBuilder();

        if (strings != null && strings.length > 0)
            for (short i = 0; i < strings.length; i++)
                if (i < strings.length - 1)
                    sb.append(strings[i]).append(",");
                else
                    sb.append(strings[i]);

        return sb.toString();
    }

    public static String StringsToString(String[] strings, String separator)
    {
        StringBuilder sb = new StringBuilder();

        if (strings != null && strings.length > 0)
            for (short i = 0; i < strings.length; i++)
                if (i < strings.length - 1)
                    sb.append(strings[i]).append(separator);
                else
                    sb.append(strings[i]);

        return sb.toString();
    }

    public static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }

    public static String decodeUnicode(String unicode)
    {
        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(unicode);
        char ch;
        while (matcher.find()) {
            String group = matcher.group(2);
            ch = (char) Integer.parseInt(group, 16);
            String group1 = matcher.group(1);
            unicode = unicode.replace(group1, ch + "");
        }
        return unicode;
    }
}
