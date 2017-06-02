package me.ihainan.bu.app.utils.ui;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.BUApplication;

/**
 * HTML Util
 */
public class HtmlUtil {
    // TAGs
    private final static String TAG = HtmlUtil.class.getSimpleName();

    // Regex
    public final static String[] REGEX_DEVICE_ARRAY = new String[]{"<a .*?>\\.\\.::发自(.*?)::\\.\\.</a>",
            "<br><br>发送自 <a href='.*?' target='_blank'><b>(.*?) @BUApp</b></a>",
            "..::发自(.*?)::..",
            "<i>来自傲立独行的(.*?)客户端</i>",
            "<br><br><i>发自联盟(.*?)客户端</i>",
            "<a href='.*?>..::发自联盟(.*?)客户端::..</a>",
            "<br><br>Sent from my (.+?)$",
            "<br><br><b>发自 (.+?) @BU for Android</b>$",
            "<a href='http://out.bitunion.org/thread-10614850-1-1.html' target='_blank'><b>发自 (.*?) @BU for Android</b></a>"};
    private static final String QUOTE_HEAD = "<br><center><table[^>]+><tr><td>&nbsp;&nbsp;引用(?:\\[<a href='[\\w\\.&\\?=]+?'>查看原帖</a>])*?.</td></tr><tr><td><table.{101,102}bgcolor='ALTBG2'>";
    private static final String QUOTE_TAIL = "</td></tr></table></td></tr></table></center><br>";
    private static final String QUOTE_REGEX = QUOTE_HEAD
            + "(((?!<br><br><center><table border=)[\\w\\W])*?)" + QUOTE_TAIL;

    private static final String CODE_REGEX = "<br><center><table .*?><tr><td .*?>&nbsp;&nbsp;代码:</td><td .*?><a href=\'###\' class=\'smalltxt\' onclick=.*?>.*?</a>&nbsp;&nbsp;</td></tr><tr><td colspan=\\'2\\'><table .*?><tr><td .*?><div class=\\'hl-surround\\'><ol type=1>(.*?)</div></td></tr></table></td></tr></table></center><br>";
    private static final String VIDEO_REGEX = "\\[video](.*?)\\[/video]";

    /* START - HTML Format */
    public static String formatHtml(String html) {
        html = replaceBase(html); // 基本替换，如换行等
        html = replaceUBBDel(html);  // 特殊处理 [s] 标签
        html = replaceImage(html);    // 替换图片地址
        html = replaceCode(html);   // 替换代码
        html = replaceQuote(html);    // 替换引用
        html = replaceVideo(html);  // 替换视频
        html = replaceLastEdit(html); // 删除 Last Edit
        html = replaceOther(html);    // 剩余内容

        return html;
    }

    /**
     * 替换 HTML 文本中的图片标签，特殊处理表情图片，非表情图片添加链接标签
     *
     * @param str 原始 HTML 文本
     * @return 处理之后的 HTML 文本
     */

    private static String replaceImage(String str) {
        // 图片
        Pattern p = Pattern.compile("<img src='([^>']+)'[^>]*(width>)?[^>]*'>");
        Matcher m = p.matcher(str);
        while (m.find()) {
            String url = parseLocalImage(m.group(1));
            if (url.contains("file:///android_asset/"))
                url = "<img id = 'face' src='" + url + "'>";
            else
                url = "<a href = '" + BUApplication.IMAGE_URL_PREFIX + url + "'>" + "<img src='" + url + "'></a>";
            str = str.replace(m.group(0), url);
            m = p.matcher(str);
        }

        // 删除前后的 br
        str = str.replaceAll("(<br>)+<img", "<br><img");
        return str;
    }

    /**
     * 替换表情 URL 为本地 URL（file:///android_asset/faces/），避免重复下载
     *
     * @param imgUrl 原始表情图片 URL
     * @return 处理之后的 URL
     */
    private static String parseLocalImage(String imgUrl) {
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

    /**
     * 基础 HTML 文本处理，包含去除 Open API 标志，双引号转单引号，多个换行转换成一个换成等等
     *
     * @param str 原始 HTML 文本
     * @return 处理之后的 HTML 文本
     */
    private static String replaceBase(String str) {
        // 单引号双引号
        str = str.replace("\"", "'");
        // str = str.replaceAll("&nbsp;", "");

        // Open API 标志
        if (str.contains("From BIT-Union Open API Project"))
            str = str.replace("<br/><span id='id_open_api_label'>..:: <a href=http://www.bitunion.org>From BIT-Union Open API Project</a> ::..<br/>", "");

        // 换行
        str = str.replace("\r\n", "");
        str = str.replace("\n", "");
        Log.d(TAG, "Body Before = " + str);
        str = str.replace("<br />", "<br>");
        str = str.replace("<br/>", "<br>");
        str = str.replaceAll("(<br>\\s*){2,}", "<br>");
        str = str.replace("<br>", "<br><br>");

        return str;
    }

    /**
     * 替换 HTML 文本中的代码文本为 blockquote
     *
     * @param str 原始 HTML 文本
     * @return 处理之后的 HTML 文本
     */
    private static String replaceCode(String str) {
        Pattern pattern = Pattern.compile(CODE_REGEX);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String codeContent = matcher.group(1);
            codeContent = codeContent.replace("<li>", "\n").replaceFirst("\n", "").replace("&nbsp;", " ");
            String htmlUrl = "http://www.ihainan.me?content=" + CommonUtils.encode(codeContent);
            codeContent = "<br><a href='" + htmlUrl + "'>[点击查看代码片段]</a><br>";
            str = str.replace(matcher.group(0), codeContent);
        }

        return str;
    }

    /**
     * 替换 HTML 文本中的视频为外部链接
     *
     * @param str 原始 HTML 文本
     * @return 处理之后的 HTML 文本
     */
    private static String replaceVideo(String str) {
        Pattern pattern = Pattern.compile(VIDEO_REGEX);
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            str = str.replace(matcher.group(0), "<br><a href = '" + matcher.group(1) + "'>[点击查看视频]</a></br>");
        }
        return str;
    }

    /**
     * 替换 HTML 文本中的引用文本为 blockquote
     *
     * @param str 原始 HTML 文本
     * @return 处理之后的 HTML 文本
     */
    private static String replaceQuote(String str) {
        Pattern p = Pattern.compile(QUOTE_REGEX);
        Matcher m = p.matcher(str);
        while (m.find()) {
            String content = m.group(1).trim();
            for (String regex : HtmlUtil.REGEX_DEVICE_ARRAY) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    content = content.replace(matcher.group(0), "");
                }
            }

            content = content.replaceAll("[(<br>)\\s]*$", "<br>");
            str = str.replace(m.group(0), "<blockquote>" + content + "</blockquote>");
            m = p.matcher(str);
        }

        // 多余的换行
        str = str.replaceAll("</blockquote>(\\s)*(<br>)+", "</blockquote>");
        str = str.replaceAll("(<br>)(\\s)*<blockquote>+", "<blockquote>");
        str = str.replaceAll("[(<br>)\\s]*</blockquote>$", "</blockquote>");
        return str;
    }

    /**
     * 去除 HTML 文本中的 Last Edit 字段
     *
     * @param str 原始 HTML 文本
     * @return 处理之后的 HTML 文本
     */
    private static String replaceLastEdit(String str) {
        // Last Edit
        Pattern p = Pattern.compile("(<br>)*\\[ Last edited by (.*?) on (.*?) at (.*?) \\]");
        Matcher m = p.matcher(str);
        while (m.find()) {
            str = str.replace(m.group(0), "");
        }

        return str;
    }

    /**
     * 去除 HTML 文本中的未被其他方法处理的标签，如去除多余的尾部空行
     *
     * @param str 原始 HTML 文本
     * @return 处理之后的 HTML 文本
     */
    public static String replaceOther(String str) {
        str = str.replaceAll("^(<br>)+", "");
        str = str.replaceAll("(<br>)*$", "");
        return str;
    }

    /**
     * 替换 HTML 文本中的表情为 UBB 代码
     *
     * @param htmlStr 原始的 HTML 文本
     * @return UBB 代码
     */
    public static String replaceQuoteSmiles(String htmlStr) {
        /* Test: 咱能别这样吗？<br><img id = 'face' src='file:///android_asset/faces/smilies_icon18.gif'> */
        Pattern p = Pattern.compile("<img id = 'face' src='file:///android_asset/faces/(.*?)'>");
        Matcher m = p.matcher(htmlStr);
        while (m.find()) {
            String smileStr = m.group(1);
            boolean isExisted = false;
            for (String key : Emoticons.EMOTICONS.keySet()) {
                if (Emoticons.EMOTICONS.get(key).equals(smileStr)) {
                    htmlStr = htmlStr.replace(m.group(0), key);
                    isExisted = true;
                }
            }

            if (!isExisted) htmlStr = htmlStr.replace(m.group(0), "");
        }

        return htmlStr;
    }

    /* END - HTML Format */

    /* START - UBB to HTML */

    /**
     * UBB 代码转换成 HTML 代码
     *
     * @param ubbStr 原始的 UBB 代码文本
     * @return 对应的 HTML 代码文本
     */
    public static String ubbToHtml(String ubbStr) {
        String result = ubbStr.replace("\n", "<br>");
        result = result.replaceAll("\\[b\\]", "<b>");
        result = result.replaceAll("\\[/b\\]", "</b>");

        result = result.replaceAll("\\[quote.*?\\]", "<blockquote>");
        result = result.replaceAll("\\[/quote\\]", "</blockquote>");

        result = result.replaceAll("\\[i\\]", "<i>");
        result = result.replaceAll("\\[/i\\]", "</i>");

        result = result.replaceAll("\\[u\\]", "<u>");
        result = result.replaceAll("\\[/u\\]", "</u>");

        // 链接
        result = replaceUrl(result);

        // 替换表情
        result = replaceEmotion(result);

        // 替换 @
        result = replaceAt(result);

        return result;
    }

    /**
     * 替换 UBB 编码中的 [s] 标签为 HTML 标签
     *
     * @param ubbCodeStr 原始 UBB 文本
     * @return 转换之后的文本
     */
    private static String replaceUBBDel(String ubbCodeStr) {
        String regex = "\\[s\\](.*?)\\[/s\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(ubbCodeStr);
        while (m.find()) {
            System.out.println(m.group());
            ubbCodeStr = ubbCodeStr.replace(m.group(0), "<s>" + m.group(1) + "</s>");
        }

        return ubbCodeStr;
    }

    /**
     * 替换 UBB 编码中的 [url] 标签为 HTML 标签
     *
     * @param ubbCodeStr 原始 UBB 文本
     * @return 转换之后的文本
     */
    private static String replaceUrl(String ubbCodeStr) {
        String regex = "\\[url=(.+?)\\](.+?)\\[/url\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(ubbCodeStr);
        while (m.find()) {
            ubbCodeStr = ubbCodeStr.replace(m.group(0), "<a href='" + m.group(1) + "'>" + m.group(2) + "</a>");
        }
        return ubbCodeStr;
    }

    /**
     * 替换 UBB 编码中的表情标签为 HTML 标签
     *
     * @param ubbCodeStr 原始 UBB 文本
     * @return 转换之后的文本
     */
    private static String replaceEmotion(String ubbCodeStr) {
        String regex = ":(\\S{1,10}?):";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(ubbCodeStr);
        while (m.find()) {
            if (Emoticons.EMOTICONS.keySet().contains(m.group(0))) {
                System.out.println("Match \"" + m.group() +
                        "\"at positions " +
                        m.start() + " - " + (m.end() - 1));
                if (Emoticons.EMOTICONS.get(m.group(0)).startsWith("smilies_"))
                    ubbCodeStr = ubbCodeStr.replace(m.group(0), "<img src=\"../images/smilies/" + Emoticons.EMOTICONS.get(m.group(0)).substring(8) + "\" style='vertical-align: middle;' border=\"0\">");
                else if (Emoticons.EMOTICONS.get(m.group(0)).startsWith("bz_"))
                    ubbCodeStr = ubbCodeStr.replace(m.group(0), "<img src=\"../images/bz/" + Emoticons.EMOTICONS.get(m.group(0)).substring(3) + "\" align=\"absmiddle\" border=\"0\">");
            }
        }

        return ubbCodeStr;
    }

    /**
     * 替换 UBB 编码中的 [@] 标签为 HTML 标签
     *
     * @param ubbCodeStr 原始 UBB 文本
     * @return 转换之后的文本
     */
    private static String replaceAt(String ubbCodeStr) {
        String regex = "\\[@\\](.+?)\\[/@\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(ubbCodeStr);
        while (m.find()) {
            ubbCodeStr = ubbCodeStr.replace(m.group(0),
                    "<a href = \"/profile-username-"
                            + CommonUtils.encode(m.group(1), "GBK")
                            + ".html\" > " + m.group(1) + "</a >");
        }

        return ubbCodeStr;
    }

    /* END - UBB to HTML */

    /* START - Post Message Summary */

    /**
     * 获取回帖内容摘要，去除设备信息，替换引用、图片，删除其他所有 HTML 标签
     *
     * @param html 需要处理的回帖 HTML
     * @return 处理之后的 HTML 文本
     */
    public static String getSummaryOfMessage(String html) {
        if (html == null) html = "";
        html = removeDeviceInfo(html);
        html = html.replaceAll("<blockquote>.*?</blockquote>", "[引用] ");
        html = html.replaceAll("<img.*?>", "[图片]");
        html = html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");

        return html.trim();
    }

    /**
     * 移除回帖内容中的设备信息和开放 API 信息
     *
     * @param message 需要处理的字符串
     * @return 处理之后的 HTML 文本
     */
    private static String removeDeviceInfo(String message) {
        for (String regex : HtmlUtil.REGEX_DEVICE_ARRAY) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            while (matcher.find()) {
                message = message.replace(matcher.group(0), "");
            }
        }

        return message;
    }

    /* END - Post Message Summary */
}
