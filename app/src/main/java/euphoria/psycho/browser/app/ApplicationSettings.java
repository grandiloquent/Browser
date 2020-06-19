package euphoria.psycho.browser.app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import androidx.preference.PreferenceFragmentCompat;
import euphoria.psycho.browser.R;

public class ApplicationSettings extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.application_preferences);
        getActivity().setTitle("");
    }
}
