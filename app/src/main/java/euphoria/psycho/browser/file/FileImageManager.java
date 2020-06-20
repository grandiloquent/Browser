package euphoria.psycho.browser.file;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;


import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.BitmapUtils;
import euphoria.psycho.browser.base.Share;
import euphoria.psycho.browser.base.Utils;
import euphoria.psycho.browser.tasks.FutureListener;
import euphoria.psycho.browser.tasks.ThreadPool;
import euphoria.psycho.browser.tasks.ThreadPool.Job;
import euphoria.psycho.browser.tasks.ThreadPool.JobContext;

public class FileImageManager {

    private final Context mContext;
    private VectorDrawableCompat mApkDrawable;
    private VectorDrawableCompat mExcelDrawable;
    private VectorDrawableCompat mFolderDrawable;
    private VectorDrawableCompat mImageDrawable;
    private VectorDrawableCompat mMusicDrawable;
    private VectorDrawableCompat mOthersDrawable;
    private VectorDrawableCompat mPdfDrawable;
    private VectorDrawableCompat mPpsDrawable;
    private VectorDrawableCompat mTextDrawable;
    private VectorDrawableCompat mVcfDrawable;
    private VectorDrawableCompat mVideoDrawable;
    private VectorDrawableCompat mWordDrawable;
    private VectorDrawableCompat mZipDrawable;
    private ThreadPool mThreadPool;
    private Handler mHandler;
    private final LruCache<String, Drawable> mLruCache = new LruCache<>(10 * 1024 * 1024);

    public FileImageManager(Context context) {
        mContext = context;
        initializeDefaultDrawables();
        mThreadPool = new ThreadPool();
        mHandler = new ImageHandler();
    }

    public Drawable getDefaultDrawable(FileItem fileItem) {
        switch (fileItem.getType()) {

            case FileHelper.TYPE_APK:
                return mApkDrawable;

            case FileHelper.TYPE_EXCEL:
                return mExcelDrawable;

            case FileHelper.TYPE_FOLDER:
                return mFolderDrawable;

            case FileHelper.TYPE_IMAGE:
                return mImageDrawable;

            case FileHelper.TYPE_MUSIC:
                return mMusicDrawable;


            case FileHelper.TYPE_PDF:
                return mPdfDrawable;

            case FileHelper.TYPE_PPS:
                return mPpsDrawable;

            case FileHelper.TYPE_TEXT:
                return mTextDrawable;

            case FileHelper.TYPE_VCF:
                return mVcfDrawable;

            case FileHelper.TYPE_VIDEO:
                return mVideoDrawable;

            case FileHelper.TYPE_WORD:
                return mWordDrawable;

            case FileHelper.TYPE_ZIP:
                return mZipDrawable;
            default:
                return mOthersDrawable;
        }
    }

    public void getDrawable(FileItem fileItem, int size, FutureListener<Drawable> futureListener) {

        switch (fileItem.getType()) {
            case FileHelper.TYPE_VIDEO:
            case FileHelper.TYPE_IMAGE:
                mThreadPool.submit(new ImageJob(fileItem, size, mLruCache), futureListener);
                return;
        }
    }

    public Handler getHandler() {
        return mHandler;
    }

    private void initializeDefaultDrawables() {
        Resources resources = mContext.getResources();
        Theme theme = mContext.getTheme();

        mApkDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_apk, theme);
        mExcelDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_excel, theme);
        mFolderDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_folder, theme);
        mImageDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_image, theme);
        mMusicDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_music, theme);
        mOthersDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_others, theme);
        mPdfDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_pdf, theme);
        mPpsDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_pps, theme);
        mTextDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_text, theme);
        mVcfDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_vcf, theme);
        mVideoDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_video, theme);
        mWordDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_word, theme);
        mZipDrawable = VectorDrawableCompat.create(resources,
                R.drawable.ic_type_zip, theme);

    }

    private static class ImageJob implements Job<Drawable> {

        private final FileItem mFileItem;
        private final int mSize;
        private final LruCache<String, Drawable> mLruCache;

        private ImageJob(FileItem fileItem, int size, LruCache<String, Drawable> lruCache) {
            mFileItem = fileItem;
            mSize = size;
            mLruCache = lruCache;
        }

        @Override
        public Drawable run(JobContext jc) {
            Drawable drawable = null;
            String key = Long.toString(Utils.crc64Long(mFileItem.getUrl()));

            drawable = mLruCache.get(key);
            if (drawable != null) {
                Log.e("TAG/", "Debug: run, from Cache\n");
                return drawable;
            }
            Bitmap bitmap = null;
            switch (mFileItem.getType()) {
                case FileHelper.TYPE_VIDEO:
                    bitmap = Share.createVideoThumbnail(mFileItem.getUrl());
                    break;
                case FileHelper.TYPE_IMAGE:
                    bitmap = BitmapFactory.decodeFile(mFileItem.getUrl());
                    break;
            }
            if (bitmap == null) {
                return null;
            }
            bitmap = BitmapUtils.resizeAndCropCenter(bitmap, mSize, true);
            drawable = new BitmapDrawable(bitmap);
            mLruCache.put(key, drawable);
            return drawable;
        }
    }

    private static class ImageHandler extends Handler {

    }
}
