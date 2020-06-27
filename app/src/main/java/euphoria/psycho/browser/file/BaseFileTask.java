package euphoria.psycho.browser.file;

import android.app.ProgressDialog;

import java.io.File;
import java.util.List;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.file.FileOperationManager.Callback;

public abstract class BaseFileTask {

    private final Callback mCallback;
    protected final FileManager mFileManager;
    private final ProgressDialog mProgressDialog;
    protected final List<FileItem> mSelections;

    public BaseFileTask(FileManager fileManager, List<FileItem> selections, Callback callback) {

        mFileManager = fileManager;
        mSelections = selections;
        mCallback = callback;
        mProgressDialog = new ProgressDialog(fileManager.getActivity());
        mProgressDialog.setTitle(R.string.copy_files_progress_dialog_title);
    }

    public void action() {
        mProgressDialog.show();
        Thread thread = new Thread(() -> {
            execute();
            updateSuccess();
        });
        thread.start();
    }

    protected abstract void execute();

    protected void updateFailure() {
        mFileManager.getActivity().runOnUiThread(() -> {
            mProgressDialog.dismiss();
            mCallback.onCompleted(false);
        });
    }

    // 使用 UI 线程更新 ProgressDialog
    protected void updateProgress(String message) {
        mFileManager.getActivity().runOnUiThread(() -> {
            mProgressDialog.setMessage(message);
        });
    }

    protected void updateSuccess() {
        mFileManager.getActivity().runOnUiThread(() -> {
            mCallback.onCompleted(true);
            mProgressDialog.dismiss();
        });
    }

    protected void moveDirectory(final File srcDir, final File destDir) {
        if (!destDir.isDirectory()) {
            destDir.mkdir();
        }
        File[] files = srcDir.listFiles();
        if (files == null || files.length == 0) return;
        for (File f : files) {
            if (f.isFile()) {
                File target = new File(destDir, f.getName());
                updateProgress(target.getName());
                NativeHelper.copyFile(f.getAbsolutePath(), target.getAbsolutePath());
            } else if (f.isDirectory()) {
                moveDirectory(f, new File(destDir, f.getName()));
            }
        }
    }
}
