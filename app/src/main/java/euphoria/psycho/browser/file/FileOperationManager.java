package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Environment;
import android.util.Log;
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
            clear();
        }
    };


    interface Callback {
        void onCompleted(boolean success);
    }

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.clear:
                clear();
                break;
            case R.id.paste:
                actionPaste();

                break;
        }
    }

    private void clear() {
        mSelections.clear();
        mPasteButton.setVisibility(View.INVISIBLE);
        mClearButton.setVisibility(View.INVISIBLE);
    }

    private static class MoveSelections {
        private final FileManager mFileManager;
        private final ProgressDialog mProgressDialog;
        private final List<FileItem> mSelections;
        private final Callback mCallback;
        private final String mInternalPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        public MoveSelections(FileManager fileManager, List<FileItem> selections, Callback callback) {

            mFileManager = fileManager;
            mSelections = selections;
            mCallback = callback;
            mProgressDialog = new ProgressDialog(fileManager.getActivity());
            mProgressDialog.setTitle(R.string.move_files_progress_dialog_title);
        }

        private boolean isInSameDisk(String source, String target) {
            if (source.startsWith(mInternalPath) == target.startsWith(mInternalPath)) {
                return true;
            }
            return false;
        }

        private void runOnUi(Runnable r) {
            mFileManager.getActivity().runOnUiThread(r);
        }


        public void action() {
            mProgressDialog.show();
            Thread thread = new Thread(() -> {
                String targetDirectory = mFileManager.getDirectory();

                for (FileItem f : mSelections) {
                    File sourceFile = new File(f.getUrl());
                    File targetFile = new File(targetDirectory, f.getTitle());
                    if (f.getUrl().equals(targetFile.getAbsolutePath())) continue;

                    if (isInSameDisk(f.getUrl(), targetFile.getAbsolutePath())) {
                        boolean result = sourceFile.renameTo(targetFile);
                        if (result) {
                            runOnUi(() -> {
                                mProgressDialog.setMessage(targetFile.getName());
                            });
                        } else {
                            runOnUi(() -> {
                                mProgressDialog.dismiss();
                                mCallback.onCompleted(false);
                            });
                            return;
                        }
                    } else {
                        if (sourceFile.isFile()) {
                            runOnUi(() -> {
                                mProgressDialog.setMessage(targetFile.getName());
                            });
                            boolean result = NativeHelper.copyFile(sourceFile.getAbsolutePath(), targetFile.getAbsolutePath());

                            if (!result) {
                                runOnUi(() -> {
                                    mProgressDialog.dismiss();
                                    mCallback.onCompleted(false);
                                });
                                return;
                            }
                            result = sourceFile.delete();
                            if (!result) {
                                runOnUi(() -> {
                                    mProgressDialog.dismiss();
                                    mCallback.onCompleted(false);
                                });
                                return;
                            }
                        }
                    }

                }
                runOnUi(() -> {
                    mCallback.onCompleted(true);
                    mProgressDialog.dismiss();
                });
            });
            thread.start();
        }
    }
}
