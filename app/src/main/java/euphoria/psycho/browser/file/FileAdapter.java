package euphoria.psycho.browser.file;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import euphoria.psycho.browser.file.FileProvider.BrowsingFileObserver;
import euphoria.psycho.browser.widget.SelectionDelegate;

public class FileAdapter extends Adapter<ViewHolder> implements BrowsingFileObserver {
    private final FileManager mFileManager;
    private final FileProvider mFileProvider;
    private final ArrayList<FileItemView> mItemViews;
    private final List<FileItem> mItems = new ArrayList<>();
    private final SelectionDelegate<FileItem> mSelectionDelegate;
    private boolean mIsDestroyed;
    private boolean mIsLoadingItems;
    private String mDirectory;

    public FileAdapter(SelectionDelegate<FileItem> delegate, FileManager manager, FileProvider provider) {
        mSelectionDelegate = delegate;
        mFileProvider = provider;
        mFileProvider.setObserver(this);
        mFileManager = manager;
        mItemViews = new ArrayList<>();
    }

    public void initialize() {
        mIsLoadingItems = true;
        mFileProvider.queryFile(mDirectory);
    }

    private void clear(boolean b) {
    }

    private void loadItems(List<FileItem> items) {
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onFileDeleted() {
        if (mIsDestroyed) return;
        mSelectionDelegate.clearSelection();
        initialize();
    }

    @Override
    public void onQueryFileComplete(List<FileItem> items) {
        if (mIsDestroyed) return;
        clear(true);
        loadItems(items);
        mIsLoadingItems = false;
    }
}
