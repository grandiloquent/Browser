package euphoria.psycho.browser.file;

import android.os.Handler;

import java.io.File;

import euphoria.psycho.browser.tasks.FutureListener;
import euphoria.psycho.browser.tasks.ThreadPool;
import euphoria.psycho.browser.tasks.ThreadPool.Job;
import euphoria.psycho.browser.tasks.ThreadPool.JobContext;
import euphoria.psycho.share.FormatUtils;

public class FileInfoManager {
    private ThreadPool mThreadPool;
    private Handler mHandler = new Handler();

    public FileInfoManager() {
        mThreadPool = new ThreadPool();
    }

    public void query(String path, FutureListener<String> futureListener) {
        mThreadPool.submit(new FileInfoJob(path), futureListener);
    }

    private static class FileInfoJob implements Job<String> {
        private String mPath;

        public FileInfoJob(String path) {
            mPath = path;
        }

        @Override
        public String run(JobContext jc) {
            File file = new File(mPath);
            long size = FileHelper.getFileSize(file, true);
            if (file.isDirectory()) {
                return String.format("%s items", size);
            } else {
                return FormatUtils.formatFileSize(file.length());
            }
        }
    }

    public Handler getHandler() {
        return mHandler;
    }
}