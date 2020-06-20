package euphoria.psycho.browser.file;

import android.content.Context;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import euphoria.psycho.browser.R;

public class FileImageHelper {
    private VectorDrawableCompat mAudioDrawable;
    private VectorDrawableCompat mDirectoryDrawable;
    private VectorDrawableCompat mImageDrawable;
    private VectorDrawableCompat mOthersDrawable;
    private VectorDrawableCompat mTextDrawable;

    public FileImageHelper(Context context) {
        mDirectoryDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_folder,
                context.getTheme());
        mAudioDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_music,
                context.getTheme());
        mTextDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_text,
                context.getTheme());
        mOthersDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_others,
                context.getTheme());
        mImageDrawable = VectorDrawableCompat.create(
                context.getResources(),
                R.drawable.ic_type_image,
                context.getTheme());
    }

    public VectorDrawableCompat getDefaultFileImage(FileItem fileItem) {
        switch (fileItem.getType()) {
            case FileHelper.TYPE_DIRECTORY:
                return mDirectoryDrawable;
            case FileHelper.TYPE_FILE_AUDIO:
                return mAudioDrawable;
            case FileHelper.TYPE_FILE_TEXT:
                return mTextDrawable;
            case FileHelper.TYPE_FILE_IMAGE:
                return mImageDrawable;
            default:
                return mOthersDrawable;
        }

    }
}
