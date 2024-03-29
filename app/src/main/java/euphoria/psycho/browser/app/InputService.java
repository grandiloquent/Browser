package euphoria.psycho.browser.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.file.Shared;
import euphoria.psycho.share.Log;

// InputServiceHelper
public class InputService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;
    private String mCurrentString = "";

    private boolean caps = false;
    private final Pattern mChinese = Pattern.compile("[\\u4e00-\\u9fa5]");

    public static String readAssetAsString(Context context, String assetName) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assetName);

            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            return new String(buffer, StandardCharsets.UTF_8);

        } catch (IOException e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ex) {
                }
            }

        }
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ClipboardManager clipboardManager = getSystemService(ClipboardManager.class);
        clipboardManager.addPrimaryClipChangedListener(() -> {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData == null) return;
            if (clipData.getItemCount() > 0) {
                CharSequence charSequence = clipData.getItemAt(0).getText();
                if (charSequence != null && !mChinese.matcher(charSequence.toString()).find() && !mCurrentString.equals(charSequence.toString())) {
                    mCurrentString = charSequence.toString();
                    if (SampleDownloadActivity.checkLink(mCurrentString)) {
                        try {
                            InputServiceHelper.switchSampleDownloadActivity(InputService.this,mCurrentString);
                        } catch (IOException e) {
                            Intent intent = new Intent(InputService.this, SampleDownloadActivity.class);
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setType("text/plain");
                            intent.putExtra(Intent.EXTRA_TEXT, mCurrentString);
                            InputService.this.startActivity(intent);
                        }
                        return;
                    }
                 if (mCurrentString.startsWith("http://") || mCurrentString.startsWith("https://"))
                        return;
                    Shared.postOnBackgroundThread(() -> {
                        if (mCurrentString.contains(" ")) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(NativeHelper.youdao(mCurrentString, true, true));
                            sb.append("\n");
                            sb.append(NativeHelper.google(mCurrentString, true));
                            sb.append("\n");
                            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, sb.toString()));
                            Shared.postOnMainThread(() -> Toast.makeText(InputService.this, sb.toString(), Toast.LENGTH_LONG).show());

                            return;
                        }
                        String r = NativeHelper.youdao(mCurrentString, true, false);
//                        if (r.trim().length() == 0) {
//                            r = NativeHelper.google(mCurrentString, true);
//                        }
                        String result = r;
                        Shared.postOnMainThread(() -> Toast.makeText(InputService.this, result, Toast.LENGTH_LONG).show());
                    });
                }
            }
        });

    }

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView) getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        // keyboard_sym = new Keyboard(this, R.xml.symbol);
        kv.setKeyboard(keyboard);

        kv.setOnKeyboardActionListener(this);
        return kv;
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {

        InputConnection ic = getCurrentInputConnection();

        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            case 1000: {

                // kv.setKeyboard(keyboard_sym);
                break;
            }
            case 1001: {
                //  kv.setKeyboard(keyboard);
                break;
            }

            default:
                char code = (char) primaryCode;
                if (Character.isLetter(code) && caps) {
                    code = Character.toUpperCase(code);
                }
                ic.commitText(String.valueOf(code), 1);
        }

    }

    @Override
    public void onPress(int primaryCode) {

        Log.e("SimpleKeyboard", "Hello3 " + primaryCode);

    }

    @Override
    public void onRelease(int primaryCode) {
    }

    @Override
    public void onText(CharSequence text) {

        Log.e("SimpleKeyboard", "Hello2 " + text);
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeUp() {
    }
}
