package euphoria.psycho.browser.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import euphoria.psycho.browser.R;
import euphoria.psycho.share.Log;
import euphoria.psycho.share.ThreadUtils;


public class InputService extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;

    private boolean caps = false;

    @Override
    public void onCreate() {
        super.onCreate();
        ClipboardManager clipboardManager = getSystemService(ClipboardManager.class);
        clipboardManager.addPrimaryClipChangedListener(() -> {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData.getItemCount() > 0) {
                CharSequence charSequence = clipData.getItemAt(0).getText();
                if (charSequence != null) {
                    ThreadUtils.postOnBackgroundThread(() -> {
                        String result = NativeHelper.youdao(charSequence.toString(), true, false);
                        ThreadUtils.postOnMainThread(() -> Toast.makeText(InputService.this, result, Toast.LENGTH_LONG).show());
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
