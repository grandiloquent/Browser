package euphoria.psycho.browser.file;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.browser.R;

public class FileImageManager {

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

    private final Context mContext;

    public FileImageManager(Context context) {
        mContext = context;
        initializeDefaultDrawables();
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
}
