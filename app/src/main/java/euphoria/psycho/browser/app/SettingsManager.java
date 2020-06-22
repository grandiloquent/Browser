package euphoria.psycho.browser.app;

import android.content.SharedPreferences;

import java.io.File;

import euphoria.psycho.browser.base.Share;
import euphoria.psycho.browser.file.FileHelper;

public class SettingsManager {


    private static final String KEY_LAST_ACCESS_DIRECTORY = "last_access_directory";
    private static final String KEY_SORT_DIRECTION = "sort_direction";
    private static final String KEY_SORT_TYPE = "sort_type";
    private static final String KEY_VIDEOS_DIRECTORY = "videos_directory";
    private static final String KEY_DISPLAY_HIDDEN_FILES = "display_hidden_files";

    private static SettingsManager sInstance;

    public static SettingsManager getInstance() {
        if (sInstance == null) {
            sInstance = new SettingsManager();
        }
        return sInstance;
    }

    public String getLastAccessDirectory() {
        return Share.getAppSharedPreferences().getString(KEY_LAST_ACCESS_DIRECTORY, null);
    }

    public void setLastAccessDirectory(String path) {
        Share.getAppSharedPreferences().edit().putString(KEY_LAST_ACCESS_DIRECTORY, path).apply();
    }

    public int getSortDirection() {
        return Share.getAppSharedPreferences().getInt(KEY_SORT_DIRECTION, FileHelper.SORT_BY_ASCENDING);
    }

    public int getSortType() {
        return Share.getAppSharedPreferences().getInt(KEY_SORT_TYPE, FileHelper.SORT_BY_NAME);
    }

    public String getVideoDirectory() {

        String videoDirectory = Share.getAppSharedPreferences().getString(KEY_VIDEOS_DIRECTORY, null);
        if (videoDirectory == null) {
            return Share.getExternalStoragePath("Videos");
        }
        File dir = new File(videoDirectory);
        if (dir.isDirectory()) {
            return videoDirectory;
        }
        return Share.getExternalStoragePath("Videos");
    }


    public void setSortTypeAndDirection(int sortType, int sortDirection) {
        Share.getAppSharedPreferences()
                .edit()
                .putInt(KEY_SORT_TYPE, sortType)
                .putInt(KEY_SORT_DIRECTION, sortDirection)
                .apply();
    }

    public boolean getDisplayHiddenFiles() {
        return Share.getAppSharedPreferences().getBoolean(KEY_DISPLAY_HIDDEN_FILES, false);
    }
}
