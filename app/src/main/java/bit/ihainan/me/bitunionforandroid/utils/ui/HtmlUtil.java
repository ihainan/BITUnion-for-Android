package bit.ihainan.me.bitunionforandroid.utils.ui;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTML Util
 */
public class HtmlUtil {
    public final static String TAG = HtmlUtil.class.getSimpleName();

    private String mBody;

    public HtmlUtil(String html) {
        this.mBody = html;
    }

    private StringBuilder mCssBuilder = new StringBuilder("<style type=\"text/css\">");

    public void addCss(String cssStyle) {
        mCssBuilder.append(cssStyle);
    }

    public String getCss() {
        return mCssBuilder.append("</style>").toString();
    }

    private StringBuilder mHeadBuilder = new StringBuilder("<head>");

    public void addHead(String head) {
        mHeadBuilder.append(head);
    }

    public String getHead() {
        return mHeadBuilder.append("</head>").toString();
    }

    private StringBuilder mJavascript = new StringBuilder("<script>");

    public void addJavascript(String js) {
        mJavascript.append(js);
    }

    public String getJavascript() {
        return mJavascript.append("</script>").toString();
    }

    private StringBuilder mHtmlAll = new StringBuilder("<html>");

    public String makeAll() {
        processBody();
        // addHead("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\" />");
        Log.d(TAG, "Body After = " + mBody);
        // return mHtmlAll.append(getHead()).append("<body>" + mBody).append("</body></html>").toString();
        return mBody;
    }

    private static final String QUOTE_HEAD = "<br><br><center><table[^>]+><tr><td>&nbsp;&nbsp;引用(?:\\[<a href='[\\w\\.&\\?=]+?'>查看原帖</a>])*?.</td></tr><tr><td><table.{101,102}bgcolor='ALTBG2'>";
    private static final String QUOTE_TAIL = "</td></tr></table></td></tr></table></center><br>";
    private static final String QUOTE_REGEX = QUOTE_HEAD
            + "(((?!<br><br><center><table border=)[\\w\\W])*?)" + QUOTE_TAIL;

    public static String replaceImage(String str) {
        // 图片
        Pattern p = Pattern.compile("<img src='([^>']+)'[^>]*(width>)?[^>]*'>");
        Matcher m = p.matcher(str);
        while (m.find()) {
            String url = parseLocalImage(m.group(1));
            if (url.contains("file:///android_asset/"))
                url = "<img id = 'face' src='" + url + "'>";
            else
                url = "<img src='" + url + "'>";
            str = str.replace(m.group(0), url);
            m = p.matcher(str);
        }

        // 删除前后的 br
        str = str.replaceAll("(<br>)+<img", "<br><img");
        return str;
    }

    public static String parseLocalImage(String imgUrl) {
        // 检查是否为本地表情文件
        Pattern p = Pattern.compile("\\.\\./images/(smilies|bz)/(.+?)\\.gif$");
        Matcher m = p.matcher(imgUrl);
        if (m.find()) {
            // Use local assets for emotions
            Log.d(TAG, "parseLocalImage >> " + imgUrl + " - " + "file:///android_asset/faces/" + m.group(1) + "_" + m.group(2) + ".gif");
            imgUrl = "file:///android_asset/faces/" + m.group(1) + "_" + m.group(2) + ".gif";
        }
        return imgUrl;
    }

    public static String replaceBase(String str) {
        // 单引号双引号
        str = str.replaceAll("\"", "'");

        // Open API 标志
        if (str.contains("From BIT-Union Open API Project"))
            str = str.replaceAll("<br/><span id='id_open_api_label'>..:: <a href=http://www.bitunion.org>From BIT-Union Open API Project</a> ::..<br/>", "");

        // 换行
        str = str.replaceAll("\r\n", "");
        str = str.replaceAll("\n", "");
        Log.d(TAG, "Body Before = " + str);
        str = str.replaceAll("<br />", "<br>");
        str = str.replaceAll("<br/>", "<br>");
        str = str.replaceAll("(<br>){2,}", "<br>");
        str = str.replaceAll("<br>", "<br><br>");

        return str;
    }

    public static String replaceQuote(String str) {
        Pattern p = Pattern.compile(QUOTE_REGEX);
        Matcher m = p.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(0), "<blockquote>" + m.group(1).trim() + "</blockquote>");
            m = p.matcher(str);

        }

        // 多余的换行
        str = str.replaceAll("</blockquote>(\\s)*(<br>)+", "</blockquote>");
        return str;
    }

    public static String replaceDel(String result) {
        String regex = "\\[s\\](.*?)\\[/s\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(result);
        while (m.find()) {
            System.out.println(m.group());
            result = result.replace(m.group(0), "<u>" + m.group(1) + "</u>");
        }

        return result;
    }

    public static String replaceLastEdit(String str) {
        // Last Edit
        Pattern p = Pattern.compile("(<br>)*\\[ Last edited by (.*?) on (.*?) at (.*?) \\]");
        Matcher m = p.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(0), "");
        }

        return str;
    }

    public static String replaceOther(String str) {
        str = str.replaceAll("(<br>)*$", "");
        return str;
    }

    private void processBody() {
        mBody = replaceBase(mBody); // 基本替换，如换行，br 等
        mBody = replaceDel(mBody);  // 特殊处理 [s] 标签
        mBody = replaceImage(mBody);    // 替换图片地址
        mBody = replaceQuote(mBody);    // 替换引用
        mBody = replaceLastEdit(mBody); // 删除 Last Edit
        mBody = replaceOther(mBody);    // 剩余内容
    }

    // TODO: 处理原始文本和 UBB Code
    public static String ubbToHtml(String ubbStr) {
        String result = ubbStr.replace("\n", "<br>");
        result = result.replaceAll("\\[b\\]", "<b>");
        result = result.replaceAll("\\[/b\\]", "</b>");

        result = result.replaceAll("\\[quote\\]", "<blockquote>");
        result = result.replaceAll("\\[/quote\\]", "</blockquote>");

        result = result.replaceAll("\\[i\\]", "<i>");
        result = result.replaceAll("\\[/i\\]", "</i>");

        result = result.replaceAll("\\[u\\]", "<u>");
        result = result.replaceAll("\\[/u\\]", "</u>");

        // 链接
        result = replaceUrl(result);

        // 替换表情
        result = replaceEmotion(result);

        return result;
    }

    public static String replaceUrl(String result) {
        String regex = "\\[url=(.+?)\\](.+?)\\[/url\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(result);
        while (m.find()) {
            result = result.replace(m.group(0), "<a href='" + m.group(1) + "'>" + m.group(2) + "</a>");
        }
        return result;
    }

    public static String replaceEmotion(String result) {
        String regex = ":(\\S{1,10}?):";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(result);
        while (m.find()) {
            if (Emoticons.EMOTICONS.keySet().contains(m.group(0))) {
                System.out.println("Match \"" + m.group() +
                        "\"at positions " +
                        m.start() + " - " + (m.end() - 1));
                if (Emoticons.EMOTICONS.get(m.group(0)).startsWith("smilies_"))
                    result = result.replace(m.group(0), "<img src=\"../images/smilies/" + Emoticons.EMOTICONS.get(m.group(0)).substring(8) + "\" align=\"absmiddle\" border=\"0\">");
                else if (Emoticons.EMOTICONS.get(m.group(0)).startsWith("bz_"))
                    result = result.replace(m.group(0), "<img src=\"../images/bz/" + Emoticons.EMOTICONS.get(m.group(0)).substring(3) + "\" align=\"absmiddle\" border=\"0\">");
            }
        }

        return result;
    }

    public static String formatHtml(String html) {
        html = replaceBase(html); // 基本替换，如换行，br 等
        html = replaceDel(html);  // 特殊处理 [s] 标签
        html = replaceImage(html);    // 替换图片地址
        html = replaceQuote(html);    // 替换引用
        html = replaceLastEdit(html); // 删除 Last Edit
        html = replaceOther(html);    // 剩余内容

        return html;
    }

    public static String getSummaryOfMessage(String html) {
        // 删除 blockquote
        html = html.replaceAll("<blockquote>.*?</blockquote>", "");

        // 图片
        html = html.replaceAll("<img.*?>", "[图片]");

        Log.d(TAG, "getSummaryOfMessage >> " + html);

        // 获取原始文本
        html = html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");

        return html.trim();
    }
}
