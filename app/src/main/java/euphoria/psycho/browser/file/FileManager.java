package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.Share;
import euphoria.psycho.browser.widget.ConversionUtils;
import euphoria.psycho.browser.widget.SelectableListLayout;
import euphoria.psycho.browser.widget.SelectableListToolbar.SearchDelegate;
import euphoria.psycho.browser.widget.SelectionDelegate;
import euphoria.psycho.browser.widget.SelectionDelegate.SelectionObserver;

public class FileManager implements OnMenuItemClickListener, SelectionObserver<FileItem>, SearchDelegate {
    private static final int FAVICON_MAX_CACHE_SIZE_BYTES =
            10 * ConversionUtils.BYTES_PER_MEGABYTE; // 10MB
    private final Activity mActivity;
    private final TextView mEmptyView;
    private final FileAdapter mFileAdapter;
    private final RecyclerView mRecyclerView;
    private final SelectableListLayout<FileItem> mSelectableListLayout;
    private final SelectionDelegate<FileItem> mSelectionDelegate;
    private final FileManagerToolbar mToolbar;

    public FileManager(Activity activity) {
        mActivity = activity;
        mSelectionDelegate = new SelectionDelegate<>();
        mSelectionDelegate.addObserver(this);
        mFileAdapter = new FileAdapter(mSelectionDelegate, this, new FileProviderImpl());
        // 1. Create SelectableListLayout.
        mSelectableListLayout =
                (SelectableListLayout<FileItem>) LayoutInflater.from(activity).inflate(
                        R.layout.file_main, null);

        // 2. Initialize RecyclerView.
        mRecyclerView = mSelectableListLayout.initializeRecyclerView(mFileAdapter);

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
        mEmptyView = mSelectableListLayout.initializeEmptyView(
                R.string.file_manager_empty, R.string.file_manager_no_results);

        ActivityManager activityManager = ((ActivityManager) Share
                .getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE));
        int maxSize = Math.min(
                (activityManager.getMemoryClass() / 4) * ConversionUtils.BYTES_PER_MEGABYTE,
                FAVICON_MAX_CACHE_SIZE_BYTES);


        Log.e("TAG/", "Debug: FileManager, \n" + activityManager.getMemoryClass() + " " + FAVICON_MAX_CACHE_SIZE_BYTES);


    }

    public ViewGroup getView() {
        return mSelectableListLayout;
    }

    public void removeItem(FileItem fileItem) {
    }


    @Override
    public void onEndSearch() {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        mToolbar.hideOverflowMenu();
        switch (item.getItemId()) {
            case R.id.close_menu_id:
                mActivity.finish();
                return true;
            case R.id.menu_id:
                FileHelper.showBottomSheet(mActivity, FileHelper.createBottomSheetItems(mActivity));
                return true;
        }
        return false;
    }

    @Override
    public void onSearchTextChanged(String query) {

    }

    @Override
    public void onSelectionStateChange(List<FileItem> selectedItems) {

    }
}
