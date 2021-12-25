package euphoria.psycho.browser.app;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.file.Shared;



public class FloatingService extends Service {
    public static boolean isStarted = false;

    private TextView mWords;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private boolean mIsShown = false;
    private View displayView;
    private View mWrapper;

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
        //
        layoutParams.height = -2;
        layoutParams.width = -2;


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
            View mOk = displayView.findViewById(R.id.ok);
            mWords = displayView.findViewById(R.id.words);
            mWrapper = displayView.findViewById(R.id.wrapper);
            mWords.setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    String value = mWords.getText().toString();
                    Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
                    Shared.postOnBackgroundThread(() -> {
                        String result = NativeHelper.youdao(value, true, value.contains(" "));
                        Shared.postOnMainThread(() -> {
                            mWords.setText("");
                            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                        });
                    });
                    return true;
                }
                return false;
            });
            mOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsShown) {
                        mWords.setText("");
                        mWrapper.setVisibility(View.GONE);
                        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                        layoutParams.width = -2;

                    } else {
                        mWrapper.setVisibility(View.VISIBLE);
                        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
                        layoutParams.width = -1;
                    }
                    windowManager.updateViewLayout(displayView, layoutParams);
                    mIsShown = !mIsShown;
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

//                    Intent act=new Intent(FloatingService.this, TransparentActivity.class);
//                    act.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(act);


                }
            });
            windowManager.addView(displayView, layoutParams);

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
