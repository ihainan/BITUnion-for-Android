package bit.ihainan.me.bitunionforandroid.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.QuoteSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

import org.xml.sax.XMLReader;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.assist.CustomSpan;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.HtmlUtil;

public class TestActivity extends AppCompatActivity {
    private final static String TAG = TestActivity.class.getSimpleName();
    // UI references
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // UI references
        message = (TextView) findViewById(R.id.thread_message);

        // Add elements
        String html = "<a href = 'http://www.baidu.com'>http://www.baidu.com</a> <br> 姑娘详情：软妹子，1992年北京大兴人，首经贸本科毕业，在创业企业认真做品牌，不给国家添乱，身高160的小型美女，体重分分钟浮动20斤跟玩似的，太瘦了卖不上价钱。<blockquote>这是一个引用</blockquote> 属性：致力于收集一枚靠谱的理工男哥哥，善良温柔，动手能力强，刚入社会，没来得及黑化，有点孩子气，但不是熊孩子。厨艺还有进步空间，红烧肉爱好者，妈妈说：爱吃肉的姑娘运气不会太差，不挑食好养活。容易咆哮，容易微笑，一点就炸，一哄就好，只有少女心，没有少女病，一句话：不矫情。爱跑步，想在24岁跑一个全马，生命在于折腾嘛<br />理想型伴侣：性别男，身高178+，白白胖胖，要帅不帅的样子就好，爱笑爱运动爱聊天不爱生气的，会留灯到最后，年龄不大于86年，特别待见man一点的，大男人都是从保护女人开始（认真脸），大男人笑起来，铁血男儿到不行，姑娘实心眼，会玩命对你好的，她说恋爱是过命的交情。<br />同学们，她比较赶时间，你们看准了一起上，不要不好意思下手啊~<br />随缘什么的太扯了，真爱也禁不起等待，时光静谧美好，我们一同领教<br />(以上文字均为妹子写的)<br />有意者加微信: A1045204188<br />后附王道两张。";
        SpannableString spannableString = new SpannableString(Html.fromHtml(new HtmlUtil(html).makeAll()));
        message.setLineSpacing(5, 1.2f);
        message.setMovementMethod(new CustomSpan.LinkTouchMovementMethod());
        CustomSpan.replaceQuoteSpans(this, spannableString);
        CustomSpan.replaceClickableSpan(this, spannableString);
        message.setText(spannableString);
    }

}
