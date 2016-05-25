package me.ihainan.bu.app.utils.network;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Session;
import me.ihainan.bu.app.utils.BUApplication;


/**
 * Session 更新服务，用于定期更新 Session
 */
public class SessionUpdateService extends IntentService {
    public final static String TAG = SessionUpdateService.class.getSimpleName();

    private final static int UPDATE_DELAY = 20; // 更新间隔，单位为分钟

    public SessionUpdateService() {
        super("SessionUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            boolean shouldReturn = false;

            @Override
            public void run() {
                if (BUApplication.username != null && BUApplication.password != null) {
                    BUApi.tryLogin(getApplicationContext(), BUApplication.username, BUApplication.password,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if (BUApi.checkStatus(response)) {
                                            BUApplication.userSession = BUApi.MAPPER.readValue(response.toString(), Session.class);

                                            if (BUApplication.userSession.credit < 0) {
                                                shouldReturn = true;
                                                return;
                                            } else {
                                                Log.i(TAG, getString(R.string.update_session_success));
                                                BUApplication.saveConfig(SessionUpdateService.this);
                                                return;
                                            }
                                        } else {
                                            shouldReturn = true;
                                            return;
                                        }
                                    } catch (IOException e) {
                                        shouldReturn = true;
                                        return;
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    return;
                                }
                            });

                    if (shouldReturn) timer.cancel();
                }
            }


        }, 1000, UPDATE_DELAY * 60 * 1000);
    }
}
