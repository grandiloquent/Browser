package euphoria.psycho.browser.app;

import java.io.File;

import euphoria.psycho.browser.base.Share;

public class SettingsManager {


    private static final String KEY_VIDEOS_DIRECTORY = "videos_directory";

    private static SettingsManager sInstance;

    public static SettingsManager getInstance() {
        if (sInstance == null) {
            sInstance = new SettingsManager();
        }
        return sInstance;
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
}
