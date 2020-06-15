package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.util.List;

import androidx.core.util.Pair;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet;
import euphoria.psycho.browser.app.BottomSheet.OnClickListener;
import euphoria.psycho.browser.app.SampleDownloadActivity;
import euphoria.psycho.browser.app.ServerActivity;
import euphoria.psycho.browser.app.TwitterHelper;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;

public class FileHelper {
    public static void showBottomSheet(Activity activity, Pair[] items) {
        new BottomSheet(activity)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClicked(Pair<Integer, String> item) {
                        switch (item.first) {
                            case R.drawable.ic_twitter:
                                extractTwitterVideo(activity);
                                break;
                            case R.drawable.ic_youtube:
                                startYouTube(activity);
                                break;
                            case R.drawable.ic_film:
                                startVideoServer(activity);
                                break;
                        }
                    }
                })
                .showDialog(items);

    }

    public static void startVideoServer(Activity activity) {
        Intent intent = new Intent(activity, ServerActivity.class);
        activity.startActivity(intent);
    }

    public static void downloadFromUrl(Context context, String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public static void startYouTube(Activity activity) {
        Intent intent = new Intent(activity, SampleDownloadActivity.class);
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

    public static Pair[] createBottomSheetItems(Context context) {
        return new Pair[]{
                Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                Pair.create(R.drawable.ic_sd_storage, context.getString(R.string.sd_storage)),
                Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.video_server)),
                Pair.create(R.drawable.ic_film, context.getString(R.string.video_server)),
                Pair.create(R.drawable.ic_twitter, context.getString(R.string.twitter)),
                Pair.create(R.drawable.ic_youtube, context.getString(R.string.youtube)),
        };
    }
}
