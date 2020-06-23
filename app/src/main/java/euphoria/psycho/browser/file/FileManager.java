package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.stream.Stream;

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet;
import euphoria.psycho.browser.app.SettingsManager;
import euphoria.psycho.browser.base.Share;
import euphoria.psycho.browser.widget.ConversionUtils;
import euphoria.psycho.browser.widget.SelectableListLayout;
import euphoria.psycho.browser.widget.SelectableListToolbar.SearchDelegate;
import euphoria.psycho.browser.widget.SelectionDelegate;
import euphoria.psycho.browser.widget.SelectionDelegate.SelectionObserver;

public class FileManager implements OnMenuItemClickListener,
        SelectionObserver<FileItem>, SearchDelegate, OnSharedPreferenceChangeListener {
    private static final int FAVICON_MAX_CACHE_SIZE_BYTES =
            10 * ConversionUtils.BYTES_PER_MEGABYTE; // 10MB
    private final Activity mActivity;
    private final FileAdapter mFileAdapter;
    private final SelectableListLayout<FileItem> mSelectableListLayout;
    private final SelectionDelegate<FileItem> mSelectionDelegate;
    private final FileManagerToolbar mToolbar;
    private BottomSheet mBottomSheet;
    private boolean mIsSearching;
    private FileImageManager mFileImageManager;
    private String mDirectory;
    private int mSortType;
    private boolean mIsShowHiddenFiles;
    private FileOperationManager mFileOperationManager;

    public FileManager(Activity activity) {
        mActivity = activity;
        loadPrefer();
        mSelectionDelegate = new SelectionDelegate<>();
        mSelectionDelegate.addObserver(this);
        mFileAdapter = new FileAdapter(mSelectionDelegate, this, new FileProviderImpl());
        // 1. Create SelectableListLayout.
        mSelectableListLayout =
                (SelectableListLayout<FileItem>) LayoutInflater.from(activity).inflate(
                        R.layout.file_main, null);
        // 2. Initialize RecyclerView.
        RecyclerView recyclerView = mSelectableListLayout.initializeRecyclerView(mFileAdapter);
        // 3. Initialize toolbar.
        mToolbar = (FileManagerToolbar) mSelectableListLayout.initializeToolbar(
                R.layout.file_toolbar, mSelectionDelegate, R.string.menu_file,
                R.id.normal_menu_group, R.id.selection_mode_menu_group, this, true,
                false);
        mToolbar.setManager(this);
        mToolbar.initializeSearchView(this, R.string.file_manager_search, R.id.search_menu_id);
        // 4. Width constrain the SelectableListLayout.
        mSelectableListLayout.configureWideDisplayStyle();
        // 5. Initialize empty view.
        TextView emptyView = mSelectableListLayout.initializeEmptyView(
                R.string.file_manager_empty, R.string.file_manager_no_results);
        mFileAdapter.initialize();
        FileHelper.initialize(activity);
        mFileOperationManager = new FileOperationManager(this);
        Share.getAppSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    public void copy(FileItem item) {
    }

    public void delete(FileItem item) {
        FileHelper.delete(this, item);
    }

    public Activity getActivity() {
        return mActivity;
    }

    public String getDirectory() {
        if (mDirectory == null) {
            mDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        }
        return mDirectory;
    }

    public FileAdapter getFileAdapter() {
        return mFileAdapter;
    }

    public FileImageManager getFileImageManager() {
        if (mFileImageManager == null) {
            ActivityManager activityManager = ((ActivityManager) Share
                    .getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE));
            int maxSize = Math.min(
                    (activityManager.getMemoryClass() / 4) * ConversionUtils.BYTES_PER_MEGABYTE,
                    FAVICON_MAX_CACHE_SIZE_BYTES);
            mFileImageManager = new FileImageManager(mActivity, maxSize);
        }
        return mFileImageManager;
    }

    public FileOperationManager getFileOperationManager() {
        return mFileOperationManager;
    }

    public SelectionDelegate<FileItem> getSelectionDelegate() {
        return mSelectionDelegate;
    }

    public boolean getShowHidden() {
        return mIsShowHiddenFiles;
    }


    public int getSortType() {
        return mSortType;
    }

    public ViewGroup getView() {
        return mSelectableListLayout;
    }

    public boolean onBackPressed() {
        String parent = Share.substringBeforeLast(mDirectory, '/');
        if (parent.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath())
                || parent.startsWith(FileHelper.getSDPath())) {
            mDirectory = parent;
            mFileAdapter.initialize();
            return true;
        }
        return mSelectableListLayout.onBackPressed();
    }

    public void onDestroy() {
        mSelectableListLayout.onDestroyed();
        mFileAdapter.onDestroyed();
        Share.getAppSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        if (mBottomSheet != null) {
            mBottomSheet.dismiss();
        }
        SettingsManager.getInstance().setLastAccessDirectory(mDirectory);
    }

    public void openDirectory(String dir) {
        mDirectory = dir;
        mFileAdapter.initialize();
    }

    public void openUrl(FileItem fileItem) {
        if (fileItem.getType() == FileHelper.TYPE_FOLDER) {
            mDirectory = fileItem.getUrl();
            mFileAdapter.initialize();
            return;
        }
        FileHelper.openUrl(mActivity, fileItem);
    }

    public void refresh() {
        mFileAdapter.initialize();
    }

    public void removeItem(FileItem fileItem) {
    }

    public void setBottomSheet(BottomSheet bottomSheet) {
        mBottomSheet = bottomSheet;
    }

    private void loadPrefer() {
        mDirectory = SettingsManager.getInstance().getLastAccessDirectory();
        mIsShowHiddenFiles = SettingsManager.getInstance().getDisplayHiddenFiles();
        mSortType = SettingsManager.getInstance().getSortType();
    }

    // 排序文件
    private void sortBy() {
        SettingsManager.getInstance().setSortType(mSortType);
        mFileAdapter.initialize();
    }

    @Override
    public void onEndSearch() {
        mFileAdapter.onEndSearch();
        mSelectableListLayout.onEndSearch();
        mIsSearching = false;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        mToolbar.hideOverflowMenu();
        switch (item.getItemId()) {
            case R.id.menu_id:
                FileHelper.showBottomSheet(mActivity, FileHelper.createBottomSheetItems(mActivity), this);
                return true;
            case R.id.selection_mode_delete_menu_id:
                for (FileItem i : mSelectionDelegate.getSelectedItems()) {
                    mFileAdapter.markItemForRemoval(i);
                }
                mFileAdapter.removeItems();
                mSelectionDelegate.clearSelection();
                return true;
            case R.id.search_menu_id:
                mToolbar.showSearchView();
                mSelectableListLayout.onStartSearch();
                mIsSearching = true;
                return true;
            case R.id.sort_by_name_menu_id:
                mSortType = (mSortType & FileHelper.SORT_BY_ASCENDING) | FileHelper.SORT_BY_NAME;
                sortBy();
                return true;
            case R.id.sort_by_size_menu_id:
                mSortType = (mSortType & FileHelper.SORT_BY_ASCENDING) | FileHelper.SORT_BY_SIZE;
                sortBy();
                return true;
            case R.id.sort_by_data_modified_menu_id:
                mSortType = (mSortType & FileHelper.SORT_BY_ASCENDING) | FileHelper.SORT_BY_DATA_MODIFIED;
                sortBy();
                return true;
            case R.id.sort_by_type_menu_id:
                mSortType = (mSortType & FileHelper.SORT_BY_ASCENDING) | FileHelper.SORT_BY_TYPE;
                sortBy();
                return true;
            case R.id.sort_by_ascending_menu_id:
                mSortType = (mSortType & 31) | FileHelper.SORT_BY_ASCENDING;
                sortBy();
                return true;
            case R.id.sort_by_descending_menu_id:
                mSortType = (mSortType & 31);
                sortBy();
                return true;
            case R.id.selection_mode_select_same_type_menu_id:
                FileHelper.selectSameType(this);
                return true;
            case R.id.selection_mode_select_all_menu_id:
                mSelectionDelegate.setSelectedItems(new HashSet<>(mFileAdapter.getFileItems()));
                return true;
            case R.id.selection_mode_copy_menu_id:
                FileHelper.copySelections(this);
                return true;
            case R.id.selection_mode_cut_menu_id:
                FileHelper.cutSelections(this);
                return true;

        }
        return false;
        /*
        ["name","size","data_modified","type","ascending","descending"
  ]
        * */
    }

    @Override
    public void onSearchTextChanged(String query) {
        mFileAdapter.search(query);
    }

    @Override
    public void onSelectionStateChange(List<FileItem> selectedItems) {
        mFileAdapter.onSelectionStateChange();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        loadPrefer();
        mFileAdapter.initialize();
    }
}