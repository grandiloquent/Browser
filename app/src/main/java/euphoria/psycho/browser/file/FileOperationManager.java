package euphoria.psycho.browser.file;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;


import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.widget.FloatingActionButton;

public class FileOperationManager implements OnClickListener {

    private final List<FileItem> mSelections = new ArrayList<>();
    private FloatingActionButton mPasteButton;
    private FloatingActionButton mClearButton;
    private final Activity mActivity;
    private boolean mIsCopy;

    public FileOperationManager(Activity activity) {
        mActivity = activity;
    }

    public void addToCopy(List<FileItem> fileItems) {
        addToAction(fileItems, true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear:
                mSelections.clear();
                mPasteButton.setVisibility(View.INVISIBLE);
                mClearButton.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private void addToAction(List<FileItem> fileItems, boolean isCopy) {
        if (mPasteButton == null) {
            mPasteButton = mActivity.findViewById(R.id.paste);
            mPasteButton.setIconDrawable(mActivity.getDrawable(R.drawable.ic_action_content_paste));
            mPasteButton.setOnClickListener(this);
            mClearButton = mActivity.findViewById(R.id.clear);
            mClearButton.setIconDrawable(mActivity.getDrawable(R.drawable.ic_action_clear));
            mClearButton.setOnClickListener(this);
        }
        mIsCopy = isCopy;
        mSelections.clear();
        mSelections.addAll(fileItems);
        mPasteButton.setVisibility(View.VISIBLE);
        mClearButton.setVisibility(View.VISIBLE);
    }


}
