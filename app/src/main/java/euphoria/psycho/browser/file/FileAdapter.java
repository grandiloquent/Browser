package euphoria.psycho.browser.file;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.file.FileProvider.BrowsingFileObserver;
import euphoria.psycho.browser.widget.SelectableItemViewHolder;
import euphoria.psycho.browser.widget.SelectionDelegate;

public class FileAdapter extends Adapter<ViewHolder> implements BrowsingFileObserver {
    private final FileManager mFileManager;
    private final FileProvider mFileProvider;
    //private final ArrayList<FileItemView> mItemViews;
    private final List<FileItem> mItems = new ArrayList<>();
    private final SelectionDelegate<FileItem> mSelectionDelegate;
    private boolean mIsDestroyed;
    private boolean mIsLoadingItems;
    private RecyclerView mRecyclerView;

    public FileAdapter(SelectionDelegate<FileItem> delegate, FileManager manager, FileProvider provider) {
        mSelectionDelegate = delegate;
        mFileProvider = provider;
        mFileProvider.setObserver(this);
        mFileManager = manager;
        //mItemViews = new ArrayList<>();
    }

    public void initialize() {
        Log.e("B5aOx2", String.format("initialize, %s", ""));
        mIsLoadingItems = true;
        mFileProvider.queryFile(mFileManager.getDirectory(), mFileManager);
//        mFileManager.getHistoryList().remove(mFileManager.getDirectory());
//        mFileManager.getHistoryList().addFirst(mFileManager.getDirectory());
//        if (mFileManager.getHistoryList().size() > 5) {
//            mFileManager.getHistoryList().removeLast();
//        }
    }

    public void markItemForRemoval(FileItem i) {
        mItems.remove(i);
        mFileProvider.markItemForRemoval(i);
    }

    public void onDestroyed() {
        mIsDestroyed = true;


        mRecyclerView = null;

    }

    public void onEndSearch() {
    }

    public void onSelectionStateChange() {
    }

    public void removeItems() {
        mFileProvider.removeItems();
    }

    public void search(String query) {
    }

    private void clear(boolean b) {
    }

    private void loadItems(List<FileItem> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        // This adapter should only ever be attached to one RecyclerView.
        assert mRecyclerView == null;

        mRecyclerView = recyclerView;
    }

    public List<FileItem> getFileItems() {
        return mItems;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem item = mItems.get(position);
        SelectableItemViewHolder<FileItem> current = (SelectableItemViewHolder<FileItem>) holder;
        current.displayItem(item);
        ((FileItemView) holder.itemView).setFileManager(mFileManager);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create the row associated with this adapter
        ViewGroup row = (ViewGroup) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.file_item_view, parent, false);
        SelectableItemViewHolder<FileItem> holder = new SelectableItemViewHolder<>(row, mSelectionDelegate);
        //((BookmarkRow) row).onDelegateInitialized(mDelegate);
        FileItemView itemView = (FileItemView) holder.itemView;
        itemView.setFileImageManager(mFileManager.getFileImageManager());
        //mItemViews.add(itemView);
        return holder;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = null;
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