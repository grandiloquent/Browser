package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.File;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.DialogUtils;
import euphoria.psycho.share.ThreadUtils;

public class TranslatorHelper {

    public static void youdaoChinese(Activity activity) {


        DialogUtils.openTextContentDialog(activity, "翻译英文文本", value -> {
            ThreadUtils.postOnBackgroundThread(() -> {
                String result = NativeHelper.youdao(value, true, value.contains(" "));
                activity.runOnUiThread(() -> {
                    new AlertDialog.Builder(activity)
                            .setMessage(result)
                            .setPositiveButton(android.R.string.ok, (dlg, which) -> {
                                ContextUtils.setClipboardString(result);
                                dlg.dismiss();
                            })
                            .show();
                });
            });

        });
    }

    public static void google(Activity activity) {

        DialogUtils.openTextContentDialog(activity, "翻译中文文本", query -> {
            ThreadUtils.postOnBackgroundThread(() -> {

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
        });
    }

}
