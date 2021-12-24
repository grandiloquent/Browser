package euphoria.psycho.browser.file;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.Handler;
import android.os.Process;
import android.text.Editable;
import android.util.Log;
import android.util.LruCache;
import android.widget.EditText;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;


import euphoria.psycho.browser.R;
import euphoria.psycho.share.BitmapUtils;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.browser.tasks.FutureListener;
import euphoria.psycho.browser.tasks.ThreadPool;
import euphoria.psycho.browser.tasks.ThreadPool.Job;
import euphoria.psycho.browser.tasks.ThreadPool.JobContext;
import euphoria.psycho.share.KeyUtils;
import euphoria.psycho.share.StringUtils;
import euphoria.share.FileShare;
import euphoria.share.StringShare;

import static euphoria.psycho.share.BitmapUtils.createVideoThumbnail;
import static euphoria.share.ThreadShare.runOnUiThread;

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

    public void covertVideo(FileItem item) {
        ProgressDialog dialog = new ProgressDialog(mContext);
        dialog.setMessage("正在下载中...");
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                //  new File(mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                // ).getAbsolutePath()
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                String arg = String.format("-i \"%s\" -c:v mpeg4 \"/storage/FD12-1F1D/Movies/%s\"", item.getUrl(), StringShare.substringAfterLast(item.getUrl(), "/"));
                Log.e("B5aOx2", String.format("run, %s", arg));

                FFmpegSession session = FFmpegKit.execute(arg);
                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    // SUCCESS
                } else if (ReturnCode.isCancel(session.getReturnCode())) {
                    // CANCEL
                } else {

                    Log.e("B5aOx2", String.format("run, %s",session.getAllLogsAsString()));
                    // FAILURE
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        }).start();
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

    public void cutVideo(FileItem item) {
        EditText editText = new EditText(mContext);
        new AlertDialog.Builder(mContext)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                    Editable value = editText.getText();
                    Pattern numbers = Pattern.compile("\\d+");
                    Matcher matcher = numbers.matcher(value);
                    int[] n = new int[4];
                    int i = 0;
                    while (matcher.find()) {
                        n[i++] = Integer.parseInt(matcher.group());
                        if (i > 3) break;
                    }
                    File src = new File(item.getUrl());
                    File dst = new File(src.getParentFile(), StringUtils.substringBeforeLast(src.getName(), '.') + "_splitted." + StringUtils.substringAfterLast(src.getName(), '.'));
                    try {
                        Log.e("TAG/", "[FileImageManager]: cutVideo" + n[0] + " " + n[1] + " " + n[2] + " " + n[3] + " ");
                        startTrim(src, dst, (n[0] * 60 * 1000 + n[1] * 1000), (n[2] * 60 * 1000 + n[3] * 1000));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    dialog1.dismiss();
                })
                .show();
    }

    public static void startTrim(File src, File dst, int startMs, int endMs) throws IOException {
        FileDataSourceImpl file = new FileDataSourceImpl(src);
        Movie movie = MovieCreator.build(file);
        // remove all tracks we will create new tracks from the old
        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        double startTime = startMs / 1000;
        double endTime = endMs / 1000;
        boolean timeCorrected = false;
        // Here we try to find a track that has sync samples. Since we can only start decoding
        // at such a sample we SHOULD make sure that the start of the new fragment is exactly
        // such a frame
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    // This exception here could be a false positive in case we have multiple tracks
                    // with sync samples at exactly the same positions. E.g. a single movie containing
                    // multiple qualities of the same video (Microsoft Smooth Streaming file)
                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
                startTime = correctTimeToSyncSample(track, startTime, false);
                endTime = correctTimeToSyncSample(track, endTime, true);
                timeCorrected = true;
            }
        }
        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            long startSample = -1;
            long endSample = -1;
            for (int i = 0; i < track.getSampleDurations().length; i++) {
                if (currentTime <= startTime) {
                    // current sample is still before the new starttime
                    startSample = currentSample;
                }
                if (currentTime <= endTime) {
                    // current sample is after the new start time and still before the new endtime
                    endSample = currentSample;
                } else {
                    // current sample is after the end of the cropped video
                    break;
                }
                currentTime += (double) track.getSampleDurations()[i] / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new CroppedTrack(track, startSample, endSample));
        }
        Container out = new DefaultMp4Builder().build(movie);
        MovieHeaderBox mvhd = Path.getPath(out, "moov/mvhd");
        mvhd.setMatrix(Matrix.ROTATE_180);
        if (!dst.exists()) {
            dst.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(dst);
        WritableByteChannel fc = fos.getChannel();
        try {
            out.writeContainer(fc);
        } finally {
            fc.close();
            fos.close();
            file.close();
        }
        file.close();
    }


    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];
            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
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
                        FileShare.writeAllBytes(image.getAbsolutePath(), buffer);
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