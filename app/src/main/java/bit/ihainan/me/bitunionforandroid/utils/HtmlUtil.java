package bit.ihainan.me.bitunionforandroid.utils;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert HTML to View
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
        // addCss("body{overflow-wrap:break-word;word-wrap:break-word;-ms-word-break:break-all;word-break:break-all;word-break:break-word;-ms-hyphens:auto;-moz-hyphens:auto;-webkit-hyphens:auto;hyphens:auto;line-height:1.6}blockquote{background:#f9f9f9;border-left:10px solid #ccc;margin:1.5em 0px;padding:.5em 10px;quotes:\"\\201C\"\"\\201D\"\"\\2018\"\"\\2019\"}blockquote:before{color:#006FDA;content:open-quote;font-size:4em;line-height:.1em;margin-right:.25em;vertical-align:-.4em}blockquote p{display:inline}blockquote cite{color:#006FDA;font-weight:700} img{width:100%;height:auto}");
        // addHead(getCss());
        // addHead("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/style.css\" />");
        // addJavascript("");
        // addHead(getJavascript());
        return mHtmlAll.append(getHead()).append(mBody).append("</body></html>").toString();
    }

    private static final String QUOTE_HEAD = "<br><br><center><table[^>]+><tr><td>&nbsp;&nbsp;引用(?:\\[<a href='[\\w\\.&\\?=]+?'>查看原帖</a>])*?.</td></tr><tr><td><table.{101,102}bgcolor='ALTBG2'>";
    private static final String QUOTE_TAIL = "</td></tr></table></td></tr></table></center><br>";
    private static final String QUOTE_REGEX = QUOTE_HEAD
            + "(((?!<br><br><center><table border=)[\\w\\W])*?)" + QUOTE_TAIL;

    private String parseLocalImage(String imgUrl) {
        // 检查是否为本地表情文件
        Pattern p = Pattern.compile("\\.\\./images/(smilies|bz)/(.+?)\\.gif$");
        Matcher m = p.matcher(imgUrl);
        if (m.find()) {
            // Use local assets for emotions
            Log.d(TAG, "parseLocalImage >> " + imgUrl + " - " + "file:///android_asset/" + m.group(1) + "_" + m.group(2) + ".gif");
            imgUrl = "file:///android_asset/" + m.group(1) + "_" + m.group(2) + ".gif";
        }
        return imgUrl;
    }

    private void processBody() {
        // 单引号双引号
        mBody = mBody.replaceAll("\"", "'");

        // Open API 标志
        if (mBody.contains("From BIT-Union Open API Project"))
            mBody = mBody.replaceAll("<br/><span id='id_open_api_label'>..:: <a href=http://www.bitunion.org>From BIT-Union Open API Project</a> ::..<br/>", "");

        // 换行
        mBody = mBody.replaceAll("<br />\r\n<br />", "<br />").replaceAll("<br />", "<br /><br />");

        // 图片
        Pattern p = Pattern.compile("<img src='([^>']+)'[^>]*(width>)?[^>]*'>");
        Matcher m = p.matcher(mBody);
        while (m.find()) {
            String url = parseLocalImage(m.group(1));
            if (url.contains("file:///android_asset/"))
                url = "<img id = 'face' src='" + url + "'>";
            else
                url = "<img src='" + url + "'>";
            mBody = mBody.replace(m.group(0), url);
            m = p.matcher(mBody);
        }


        // 引用，参考 BUapp
        p = Pattern.compile(QUOTE_REGEX);
        m = p.matcher(mBody);
        while (m.find()) {
            mBody = mBody.replace(m.group(0), "<blockquote>" + m.group(1).trim() + "</blockquote>");
            m = p.matcher(mBody);
        }

        // 测试
        // mBody = mBody + "<blockquote>校再见吧，我现在真的对你没什么感情，不爱你也不恨你，我会想念我的同学朋友们，但我真的对你没感觉。贵校再见吧，我现在真的对你没什么感情，不爱你也不恨你，我会想念我的同学朋友们，但我真的对你没感觉。 <cite> &emsp;&emsp;——lanqiang</cite>";
    }
}
