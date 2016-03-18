package bit.ihainan.me.bitunionforandroid.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Favorite;
import bit.ihainan.me.bitunionforandroid.models.Follow;
import bit.ihainan.me.bitunionforandroid.models.Post;
import bit.ihainan.me.bitunionforandroid.models.TimelineEvent;
import bit.ihainan.me.bitunionforandroid.ui.assist.SwipeActivity;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;
import bit.ihainan.me.bitunionforandroid.utils.network.ExtraApi;

public class TestActivity extends SwipeActivity {
    private final static String TAG = TestActivity.class.getSimpleName();
    public final static int CHOOSE_PHOTO_TAG = 0;


    // UI references
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // UI references
        message = (TextView) findViewById(R.id.thread_message);

        // 测试收藏接口
        // testFavoriteAPI();

        // 测试关注接口
        // testFollowAPI();

        // 测试搜索接口
        // testSearchAPI();

        // 测试时间轴动态接口
        // testTimelineAPI();

        // 测试发帖回帖子
        testAsync();
    }

    private void testAsync(){
        ExtraApi.getFavoriteList(this, 0, 10, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    // 提取出收藏数据
                    try {
                        List<Favorite> favoriteList = BUApi.MAPPER.readValue(response.get("data").toString(),
                                new TypeReference<List<Favorite>>() {
                                });
                        Log.i(TAG, "getFavoriteList >> " + favoriteList);
                        Log.d(TAG, "isFinishing = " + isFinishing());
                        message.setText("Fuck You");
                    } catch (IOException e) {
                        Log.e(TAG, "解析收藏列表 JSON 数据失败", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析收藏列表 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "获取收藏列表失败", error);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            try {
                BUApi.postNewPost(this, 10610779L, "测试////?&", getFileData(uri), new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        if (isFinishing()) return;
                        Log.i(TAG, "发表带附件帖子成功：" + response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (isFinishing()) return;
                        Log.e(TAG, "发表带附件帖子失败", error);
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "发表带附件帖子 - 构建请求失败", e);
            }
        }
    }

    private byte[] getFileData(Uri data) throws IOException {
        InputStream iStream = getContentResolver().openInputStream(data);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len = 0;
        while ((len = iStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private void testSendPost() {
        try {
            BUApi.postNewPost(this, 10610779L, "测试////?&", null, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    if (isFinishing()) return;
                    Log.i(TAG, "发表普通帖子成功：" + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (isFinishing()) return;
                    Log.e(TAG, "发表普通帖子失败", error);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "发表普通帖子 - 构建请求失败", e);
        }

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, CHOOSE_PHOTO_TAG);
            }
        });
    }

    private void parseEvents(String tag, List<TimelineEvent> events) throws IOException {
        for (TimelineEvent event : events) {
            if (event.type == 1) {
                // 帖子
                Post post = BUApi.MAPPER.readValue(BUApi.MAPPER.writeValueAsString(event.content), Post.class);
                Log.i(TAG, tag + " >> " + post);
            } else if (event.type == 2) {
                // 收藏
                Favorite favorite = BUApi.MAPPER.readValue(BUApi.MAPPER.writeValueAsString(event.content), Favorite.class);
                Log.i(TAG, tag + " >> " + favorite);
            } else if (event.type == 3) {
                // 关注
                Follow follow = BUApi.MAPPER.readValue(BUApi.MAPPER.writeValueAsString(event.content), Follow.class);
                Log.i(TAG, tag + " >> " + follow);
            }
        }
    }

    private void testTimelineAPI() {
        ExtraApi.getSpecialUserTimeline(this, "ihainan", 0, 20, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    // 提取出收藏数据
                    try {
                        List<TimelineEvent> events = BUApi.MAPPER.readValue(response.get("data").toString(),
                                new TypeReference<List<TimelineEvent>>() {
                                });
                        parseEvents("getSpecialUserTimeline", events);
                    } catch (IOException e) {
                        Log.e(TAG, "解析指定用户动态列表 JSON 数据失败", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析指定用户动态列表 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "获取指定用户动态失败", error);
            }
        });

        ExtraApi.getFocusTimeline(this, 0, 20, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    // 提取出收藏数据
                    try {
                        List<TimelineEvent> events = BUApi.MAPPER.readValue(response.get("data").toString(),
                                new TypeReference<List<TimelineEvent>>() {
                                });
                        parseEvents("getFocusTimeline", events);
                    } catch (IOException e) {
                        Log.e(TAG, "解析用户关注列表 JSON 数据失败", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析用户关注列表 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "获取用户关注列表失败", error);
            }
        });
    }

    private void testSearchAPI() {
        /* 主题搜索 */
        ExtraApi.searchThreads(this, "Alpha", 0, 20, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    // 提取出收藏数据
                    try {
                        List<Post> posts = BUApi.MAPPER.readValue(response.get("data").toString(),
                                new TypeReference<List<Post>>() {
                                });
                        Log.i(TAG, "searchThreads >> " + posts);
                    } catch (IOException e) {
                        Log.e(TAG, "解析关注列表 JSON 数据失败", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析关注列表 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "获取关注列表失败", error);
            }
        });

        /* 帖子搜索 */
        ExtraApi.searchPosts(this, "Alpha", 0, 20, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    // 提取出收藏数据
                    try {
                        List<Post> posts = BUApi.MAPPER.readValue(response.get("data").toString(),
                                new TypeReference<List<Post>>() {
                                });
                        Log.i(TAG, "searchPosts >> " + posts);
                    } catch (IOException e) {
                        Log.e(TAG, "解析关注列表 JSON 数据失败", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析关注列表 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "获取关注列表失败", error);
            }
        });
    }

    private void testFollowAPI() {
        /* 添加关注 */
        ExtraApi.addFollow(this, "小猫香蒲", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    Log.d(TAG, "添加关注成功");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "添加关注失败", error);
            }
        });

        /* 获取关注列表 */
        ExtraApi.getFollowingList(this, 0, 10, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    // 提取出收藏数据
                    try {
                        List<Follow> followList = BUApi.MAPPER.readValue(response.get("data").toString(),
                                new TypeReference<List<Follow>>() {
                                });
                        Log.i(TAG, "getFollowingList >> " + followList);
                    } catch (IOException e) {
                        Log.e(TAG, "解析关注列表 JSON 数据失败", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析关注列表 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "获取关注列表失败", error);
            }
        });

        /* 删除关注 */
        ExtraApi.delFollow(this, "小猫香蒲", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    Log.d(TAG, "取消关注成功");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "取消关注失败", error);
            }
        });

        /* 获取关注状态 */
        ExtraApi.getFollowStatus(this, "lanqiang", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    try {
                        Boolean status = response.getBoolean("data");
                        Log.i(TAG, "getFollowStatus >> " + status);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析关注状态 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "解析关注状态失败", error);
            }
        });
    }

    private void testFavoriteAPI() {
        /* 添加收藏 */
        ExtraApi.addFavorite(this, 10610499, "新桌面儿", "小猫香蒲", new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    Log.d(TAG, "添加收藏成功");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "添加收藏失败", error);
            }
        });

        /* 获取收藏列表 */
        ExtraApi.getFavoriteList(this, 0, 10, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    // 提取出收藏数据
                    try {
                        List<Favorite> favoriteList = BUApi.MAPPER.readValue(response.get("data").toString(),
                                new TypeReference<List<Favorite>>() {
                                });
                        Log.i(TAG, "getFavoriteList >> " + favoriteList);
                    } catch (IOException e) {
                        Log.e(TAG, "解析收藏列表 JSON 数据失败", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析收藏列表 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "获取收藏列表失败", error);
            }
        });

        /* 删除收藏 */
        ExtraApi.delFavorite(this, 10610499, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    Log.d(TAG, "删除收藏成功");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "删除收藏失败", error);
            }
        });

        /* 获取收藏状态 */
        ExtraApi.getFavoriteStatus(this, 10610499, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (isFinishing()) return;
                if (ExtraApi.checkStatus(response)) {
                    try {
                        Boolean status = response.getBoolean("data");
                        Log.i(TAG, "getFavoriteStatus >> " + status);
                    } catch (JSONException e) {
                        Log.e(TAG, "解析收藏状态 JSON 数据失败", e);
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isFinishing()) return;
                Log.e(TAG, "获取收藏状态失败", error);
            }
        });
    }
}
