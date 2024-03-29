package euphoria.psycho.browser.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import euphoria.psycho.browser.R;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.share.Log;

public class SampleDownloadActivity extends Activity {
    private static String youtubeLink;
    private LinearLayout mainLayout;
    private ProgressBar mainProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_download);
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        mainProgressBar = (ProgressBar) findViewById(R.id.prgrBar);
        // Check how it was started and if we can get the youtube link
        if (savedInstanceState == null && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {
            String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (checkLink(ytLink)) {
                youtubeLink = ytLink;
                // We have a valid link
                getYoutubeDownloadUrl(youtubeLink);
            } else {
                Toast.makeText(this, R.string.error_no_yt_link, Toast.LENGTH_LONG).show();
                finish();
            }
        } else if (savedInstanceState != null && youtubeLink != null) {
            getYoutubeDownloadUrl(youtubeLink);
        } else {
            EditText editText = new EditText(this);


            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("YouTube视频链接")
                    .setView(editText)
                    .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                        String ytLink = editText.getText().toString();
                        if (checkLink(ytLink)) {
                            youtubeLink = ytLink;
                            Log.e("TAG/", ytLink);
                            getYoutubeDownloadUrl(ytLink);
                        } else {
                            finish();
                        }
                    }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                        dialogInterface.dismiss();
                        finish();
                    })
                    .create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
            //            CharSequence c = ContextUtils.getClipboardString();
//            String ytLink = c != null ? c.toString() : "";
//            if (checkLink(ytLink)) {
//                youtubeLink = ytLink;
//                getYoutubeDownloadUrl(ytLink);
//            } else {
//                finish();
//            }
        }
    }

   public static boolean checkLink(String ytLink) {
        return ytLink != null
                && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="));
    }

    private void getYoutubeDownloadUrl(String youtubeLink) {
        new YouTubeExtractor(this) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                mainProgressBar.setVisibility(View.GONE);
                if (ytFiles == null) {
                    Toast.makeText(SampleDownloadActivity.this, youtubeLink, Toast.LENGTH_LONG).show();
                    // Something went wrong we got no urls. Always check this.
                    finish();
                    return;
                }
                // Iterate over itags
                for (int i = 0, itag; i < ytFiles.size(); i++) {
                    itag = ytFiles.keyAt(i);
                    // ytFile represents one file with its url and meta data
                    YtFile ytFile = ytFiles.get(itag);
                    // Just add videos in a decent format => height -1 = audio
                    if (ytFile.getFormat().getHeight() == -1 || ytFile.getFormat().getHeight() >= 360) {
                        addButtonToMainLayout(vMeta.getTitle(), ytFile);
                    }
                }
            }

        }.extract(youtubeLink, true, false);
    }

    private void addButtonToMainLayout(final String videoTitle, final YtFile ytfile) {
        // Display some buttons and let the user choose the format
        String btnText = (ytfile.getFormat().getHeight() == -1) ? "Audio " +
                ytfile.getFormat().getAudioBitrate() + " kbit/s" :
                ytfile.getFormat().getHeight() + "p";
        btnText += (ytfile.getFormat().isDashContainer()) ? " dash" : "";
        Button btn = new Button(this);
        btn.setText(btnText);
        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename;
                if (videoTitle.length() > 55) {
                    filename = videoTitle.substring(0, 55) + "." + ytfile.getFormat().getExt();
                } else {
                    filename = videoTitle + "." + ytfile.getFormat().getExt();
                }
                filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
                downloadFromUrl(ytfile.getUrl(), videoTitle, filename);
                finish();
            }
        });
        mainLayout.addView(btn);
    }

    private void downloadFromUrl(String youtubeDlUrl, String downloadTitle, String fileName) {
        ContextUtils.setClipboardString(youtubeDlUrl);
        
//        Uri uri = Uri.parse(youtubeDlUrl);
//        DownloadManager.Request request = new DownloadManager.Request(uri);
//        request.setTitle(downloadTitle);
//        request.allowScanningByMediaScanner();
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
//        manager.enqueue(request);
    }
}