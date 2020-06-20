package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.util.Pair;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet;
import euphoria.psycho.browser.app.BottomSheet.OnClickListener;
import euphoria.psycho.browser.app.FunctionsMenu;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.app.SampleDownloadActivity;
import euphoria.psycho.browser.app.ServerActivity;
import euphoria.psycho.browser.app.SettingsActivity;
import euphoria.psycho.browser.app.TwitterHelper;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FileHelper {
    public static final int TYPE_APK = 0;
    public static final int TYPE_EXCEL = 1;
    public static final int TYPE_FOLDER = 2;
    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_MUSIC = 4;
    public static final int TYPE_OTHERS = 5;
    public static final int TYPE_PDF = 6;
    public static final int TYPE_PPS = 7;
    public static final int TYPE_TEXT = 8;
    public static final int TYPE_VCF = 9;
    public static final int TYPE_VIDEO = 10;
    public static final int TYPE_WORD = 11;
    public static final int TYPE_ZIP = 12;

    private static boolean sIsHasSD;

    /*
    ["apk",
"excel",
"folder",
"image",
"music",
"others",
"pdf",
"pps",
"text",
"vcf",
"video",
"word",
"zip"]
    * */

    static ExecutorService sSingleThreadExecutor;

    public static void initialize(Context context) {
        sIsHasSD = getExternalStoragePath(context, true) != null;
    }

    public static Pair[] createBottomSheetItems(Context context) {
        if (sIsHasSD) {
            return new Pair[]{
                    Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                    Pair.create(R.drawable.ic_sd_storage, context.getString(R.string.sd_storage)),
                    Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                    Pair.create(R.drawable.ic_info, context.getString(R.string.directory_info)),
                    Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
                    Pair.create(R.drawable.ic_more_vert, context.getString(R.string.more))
            };
        } else {
            return new Pair[]{
                    Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                    Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                    Pair.create(R.drawable.ic_info, context.getString(R.string.directory_info)),
                    Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
                    Pair.create(R.drawable.ic_more_vert, context.getString(R.string.more))
            };
        }
    }

    public static Pair[] createFunctionsMenuItems(Context context) {
        return new Pair[]{
                Pair.create(R.drawable.ic_film, context.getString(R.string.video_server)),
                Pair.create(R.drawable.ic_twitter, context.getString(R.string.twitter)),
                Pair.create(R.drawable.ic_youtube, context.getString(R.string.youtube)),
                Pair.create(R.drawable.ic_translate, context.getString(R.string.youdao)),
                Pair.create(R.drawable.ic_g_translate, context.getString(R.string.google)),
        };
    }

    public static void createFuntionsMenu(Activity activity, FileManager fileManager) {
        FunctionsMenu functionsMenu = new FunctionsMenu(activity, fileManager.getView(), new OnClickListener() {
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
                    case R.drawable.ic_translate:
                        youdaoChinese(activity);
                        break;
                    case R.drawable.ic_g_translate:
                        google(activity);
                        break;
                }
            }
        });
        functionsMenu.showDialog(createFunctionsMenuItems(activity));
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

    public static void extractTwitterVideo(Activity activity) {
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMessage(activity.getText(R.string.extracting));
        dialog.show();

        new Thread(() -> {
            try {
                CharSequence twitterUrl = Share.getClipboardString();
                if (twitterUrl != null) {
                    String id = Share.substringAfterLast(twitterUrl.toString(), "/");
                    if (Share.isDigits(id)) {
                        List<TwitterVideo> twitterVideos = TwitterHelper.extractTwitterVideo(id);
                        activity.runOnUiThread(() -> {
                            dialog.dismiss();
                            TwitterHelper.showDialog(twitterVideos, activity);
                        });
                    }
                }
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    dialog.dismiss();
                    Share.showExceptionDialog(activity, e);
                });
            }
        }).start();
    }

    public static long getFileSize(File file) {
        if (file.isDirectory()) {
            return file.listFiles().length;
        }
        return file.length();
    }

    public static int getFileType(File file) {
        if (file.isDirectory()) {
            return TYPE_FOLDER;
        }
        String extension = Share.substringAfterLast(file.getName(), '.');

        switch (extension) {
            case "apk":
                return TYPE_APK;
            case "excel":
                return TYPE_EXCEL;
            case "bmp":
            case "gif":
            case "jpg":
            case "png":
            case "webp":
            case "heic":
            case "heif":
            case "jpeg":
                return TYPE_IMAGE;
            case "m4a":
            case "aac":
            case "flac":
            case "gsm":
            case "mid":
            case "xmf":
            case "mxmf":
            case "rtttl":
            case "rtx":
            case "ota":
            case "imy":
            case "mp3":
            case "wav":
            case "ogg":
                return TYPE_MUSIC;
            case "pdf":
                return TYPE_PDF;
            case "pps":
                return TYPE_PPS;
            case "txt":
            case "html":
            case "sql":
            case "c":
            case "css":
            case "cs":
            case "cc":
            case "h":
            case "js":
                return TYPE_TEXT;
            case "vcf":
                return TYPE_VCF;
            case "3gp":
            case "mp4":
            case "webm":
            case "ts":
            case "mkv":
                return TYPE_VIDEO;
            case "word":
                return TYPE_WORD;
            case "zip":
                return TYPE_ZIP;
            default:
                return TYPE_OTHERS;
        }
    }

    public static void openUrl(Activity activity, FileItem fileItem) {
        String url = fileItem.getUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(url)),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        Share.substringAfterLast(url, '.')
                ));
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.open)));
    }

    public static void showBottomSheet(Activity activity, Pair[] items, FileManager fileManager) {
        BottomSheet bottomSheet = new BottomSheet(activity)
                .setOnClickListener(item -> {
                    switch (item.first) {
                        case R.drawable.ic_storage:
                            fileManager.openDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
                            break;
                        case R.drawable.ic_sd_storage:

                            fileManager.openDirectory(getExternalStoragePath(activity, true));

                            break;
                        case R.drawable.ic_settings:
                            Intent settingsActivity = new Intent(activity, SettingsActivity.class);
                            activity.startActivity(settingsActivity);
                            break;
                        case R.drawable.ic_info:
                            showDirectoryInfo(activity, fileManager.getDirectory());
                            break;
                        case R.drawable.ic_more_vert:
                            createFuntionsMenu(activity, fileManager);
                            break;
                    }
                    fileManager.setBottomSheet(null);
                });
        fileManager.setBottomSheet(bottomSheet);
        bottomSheet.showDialog(items);

    }

    public static void startVideoServer(Activity activity) {
        Intent intent = new Intent(activity, ServerActivity.class);
        activity.startActivity(intent);
    }

    public static void startYouTube(Activity activity) {
        Intent intent = new Intent(activity, SampleDownloadActivity.class);
        activity.startActivity(intent);
    }

    private static String getExternalStoragePath(Context context, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (is_removable == removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void google(Activity activity) {
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

    private static void showDirectoryInfo(Activity activity, String directory) {


//            Log.e("TAG/", "Debug: showDirectoryInfo, \n" + Files.walk(new File(Environment.getExternalStorageDirectory(),"Videos").toPath()).mapToLong(p -> p.toFile().length()).sum());
        File[] files = new File(directory).listFiles(File::isDirectory);
        List<Pair<Long, String>> pairList = new ArrayList<>();
        for (File f : files) {
            pairList.add(Pair.create(NativeHelper.dirSize(f.getAbsolutePath()), f.getName()));
        }
        Collections.sort(pairList, (o1, o2) -> {


            long a = o1.first - o2.first;
            if (a > 0) return -1;
            if (a < 0) return 1;
            return 0;
        });
        StringBuilder stringBuilder = new StringBuilder();
        for (Pair<Long, String> p : pairList) {
            stringBuilder.append(Share.formatFileSize(p.first))
                    .append(" = ")
                    .append(p.second)
                    .append('\n');
        }
        new AlertDialog.Builder(activity)
                .setMessage(stringBuilder.toString())
                .show();

    }

    private static void youdaoChinese(Activity activity) {
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
}
