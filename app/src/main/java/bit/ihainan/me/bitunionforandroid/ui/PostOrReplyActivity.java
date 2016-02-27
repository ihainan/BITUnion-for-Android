package bit.ihainan.me.bitunionforandroid.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import bit.ihainan.me.bitunionforandroid.R;

public class PostOrReplyActivity extends AppCompatActivity {

    // UI References
    private EditText mMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_or_reply);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setTitle("发布新帖");

        mMessage = (EditText) findViewById(R.id.new_post_message);
        mMessage.setLineSpacing(10, 1.3f);
        mMessage.setText("本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间，刚进去时是一毛二（硕士进去的话是一毛三），现在是两毛一，之前是在带兵，现在进团部当作训参谋了。这个军衔上升的时间段供你参考。部队现在说实话待遇也是一般，但相比以前是好一些了，也让用手机了（必须要用国产的手机），平时也严禁喝酒什么的了，其实主要是将来看你能不能承受得住那里的环境，一年得有半年的驻外拉练，冬天就住单层的单兵帐篷啥的。两毛三以下的都会面临转业的问题，现在说实话想转个好单位到时候还得找关系，所以这点你也得想清楚。总而言之，我把我能知道的这些都告诉你了，决定权还是在你。\n\n 转业的话就要面临二次就业的问题，这个在我30多岁的时候身边的人都已经稳定下来了，而我很有可能从头再来，所以说这个是比较困难的。在基层部队训练带兵也是一件很苦的事情，学的东西完全没有用，而且还不如军校生，但是现在退出的后果又比较严重，选择两难啊 \n 转业的话就要面临二次就业的问题，这个在我30多岁的时候身边的人都已经稳定下来了，而我很有可能从头再来，所以说这个是比较困难的。在基层部队训练带兵也是一件很苦的事情，学的东西完全没有用，而且还不如军校生，但是现在退出的后果又比较严重，选择两难啊");
    }

}
