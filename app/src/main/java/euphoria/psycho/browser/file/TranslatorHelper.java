package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;

import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.ThreadUtils;

public class TranslatorHelper {

    public static void youdaoChinese(Activity activity) {
        ThreadUtils.postOnBackgroundThread(() -> {
            CharSequence q = ContextUtils.getClipboardString();
            if (q == null) return;
            String query = q.toString().trim();
            String result = NativeHelper.youdao(query, true, query.contains(" "));
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            ContextUtils.setClipboardString(result);
                            dialog.dismiss();
                        })
                        .show();
            });
        });
    }

    public static void google(Activity activity) {

        ThreadUtils.postOnBackgroundThread(() -> {
            CharSequence q = ContextUtils.getClipboardString();
            if (q == null) return;
            String query = q.toString().trim();
            String result = NativeHelper.google(query, false);
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            ContextUtils.setClipboardString(result);
                            dialog.dismiss();
                        })
                        .show();
            });
        });
    }

}
