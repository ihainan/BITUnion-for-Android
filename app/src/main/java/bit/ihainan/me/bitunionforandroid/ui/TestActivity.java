package bit.ihainan.me.bitunionforandroid.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.LinearLayout;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.utils.HtmlUtil;

public class TestActivity extends AppCompatActivity {

    // UI references
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // UI references
        webView = (WebView) findViewById(R.id.thread_message);

        // Add elements
        HtmlUtil htmlUtil = new HtmlUtil("校再见吧，我现在真的对你没什么感情，不爱你也不恨你，我会想念我的同学朋友们，但我真的对你没感觉。贵校再见吧，我现在真的对你没什么感情，不爱你也不恨你，我会想念我的同学朋友们，但我真的对你没感觉。<br/>\n" +
                "<blockquote>\n" +
                "校再见吧，我现在真的对你没什么感情，不爱你也不恨你，我会想念我的同学朋友们，但我真的对你没感觉。贵校再见吧，我现在真的对你没什么感情，不爱你也不恨你，我会想念我的同学朋友们，但我真的对你没感觉。\n" +
                "<cite> &emsp;&emsp;——lanqiang</cite>");
        webView.loadData(htmlUtil.makeAll(), "text/html; charset=UTF-8", null);

    }
}
