package euphoria.psycho.browser.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.system.ErrnoException;
import android.system.Os;
import android.system.StructStat;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import euphoria.psycho.browser.BuildConfig;

public class Share {
    private static Context sApplicationContext;

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

    public static String getDeviceIP(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        try {
            @SuppressLint("MissingPermission") WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            InetAddress inetAddress = intToInetAddress(wifiInfo.getIpAddress());
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            return null;
        }
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

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};
        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }

    public static String substringAfter(String string, char delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(index + 1);
        return string;
    }

    public static String substringAfter(String string, String delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(index + delimiter.length());
        return string;
    }

    public static String substringAfterLast(String string, char delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(index + 1);
        return string;
    }

    public static String substringAfterLast(String string, String delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(index + delimiter.length());
        return string;
    }

    public static String substringBefore(String string, char delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(0, index);
        return string;
    }

    public static String substringBefore(String string, String delimiter) {
        int index = string.indexOf(delimiter);
        if (index != -1) return string.substring(0, index + delimiter.length());
        return string;
    }

    public static String substringBeforeLast(String string, char delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(0, index);
        return string;
    }

    public static String substringBeforeLast(String string, String delimiter) {
        int index = string.lastIndexOf(delimiter);
        if (index != -1) return string.substring(0, index + delimiter.length());
        return string;
    }

    /**
     * Only called by the static holder class and tests.
     *
     * @return The application-wide shared preferences.
     */
    private static SharedPreferences fetchAppSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(sApplicationContext);
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
     * Return list of all normal files under the given directory, traversing
     * directories recursively.
     *
     * @param exclude ignore dirs with this name, or {@code null} to ignore.
     * @param uid     only return files owned by this UID, or {@code -1} to ignore.
     */
    static List<ConcreteFile> listFilesRecursive(File startDir, String exclude, int uid) {
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
