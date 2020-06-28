package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.base.Share;

public class TranslatorHelper {
    static ExecutorService sSingleThreadExecutor;

    public static void youdaoChinese(Activity activity) {
        if (sSingleThreadExecutor == null)
            sSingleThreadExecutor = Executors.newSingleThreadExecutor();
        sSingleThreadExecutor.submit(() -> {
            CharSequence q = Share.getClipboardString();
            if (q == null) return;
            String query = q.toString().trim();
            String result = NativeHelper.youdao(query, true, query.contains(" "));
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Share.setClipboardString(result);
                            dialog.dismiss();
                        })
                        .show();
            });
        });
    }

    public static void google(Activity activity) {
        if (sSingleThreadExecutor == null)
            sSingleThreadExecutor = Executors.newSingleThreadExecutor();
        sSingleThreadExecutor.submit(() -> {
            CharSequence q = Share.getClipboardString();
            if (q == null) return;
            String query = q.toString().trim();
            String result = NativeHelper.google(query, false);
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Share.setClipboardString(result);
                            dialog.dismiss();
                        })
                        .show();
            });
        });
    }

}
