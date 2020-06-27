package euphoria.psycho.browser.file;

import android.app.Activity;
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
    private Callback mCallback = new Callback() {
        @Override
        public void onCompleted(boolean success) {
            if (success) mFileManager.getFileAdapter().initialize();
            clearSelections();
        }
    };

    public FileOperationManager(FileManager fileManager) {
        mFileManager = fileManager;
    }

    public void addToCopy(List<FileItem> fileItems) {
        addToAction(fileItems, true);
    }

    public void addToCut(List<FileItem> fileItems) {
        addToAction(fileItems, false);
    }

    private void actionPaste() {
        if (mIsCopy) {
            new CopySelections(mFileManager, mSelections, mCallback)
                    .action();
        } else {
            new MoveSelections(mFileManager, mSelections, mCallback).action();
        }
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

    private void clearSelections() {
        mSelections.clear();
        mPasteButton.setVisibility(View.INVISIBLE);
        mClearButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear:
                clearSelections();
                break;
            case R.id.paste:
                actionPaste();

                break;
        }
    }

    interface Callback {
        void onCompleted(boolean success);
    }

    private static class CopySelections extends BaseFileTask {

        public CopySelections(FileManager fileManager, List<FileItem> selections, Callback callback) {
            super(fileManager, selections, callback);
        }

        @Override
        protected void execute() {
            String targetDirectory = mFileManager.getDirectory();

            for (FileItem f : mSelections) {
                File sourceFile = new File(f.getUrl());
                File targetFile = new File(targetDirectory, f.getTitle());
                if (f.getUrl().equals(targetFile.getAbsolutePath())) continue;

                // 移动文件到另一个硬件
                if (sourceFile.isFile()) {
                    updateProgress(targetFile.getName());
                    boolean result = NativeHelper.copyFile(sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());
                    if (!result) {
                        updateFailure();
                        return;
                    }
                    result = sourceFile.delete();
                    if (!result) {
                        updateFailure();
                        return;
                    }
                } else {
                    moveDirectory(sourceFile, targetFile);
                }

            }
        }
    }

    private static class MoveSelections extends BaseFileTask {


        private final String mInternalPath;

        public MoveSelections(FileManager fileManager, List<FileItem> selections, Callback callback) {
            super(fileManager, selections, callback);
            mInternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        private boolean isInSameDisk(String source, String target) {
            return source.startsWith(mInternalPath) == target.startsWith(mInternalPath);
        }

        @Override
        protected void execute() {
            String targetDirectory = mFileManager.getDirectory();
            for (FileItem f : mSelections) {
                File sourceFile = new File(f.getUrl());
                File targetFile = new File(targetDirectory, f.getTitle());
                if (f.getUrl().equals(targetFile.getAbsolutePath())) continue;

                if (isInSameDisk(f.getUrl(), targetFile.getAbsolutePath())) {
                    boolean result = sourceFile.renameTo(targetFile);
                    if (result) {
                        updateProgress(targetFile.getName());
                    } else {
                        updateFailure();
                        return;
                    }
                } else {
                    // 移动文件到另一个硬件
                    if (sourceFile.isFile()) {
                        updateProgress(targetFile.getName());
                        boolean result = NativeHelper.copyFile(sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());
                        if (!result) {
                            updateFailure();
                            return;
                        }
                        result = sourceFile.delete();
                        if (!result) {
                            updateFailure();
                            return;
                        }
                    } else {
                        moveDirectory(sourceFile, targetFile);
                    }
                }

            }
        }


    }
}
