package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.widget.FloatingActionButton;

public class FileOperationManager implements OnClickListener {

    private final FileManager mFileManager;
    private final List<FileItem> mSelections = new ArrayList<>();
    private FloatingActionButton mPasteButton;
    private FloatingActionButton mClearButton;
    private boolean mIsCopy;

    public FileOperationManager(FileManager fileManager) {
        mFileManager = fileManager;
    }

    public void addToCopy(List<FileItem> fileItems) {
        addToAction(fileItems, true);
    }

    public void addToCut(List<FileItem> fileItems) {
        addToAction(fileItems, false);
    }


    private void addToAction(List<FileItem> fileItems, boolean isCopy) {
        Activity activity = mFileManager.getActivity();
        if (mPasteButton == null) {
            mPasteButton = activity.findViewById(R.id.paste);
            mPasteButton.setIconDrawable(activity.getDrawable(R.drawable.ic_action_content_paste));
            mPasteButton.setOnClickListener(this);
            mClearButton = activity.findViewById(R.id.clear);
            mClearButton.setIconDrawable(activity.getDrawable(R.drawable.ic_action_clear));
            mClearButton.setOnClickListener(this);
        }
        mIsCopy = isCopy;
        mSelections.clear();
        mSelections.addAll(fileItems);
        mPasteButton.setVisibility(View.VISIBLE);
        mClearButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear:
                mSelections.clear();
                mPasteButton.setVisibility(View.INVISIBLE);
                mClearButton.setVisibility(View.INVISIBLE);
            case R.id.paste:
                actionPaste();
                break;
        }
    }

    private void actionPaste() {
        if (mIsCopy) {
        } else {
            new MoveSelections(mFileManager, mSelections).action();
        }
    }


    private static class MoveSelections {
        private final FileManager mFileManager;
        private final List<FileItem> mSelections;
        private final ProgressDialog mProgressDialog;

        public MoveSelections(FileManager fileManager, List<FileItem> selections) {

            mFileManager = fileManager;
            mSelections = selections;
            mProgressDialog = new ProgressDialog(fileManager.getActivity());
        }

        public void action() {
            mProgressDialog.show();
            Thread thread = new Thread(() -> {
                String targetDirectory = mFileManager.getDirectory();
                String internalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

                for (FileItem f : mSelections) {
                    String source = f.getUrl();
                    File targetFile = new File(targetDirectory, f.getTitle());
                    if (source.equals(targetFile.getAbsolutePath())) continue;

                    NativeHelper.moveFileSystem(
                            source,
                            targetFile.getAbsolutePath(),
                            internalPath
                    );
                }
                mFileManager.getActivity().runOnUiThread(() -> {
                    mProgressDialog.dismiss();
                });
            });
            thread.start();
        }
    }
}
