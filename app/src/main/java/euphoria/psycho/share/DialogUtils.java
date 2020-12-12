package euphoria.psycho.share;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;

import euphoria.psycho.browser.app.NativeHelper;

public class DialogUtils {

    public static AlertDialog buildProgressDialog(Activity activity, String title, String content) {
        ProgressBar progressBar = new ProgressBar(activity);
        progressBar.setIndeterminate(true);
        int padding = ViewUtils.dpToPx(activity, 8);

        LinearLayout container = new LinearLayout(activity);
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(padding, padding, padding, padding);


        LinearLayout.LayoutParams progressParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        container.addView(progressBar, progressParams);
        if (content != null) {
            TextView contentTextView = new TextView(activity);
            LinearLayout.LayoutParams layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 1;
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.leftMargin = padding;
            contentTextView.setText(content);
            container.addView(contentTextView, layoutParams);
        }

        AlertDialog.Builder builder = new Builder(activity);
        if (title != null) {
            builder.setTitle(title);
        }
        builder.setView(container);
        builder.setCancelable(false);

        return builder.create();
    }

    public interface Listener {
        void onSuccess(String value);
    }

    public static void openTextContentDialog(Activity activity, String title, Listener listener) {
        EditText editText = new EditText(activity);
        editText.requestFocus();
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    listener.onSuccess(editText.getText().toString());
                    dialogInterface.dismiss();
                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }
}