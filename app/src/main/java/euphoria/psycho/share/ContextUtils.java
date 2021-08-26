package euphoria.psycho.share;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.os.Environment.DIRECTORY_MUSIC;

public class ContextUtils {
    private static final int DEFAULT_JPEG_QUALITY = 90;
    private static final String TAG = "TAG/" + ContextUtils.class.getSimpleName();
    private static Context sApplicationContext;
    private static ClipboardManager sClipboardManager;
    private static float sPixelDensity = -1f;
    private static int sWidthPixels;


    public static boolean checkSelfPermission(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }


    public static float dpToPixel(float dp) {
        return sPixelDensity * dp;
    }

    public static int dpToPixel(int dp) {
        return Math.round(dpToPixel((float) dp));
    }

    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError ignored) {
            }
        }
        return null;
    }

    /**
     * This is used to ensure that we always use the application context to fetch the default shared
     * preferences. This avoids needless I/O for android N and above. It also makes it clear that
     * the app-wide shared preference is desired, rather than the potentially context-specific one.
     *
     * @return application-wide shared preferences.
     */
    public static SharedPreferences getAppSharedPreferences() {
        return Holder.sSharedPreferences;
    }

    /**
     * Get the Android application context.
     * <p>
     * Under normal circumstances there is only one application context in a process, so it's safe
     * to treat this as a global. In WebView it's possible for more than one app using WebView to be
     * running in a single process, but this mechanism is rarely used and this is not the only
     * problem in that scenario, so we don't currently forbid using it as a global.
     * <p>
     * Do not downcast the context returned by this method to Application (or any subclass). It may
     * not be an Application object; it may be wrapped in a ContextWrapper. The only assumption you
     * may make is that it is a Context whose lifetime is the same as the lifetime of the process.
     */
    public static Context getApplicationContext() {
        return sApplicationContext;
    }

    public static CharSequence getClipboardString() {
        if (sClipboardManager == null)
            sClipboardManager = (ClipboardManager) sApplicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = sClipboardManager.getPrimaryClip();
        if (clip != null && clip.getItemCount() > 0) {
            return clip.getItemAt(0).getText();
        }
        return null;
    }

    public static void setClipboardString(String string) {
        if (sClipboardManager == null)
            sClipboardManager = (ClipboardManager) sApplicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
        sClipboardManager.setPrimaryClip(ClipData.newPlainText(null, string));
    }


    public static String getExternalStorageDirectory() {
        File directory = Environment.getExternalStorageDirectory();
        if (directory != null) {
            return directory.getAbsolutePath();
        }
        directory = sApplicationContext.getExternalFilesDir(DIRECTORY_MUSIC);
        if (directory != null) {
            return StringUtils.substringBefore(directory.getAbsolutePath(), "/Android/");
        }
        return null;
    }

    public static String getExternalStoragePath(String fileName) {
        return Environment.getExternalStorageDirectory() + "/" + fileName;
    }


    public static int getWidthPixels() {
        return sWidthPixels;
    }

    /**
     * Initializes the java application context.
     * <p>
     * This should be called exactly once early on during startup, before native is loaded and
     * before any other clients make use of the application context through this class.
     *
     * @param appContext The application context.
     */
    public static void initApplicationContext(Context appContext) {
        // Conceding that occasionally in tests, native is loaded before the browser process is
        // started, in which case the browser process re-sets the application context.
        assert sApplicationContext == null || sApplicationContext == appContext
                || ((ContextWrapper) sApplicationContext).getBaseContext() == appContext;
        initJavaSideApplicationContext(appContext);
    }

    public static void initialize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)
                context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        sPixelDensity = metrics.density;
        sWidthPixels = metrics.widthPixels;

    }

    /**
     * Return list of all normal files under the given directory, traversing
     * directories recursively.
     *
     * @param exclude ignore dirs with this name, or {@code null} to ignore.
     * @param uid     only return files owned by this UID, or {@code -1} to ignore.
     */
    public static List<ConcreteFile> listFilesRecursive(File startDir, String exclude, int uid) {
        final ArrayList<ConcreteFile> files = new ArrayList<>();
        final LinkedList<File> dirs = new LinkedList<File>();
        dirs.add(startDir);
        while (!dirs.isEmpty()) {
            final File dir = dirs.removeFirst();
            if (Objects.equals(dir.getName(), exclude)) continue;
            final File[] children = dir.listFiles();
            if (children == null) continue;
            for (File child : children) {
                if (child.isDirectory()) {
                    dirs.add(child);
                } else if (child.isFile()) {
                    try {
                        final ConcreteFile file = new ConcreteFile(child);
                        if (uid == -1 || file.stat.st_uid == uid) {
                            files.add(file);
                        }
                    } catch (ErrnoException ignored) {
                    }
                }
            }
        }
        return files;
    }


    public static void showExceptionDialog(Context context, Exception e) {
        TextView textView = new TextView(context);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("## Message")
                .append("\n\n")
                .append(e.getMessage())
                .append("\n\n")
                .append("## StackTrace");

        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            stringBuilder.append(stackTraceElement.toString())
                    .append("\n\n");
        }
        String content = stringBuilder.toString();
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(textView)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContextUtils.setClipboardString(content);
                        dialog.dismiss();
                    }
                })
                .create();
    }//

    /**
     * Only called by the static holder class and tests.
     *
     * @return The application-wide shared preferences.
     */
    private static SharedPreferences fetchAppSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
    }

    private static String getExternalStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
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

    private static void initJavaSideApplicationContext(Context appContext) {
        assert appContext != null;
        // Guard against anyone trying to downcast.
        if (appContext instanceof Application) {
            appContext = new ContextWrapper(appContext);
        }
        sApplicationContext = appContext;
    }

    /**
     * Initialization-on-demand holder. This exists for thread-safe lazy initialization.
     */
    private static class Holder {
        // Not final for tests.
        private static SharedPreferences sSharedPreferences = fetchAppSharedPreferences();
    }

    /**
     * Concrete file on disk that has a backing device and inode. Faster than
     * {@code realpath()} when looking for identical files.
     */
    static class ConcreteFile {
        public final File file;
        public final StructStat stat;

        public ConcreteFile(File file) throws ErrnoException {
            this.file = file;
            this.stat = Os.lstat(file.getAbsolutePath());
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof ConcreteFile) {
                final ConcreteFile f = (ConcreteFile) o;
                return (f.stat.st_dev == stat.st_dev) && (f.stat.st_ino == stat.st_ino);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + (int) (stat.st_dev ^ (stat.st_dev >>> 32));
            result = 31 * result + (int) (stat.st_ino ^ (stat.st_ino >>> 32));
            return result;
        }
    }

}

/*
Array.from($0.querySelectorAll('li a')).filter(i=>i.textContent.indexOf('substr')!==-1).map(i=>`public static String ${i.textContent.substr(i.textContent.lastIndexOf('.')+1)}(String string,char delimiter){ int index=string.indexOf(delimiter); if(index!=-1)return string.substring(0,index); return string;\n}`).join('\n')
*
* */
