package euphoria.psycho.browser.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;

public class FileHelper {
    public static void startVideoServer(Activity activity) {
        Intent intent = new Intent(activity, ServerActivity.class);
        activity.startActivity(intent);
    }

    public static void extractTwitterVideo(Activity activity) {
        new Thread(() -> {
            try {
                CharSequence twitterUrl = Share.getClipboardString();
                if (twitterUrl != null) {
                    String id = Share.substringAfterLast(twitterUrl.toString(), "/");
                    if (Share.isDigits(id)) {
                        List<TwitterVideo> twitterVideos = TwitterHelper.extractTwitterVideo(id);
                        activity.runOnUiThread(() -> {
                            TwitterHelper.showDialog(twitterVideos, activity);
                        });
                    }
                }
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    Share.showExceptionDialog(activity, e);
                });
            }
        }).start();
    }
}
