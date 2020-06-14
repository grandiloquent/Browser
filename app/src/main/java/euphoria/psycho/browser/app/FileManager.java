package euphoria.psycho.browser.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.widget.SelectableListLayout;
import euphoria.psycho.browser.widget.SelectionDelegate;
import euphoria.psycho.browser.widget.SelectionDelegate.SelectionObserver;

public class FileManager implements SelectionObserver<FileItem> {
    private final Activity mActivity;
    private final SelectableListLayout<FileItem> mSelectableListLayout;
    private final RecyclerView mRecyclerView;
    private final FileAdapter mFileAdapter;
    private final SelectionDelegate<FileItem> mSelectionDelegate;

    public FileManager(Activity activity) {
        mActivity = activity;
        mSelectionDelegate = new SelectionDelegate<>();
        mSelectionDelegate.addObserver(this);
        mFileAdapter = new FileAdapter(mSelectionDelegate, this);

        mSelectableListLayout =
                (SelectableListLayout<FileItem>) LayoutInflater.from(activity).inflate(
                        R.layout.file_main, null);
        mRecyclerView = mSelectableListLayout.initializeRecyclerView(mFileAdapter);

    }

    public ViewGroup getView() {
        return mSelectableListLayout;
    }

    @Override
    public void onSelectionStateChange(List<FileItem> selectedItems) {

    }
}
