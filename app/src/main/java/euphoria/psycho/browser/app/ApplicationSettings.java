package euphoria.psycho.browser.app;
import android.os.Bundle;
import android.os.Environment;
import java.io.File;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import euphoria.psycho.browser.R;
public class ApplicationSettings extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.application_preferences);
        EditTextPreference videoDirectory = (EditTextPreference) findPreference("videos_directory");
        videoDirectory.setDialogLayoutResource(R.layout.edit_text_dialog);
        videoDirectory.setDefaultValue(getDefaultVideoDirectory());
        getActivity().setTitle(R.string.settings);
    }
    private String getDefaultVideoDirectory() {
        File videoDirectory = new File(Environment.getExternalStorageDirectory(), "Videos");
        if (!videoDirectory.isDirectory())
            videoDirectory.mkdirs();
        return videoDirectory.getAbsolutePath();
    }
}