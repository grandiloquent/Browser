package euphoria.psycho.browser.app;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.ApiCompatibilityUtils;
import euphoria.psycho.browser.settings.SettingsUtils;
import euphoria.psycho.browser.ui.ColorUtils;

public class SettingsActivity extends AppCompatActivity {
    static final String EXTRA_SHOW_FRAGMENT = "show_fragment";
    static final String EXTRA_SHOW_FRAGMENT_ARGUMENTS = "show_fragment_args";
    private static boolean sActivityNotExportedChecked;
    private static SettingsActivity sResumedInstance;

    private boolean mIsNewlyCreated;

    public Fragment getMainFragment() {
        return getSupportFragmentManager().findFragmentById(android.R.id.content);
    }

    public boolean onPreferenceStartFragment(
            PreferenceFragmentCompat caller, Preference preference) {
        startFragment(preference.getFragment(), preference.getExtras());
        return true;
    }

    public void startFragment(String fragmentClass, Bundle args) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClass(this, getClass());
        intent.putExtra(EXTRA_SHOW_FRAGMENT, fragmentClass);
        intent.putExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS, args);
        startActivity(intent);
    }

    private void ensureActivityNotExported() {
        if (sActivityNotExportedChecked) return;
        sActivityNotExportedChecked = true;
        try {
            ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 0);


            if (activityInfo.exported) {
                throw new IllegalStateException("SettingsActivity must not be exported.");
            }
        } catch (NameNotFoundException ex) {

            throw new RuntimeException(ex);
        }
    }

    private void setStatusBarColor() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;

        int statusBarColor =
                ApiCompatibilityUtils.getColor(getResources(), R.color.default_bg_color);
        ApiCompatibilityUtils.setStatusBarColor(getWindow(), statusBarColor);

        boolean needsDarkStatusBarIcons =
                !ColorUtils.shouldUseLightForegroundOnBackground(statusBarColor);
        ApiCompatibilityUtils.setStatusBarIconColor(
                getWindow().getDecorView().getRootView(), needsDarkStatusBarIcons);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ensureActivityNotExported();
        super.onCreate(savedInstanceState);
        mIsNewlyCreated = savedInstanceState == null;
        String initialFragment = getIntent().getStringExtra(EXTRA_SHOW_FRAGMENT);
        Bundle initialArguments = getIntent().getBundleExtra(EXTRA_SHOW_FRAGMENT_ARGUMENTS);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setElevation(0);


        if (savedInstanceState == null) {
            if (initialFragment == null) initialFragment = ApplicationSettings.class.getName();
            Fragment fragment = Fragment.instantiate(this, initialFragment, initialArguments);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, fragment)
                    .commit();
        }
        Resources res = getResources();
        ApiCompatibilityUtils.setTaskDescription(this, res.getString(R.string.app_name),
                BitmapFactory.decodeResource(res, R.mipmap.app_icon),
                ApiCompatibilityUtils.getColor(res, R.color.default_primary_color));
        setStatusBarColor();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (sResumedInstance != null && sResumedInstance.getTaskId() != getTaskId()
                && !mIsNewlyCreated) {


            finish();
        } else {

            if (sResumedInstance != null && sResumedInstance.getTaskId() != getTaskId()) {
                sResumedInstance.finish();
            }
            sResumedInstance = this;
            mIsNewlyCreated = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (sResumedInstance == this) sResumedInstance = null;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Fragment fragment = getMainFragment();
        ViewGroup listView = null;
        if (fragment instanceof PreferenceFragmentCompat) {
            listView = ((PreferenceFragmentCompat) fragment).getListView();
        } else if (fragment instanceof ListFragment) {
            listView = ((ListFragment) fragment).getListView();
        }
        if (listView == null) return;

        View inflatedView = getLayoutInflater().inflate(
                R.layout.settings_action_bar_shadow, findViewById(android.R.id.content));

        listView.getViewTreeObserver().addOnScrollChangedListener(
                SettingsUtils.getShowShadowOnScrollListener(
                        listView, inflatedView.findViewById(R.id.shadow)));
    }

    @Override
    public void onBackPressed() {
        Fragment activeFragment = getMainFragment();
        if (!(activeFragment instanceof OnBackPressedListener)) {
            super.onBackPressed();
            return;
        }
        OnBackPressedListener listener = (OnBackPressedListener) activeFragment;
        if (!listener.onBackPressed()) {

            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

//        MenuItem help = menu.add(
//                Menu.NONE, R.id.menu_id_general_help, Menu.CATEGORY_SECONDARY, R.string.menu_help);
//        help.setIcon(VectorDrawableCompat.create(
//                getResources(), R.drawable.ic_help_and_feedback, getTheme()));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Fragment mainFragment = getMainFragment();
        if (mainFragment != null && mainFragment.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.size() == 1) {
            MenuItem item = menu.getItem(0);
            if (item.getIcon() != null) item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public interface OnBackPressedListener {

        boolean onBackPressed();
    }
}
