package euphoria.psycho.browser.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.concurrent.TransferQueue;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.file.TranslatorHelper;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.Log;
import euphoria.psycho.share.StringUtils;
import euphoria.psycho.share.ThreadUtils;


public class FloatingService extends Service {
    public static boolean isStarted = false;

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;

    private int[] images;
    private int imageIndex = 0;


    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 500;
        layoutParams.height = 500;
        layoutParams.x = 300;
        layoutParams.y = 300;

        images = new int[]{
                R.drawable.image_01,
                R.drawable.image_02,
                R.drawable.image_03,
                R.drawable.image_04,
                R.drawable.image_05,
        };

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.image_display, null);
            displayView.setOnTouchListener(new FloatingOnTouchListener());
            //displayView.findViewById(R.id.image_display_imageview);
            displayView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    CharSequence charSequence = ContextUtils.getClipboardString();
//                    if (charSequence == null) return;
//
//                    String query = charSequence.toString().trim();
//                    Log.e("TAG/", "[FloatingService]: onClick" + query);
//                    ThreadUtils.postOnBackgroundThread(() -> {
//                        String result = NativeHelper.youdao(query, true, query.contains(" "));
//                        ThreadUtils.postOnMainThread(() -> {
//                            Toast.makeText(FloatingService.this, result, Toast.LENGTH_LONG).show();
//                        });
//                    });

                    Intent act=new Intent(FloatingService.this, TransparentActivity.class);
                    act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(act);


                }
            });
            windowManager.addView(displayView, layoutParams);

        }
    }

    public static class TransparentActivity extends Activity {
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            TranslatorHelper.youdaoChinese(this);
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
