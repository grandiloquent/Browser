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

import java.io.File;
import java.io.IOException;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.browser.R;
import euphoria.psycho.share.BitmapUtils;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.browser.tasks.FutureListener;
import euphoria.psycho.browser.tasks.ThreadPool;
import euphoria.psycho.browser.tasks.ThreadPool.Job;
import euphoria.psycho.browser.tasks.ThreadPool.JobContext;
import euphoria.psycho.share.FileUtils;
import euphoria.psycho.share.KeyUtils;

import static euphoria.psycho.share.BitmapUtils.createVideoThumbnail;

public class FileImageManager {
    private final Context mContext;
    private final LruCache<String, Drawable> mLruCache;
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
    private String mCacheDirectory;

    public FileImageManager(Context context, int maxCacheSize) {
        mContext = context;
        initializeDefaultDrawables();
        mThreadPool = new ThreadPool();
        mHandler = new ImageHandler();
        File cacheDirectory = new File(context.getCacheDir(), "images");
        if (!cacheDirectory.isDirectory()) {
            cacheDirectory.mkdirs();
        }
        mCacheDirectory = cacheDirectory.getAbsolutePath();
        mLruCache = new LruCache<String, Drawable>(maxCacheSize);
    }

    public Drawable getDefaultDrawable(FileItem fileItem) {
        switch (fileItem.getType()) {
            case FileConstantsHelper.TYPE_APK:
                return mApkDrawable;
            case FileConstantsHelper.TYPE_EXCEL:
                return mExcelDrawable;
            case FileConstantsHelper.TYPE_FOLDER:
                return mFolderDrawable;
            case FileConstantsHelper.TYPE_IMAGE:
                return mImageDrawable;
            case FileConstantsHelper.TYPE_MUSIC:
                return mMusicDrawable;
            case FileConstantsHelper.TYPE_PDF:
                return mPdfDrawable;
            case FileConstantsHelper.TYPE_PPS:
                return mPpsDrawable;
            case FileConstantsHelper.TYPE_TEXT:
                return mTextDrawable;
            case FileConstantsHelper.TYPE_VCF:
                return mVcfDrawable;
            case FileConstantsHelper.TYPE_VIDEO:
                return mVideoDrawable;
            case FileConstantsHelper.TYPE_WORD:
                return mWordDrawable;
            case FileConstantsHelper.TYPE_ZIP:
                return mZipDrawable;
            default:
                return mOthersDrawable;
        }
    }

    public void getDrawable(FileItem fileItem, int size, FutureListener<Drawable> futureListener) {
        switch (fileItem.getType()) {
            case FileConstantsHelper.TYPE_VIDEO:
            case FileConstantsHelper.TYPE_IMAGE:
                mThreadPool.submit(new ImageJob(fileItem, size, mLruCache, mCacheDirectory), futureListener);
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
        private final String mCacheDirectory;
        private final FileItem mFileItem;
        private final LruCache<String, Drawable> mLruCache;
        private final int mSize;

        private ImageJob(FileItem fileItem, int size, LruCache<String, Drawable> lruCache, String cacheDirectory) {
            mFileItem = fileItem;
            mSize = size;
            mLruCache = lruCache;
            mCacheDirectory = cacheDirectory;
        }

        @Override
        public Drawable run(JobContext jc) {
            Drawable drawable = null;
            String key = Long.toString(KeyUtils.crc64Long(mFileItem.getUrl()));
            drawable = mLruCache.get(key);
            if (drawable != null) {
                return drawable;
            }
            Bitmap bitmap = null;
            File image = new File(mCacheDirectory, key);
            if (image.isFile()) {
                BitmapFactory.Options opts;
                bitmap = BitmapFactory.decodeFile(image.getAbsolutePath());
            }
            if (bitmap == null) {
                switch (mFileItem.getType()) {
                    case FileConstantsHelper.TYPE_VIDEO:
                        bitmap = createVideoThumbnail(mFileItem.getUrl());
                        break;
                    case FileConstantsHelper.TYPE_IMAGE:
                        final BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(mFileItem.getUrl(), options);
                        options.inSampleSize = BitmapUtils.calculateInSampleSize(options, mSize, mSize);
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeFile(mFileItem.getUrl(), options);
                        break;
                    case FileConstantsHelper.TYPE_APK:
                        Drawable ico = ContextUtils.getApkIcon(ContextUtils.getApplicationContext(), mFileItem.getUrl());
                        if (ico != null)
                            bitmap = BitmapUtils.drawableToBitmap(ico);
                        break;
                }
                if (bitmap != null) {
                    byte[] buffer = BitmapUtils.compressToBytes(bitmap);
                    try {
                        FileUtils.writeAllBytes(image.getAbsolutePath(), buffer);
                    } catch (IOException e) {
                        Log.e("TAG/" + ImageJob.this.getClass().getSimpleName(), "Error: run, " + e.getMessage() + " " + e.getCause());
                    }
                }
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