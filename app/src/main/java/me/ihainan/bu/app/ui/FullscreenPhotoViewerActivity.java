package me.ihainan.bu.app.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.utils.CommonUtils;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenPhotoViewerActivity extends Activity {
    // TAG
    private final static String TAG = FullscreenPhotoViewerActivity.class.getSimpleName();
    public final static String IMAGE_URL_TAG = "_IMAGE_URL_TAG";
    private final static int PERMISSIONS_REQUEST_READ_FILES = 1;

    // UI
    private PhotoView mImageView;
    private PhotoViewAttacher mAttacher;

    // Data
    private String mImageURL;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen_photo_viewer);

        mVisible = true;
        View mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.iv_photo);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        // Get Image URL
        mImageURL = getIntent().getExtras().getString(IMAGE_URL_TAG, null);

        // ImageView
        mImageView = findViewById(R.id.iv_photo);
        Picasso.with(FullscreenPhotoViewerActivity.this).load(mImageURL).into(mImageView, new Callback() {
            @Override
            public void onSuccess() {
                mAttacher = new PhotoViewAttacher(mImageView);

                mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                    @Override
                    public void onPhotoTap(View view, float x, float y) {
                        toggle();
                    }

                    @Override
                    public void onOutsidePhotoTap() {

                    }
                });

                registerForContextMenu(mImageView);
                mAttacher.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        // Download Image
                        v.showContextMenu();
                        return true;
                    }
                });
            }

            @Override
            public void onError() {
                String message = "下载图片失败，请检查网络或者稍后重试";
                String debugMessage = message + " " + mImageURL;
                Log.e(TAG, debugMessage);
                CommonUtils.showDialog(FullscreenPhotoViewerActivity.this, getString(R.string.error_title), message);
            }
        });
    }

    private final Target mDownloadTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (mDownloadDialog != null) mDownloadDialog.dismiss();

            // 创建文件
            File pictureFile = CommonUtils.getOutputMediaFile(FullscreenPhotoViewerActivity.this);
            if (pictureFile == null) {
                String message = getString(R.string.error_no_storage_permission);
                String debugMessage = message + " - " + mImageURL;
                CommonUtils.showDialog(FullscreenPhotoViewerActivity.this, getString(R.string.error_title), debugMessage);
                Log.e(TAG, debugMessage);
                return;
            }

            // 压缩并写入到文件中
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(pictureFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();
                Toast.makeText(FullscreenPhotoViewerActivity.this, getString(R.string.success_download_image), Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                String message = getString(R.string.error_storage_file_not_found);
                String debugMessage = message + " - " + mImageURL;
                CommonUtils.showDialog(FullscreenPhotoViewerActivity.this, getString(R.string.error_title), debugMessage);
                Log.e(TAG, debugMessage, e);
                return;
            }

            // 保存到图库
            CommonUtils.updateGallery(FullscreenPhotoViewerActivity.this, pictureFile);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            if (mDownloadDialog != null) mDownloadDialog.dismiss();

            String message = getString(R.string.error_download_image);
            String debugMessage = message + " - " + mImageURL;
            CommonUtils.showDialog(FullscreenPhotoViewerActivity.this, getString(R.string.error_title), debugMessage);
            Log.e(TAG, debugMessage);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            onBitmapFailed(placeHolderDrawable);
        }
    };

    // 菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // super.onCreateContextMenu(menu, v, menuInfo);
        if (v.equals(mImageView)) {
            menu.add(0, 1, Menu.NONE, "保存图片");
            menu.add(0, 2, Menu.NONE, "复制地址");
            menu.add(0, 3, Menu.NONE, "取消");

        }
    }

    private ProgressDialog mDownloadDialog;

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (item.getGroupId() == 0) {
            switch (item.getItemId()) {
                case 1:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(FullscreenPhotoViewerActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(FullscreenPhotoViewerActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSIONS_REQUEST_READ_FILES);
                        } else {
                            mDownloadDialog = ProgressDialog.show(FullscreenPhotoViewerActivity.this, "",
                                    getString(R.string.downloading_image), true);
                            mDownloadDialog.show();
                            Picasso.with(this).load(mImageURL).into(mDownloadTarget);
                        }
                    } else {
                        mDownloadDialog = ProgressDialog.show(FullscreenPhotoViewerActivity.this, "",
                                getString(R.string.downloading_image), true);
                        mDownloadDialog.show();
                        Picasso.with(this).load(mImageURL).into(mDownloadTarget);
                    }

                    break;
                case 2:
                    ClipData clipData = ClipData.newPlainText("Email", mImageURL);
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    break;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_FILES:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mDownloadDialog = ProgressDialog.show(FullscreenPhotoViewerActivity.this, "",
                            getString(R.string.downloading_image), true);
                    mDownloadDialog.show();
                    Picasso.with(this).load(mImageURL).into(mDownloadTarget);
                } else {
                    Toast.makeText(this, getString(R.string.error_no_permission), Toast.LENGTH_LONG).show();
                }
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        mImageView.setVisibility(View.VISIBLE);

        // Hide UI first
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
