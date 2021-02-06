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
      /*  EditText editText = new EditText(activity);


        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("有道英文翻译为中文")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    String string = editText.getText().toString();

                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
        */
        EditText editText = new EditText(activity);


        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("有道英文翻译为中文")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    String string = editText.getText().toString();
                    ThreadUtils.postOnBackgroundThread(() -> {

                        String query = string.trim();
                        String result = NativeHelper.youdao(query, true, query.contains(" "));
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
                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();


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


        EditText editText = new EditText(activity);


        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("谷歌英文翻译中文")
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    String string = editText.getText().toString();
                    ThreadUtils.postOnBackgroundThread(() -> {

                        String query = string.trim();
                        String result = NativeHelper.google(query, false);
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
                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();


        DialogUtils.openTextContentDialog(activity, "翻译中文文本", query -> {
            ThreadUtils.postOnBackgroundThread(() -> {

                String result = NativeHelper.google(query, false);
                activity.runOnUiThread(() -> {
                    new AlertDialog.Builder(activity)
                            .setMessage(result)
                            .setPositiveButton(android.R.string.ok, (i, which) -> {
                                ContextUtils.setClipboardString(result);
                                i.dismiss();
                            })
                            .show();
                });
            });
        });
    }

}
