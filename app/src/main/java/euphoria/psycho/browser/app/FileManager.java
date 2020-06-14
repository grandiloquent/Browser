package euphoria.psycho.browser.app;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet.OnClickListener;
import euphoria.psycho.browser.base.Share;
import euphoria.psycho.browser.widget.SelectableListLayout;
import euphoria.psycho.browser.widget.SelectableListToolbar.SearchDelegate;
import euphoria.psycho.browser.widget.SelectionDelegate;
import euphoria.psycho.browser.widget.SelectionDelegate.SelectionObserver;

public class FileManager implements OnMenuItemClickListener, SelectionObserver<FileItem>, SearchDelegate {
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
        mFileAdapter = new FileAdapter(mSelectionDelegate, this);

        mSelectableListLayout =
                (SelectableListLayout<FileItem>) LayoutInflater.from(activity).inflate(
                        R.layout.file_main, null);
        mRecyclerView = mSelectableListLayout.initializeRecyclerView(mFileAdapter);

        mToolbar = (FileManagerToolbar) mSelectableListLayout.initializeToolbar(
                R.layout.file_toolbar, mSelectionDelegate, R.string.menu_file,
                R.id.normal_menu_group, R.id.selection_mode_menu_group, this, true,
                false);
        mToolbar.setManager(this);
        mToolbar.initializeSearchView(this, R.string.file_manager_search, R.id.search_menu_id);

        mSelectableListLayout.configureWideDisplayStyle();

        mEmptyView = mSelectableListLayout.initializeEmptyView(
                R.string.file_manager_empty, R.string.file_manager_no_results);
    }

    public ViewGroup getView() {
        return mSelectableListLayout;
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
                new BottomSheet(mActivity)
                        .setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClicked(Pair<Integer, String> item) {
                                switch (item.first) {
                                    case R.drawable.ic_twitter:
                                        FileHelper.extractTwitterVideo(mActivity);
                                        break;
                                    case R.drawable.ic_youtube:
                                        FileHelper.startYouTube(mActivity);
                                        break;
                                    case R.drawable.ic_film:
                                        FileHelper.startVideoServer(mActivity);
                                        break;
                                }
                            }
                        })
                        .showDialog();

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
