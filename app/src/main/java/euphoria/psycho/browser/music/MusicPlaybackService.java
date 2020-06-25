package euphoria.psycho.browser.music;

import android.app.Notification;
import android.app.Notification.MediaStyle;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.DrmInfo;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnDrmConfigHelper;
import android.media.MediaPlayer.OnDrmInfoListener;
import android.media.MediaPlayer.OnDrmPreparedListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnMediaTimeDiscontinuityListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnSubtitleDataListener;
import android.media.MediaPlayer.OnTimedMetaDataAvailableListener;
import android.media.MediaPlayer.OnTimedTextListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.media.MediaTimestamp;
import android.media.SubtitleData;
import android.media.TimedMetaData;
import android.media.TimedText;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.CalendarContract.Colors;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.base.BitmapUtils;
import euphoria.psycho.browser.base.Share;

public class MusicPlaybackService extends Service implements
        OnTimedTextListener, OnTimedMetaDataAvailableListener, OnSeekCompleteListener, OnPreparedListener, OnErrorListener, OnCompletionListener, OnBufferingUpdateListener, OnInfoListener {

    public static final String ACTION_FILES = "action_files";
    public static final String EXTRA_FILENAME = "filename";
    private static final String ACTION_NEXT = "action_next";
    private static final String ACTION_PAUSE = "action_pause";
    private static final String ACTION_PREVIOUS = "action_previous";
    private static final String ACTION_STOP = "action_stop";
    private static final String CHANNEL_ID = "Browser_channel_01";
    private long mNotificationPostTime;
    private int mNotificationId;
    private MediaPlayer mMediaPlayer;
    private int mIndex;
    private File[] mMusics;
    WakeLock mWakeLock;
    NotificationManager mNotificationManager;
    private RemoteViews mRemoteViews;

    public static String dumpMediaMeta(String path) {
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(path);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("METADATA_KEY_CD_TRACK_NUMBER").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER)).append("\n");
        stringBuilder.append("METADATA_KEY_ALBUM").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)).append("\n");
        stringBuilder.append("METADATA_KEY_ARTIST").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)).append("\n");
        stringBuilder.append("METADATA_KEY_AUTHOR").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR)).append("\n");
        stringBuilder.append("METADATA_KEY_COMPOSER").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPOSER)).append("\n");
        stringBuilder.append("METADATA_KEY_DATE").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DATE)).append("\n");
        stringBuilder.append("METADATA_KEY_GENRE").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)).append("\n");
        stringBuilder.append("METADATA_KEY_TITLE").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)).append("\n");
        stringBuilder.append("METADATA_KEY_YEAR").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR)).append("\n");
        stringBuilder.append("METADATA_KEY_DURATION").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)).append("\n");
        stringBuilder.append("METADATA_KEY_NUM_TRACKS").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS)).append("\n");
        stringBuilder.append("METADATA_KEY_WRITER").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_WRITER)).append("\n");
        stringBuilder.append("METADATA_KEY_MIMETYPE").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)).append("\n");
        stringBuilder.append("METADATA_KEY_ALBUMARTIST").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST)).append("\n");
        stringBuilder.append("METADATA_KEY_DISC_NUMBER").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER)).append("\n");
        stringBuilder.append("METADATA_KEY_COMPILATION").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION)).append("\n");
        stringBuilder.append("METADATA_KEY_HAS_AUDIO").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO)).append("\n");
        stringBuilder.append("METADATA_KEY_HAS_VIDEO").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)).append("\n");
        stringBuilder.append("METADATA_KEY_VIDEO_WIDTH").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)).append("\n");
        stringBuilder.append("METADATA_KEY_VIDEO_HEIGHT").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)).append("\n");
        stringBuilder.append("METADATA_KEY_BITRATE").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)).append("\n");
        stringBuilder.append("METADATA_KEY_LOCATION").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_LOCATION)).append("\n");
        stringBuilder.append("METADATA_KEY_VIDEO_ROTATION").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)).append("\n");
        stringBuilder.append("METADATA_KEY_CAPTURE_FRAMERATE").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)).append("\n");
        stringBuilder.append("METADATA_KEY_HAS_IMAGE").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_IMAGE)).append("\n");
        stringBuilder.append("METADATA_KEY_IMAGE_COUNT").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_COUNT)).append("\n");
        stringBuilder.append("METADATA_KEY_IMAGE_PRIMARY").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_PRIMARY)).append("\n");
        stringBuilder.append("METADATA_KEY_IMAGE_WIDTH").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH)).append("\n");
        stringBuilder.append("METADATA_KEY_IMAGE_HEIGHT").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT)).append("\n");
        stringBuilder.append("METADATA_KEY_IMAGE_ROTATION").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_ROTATION)).append("\n");
        stringBuilder.append("METADATA_KEY_VIDEO_FRAME_COUNT").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)).append("\n");
        stringBuilder.append("METADATA_KEY_EXIF_OFFSET").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_EXIF_OFFSET)).append("\n");
        stringBuilder.append("METADATA_KEY_EXIF_LENGTH").append(" : ").append(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_EXIF_LENGTH)).append("\n");

        return stringBuilder.toString();
    }

    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void onMediaTimeDiscontinuity(@NonNull MediaPlayer mediaPlayer, @NonNull MediaTimestamp mediaTimestamp) {

    }

    private void actionStop() {
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        stopSelf();
    }

    private Notification buildNotification() {
        Builder builder;
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            builder = new Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        if (mNotificationPostTime == 0) {
            mNotificationPostTime = System.currentTimeMillis();
        }

        if (mRemoteViews == null) {
            mRemoteViews = buildRemoteViews();
        }

        rendererRemoteViews();

        builder.setSmallIcon(R.drawable.ic_stat_music_note)
                .setContentTitle(getString(R.string.notification_music_title))
                .setWhen(mNotificationPostTime)
                .setCustomContentView(mRemoteViews);


        return builder.build();

    }

    private PendingIntent buildPendingIntent(String action) {
        ComponentName componentName = new ComponentName(this, MusicPlaybackService.class);
        Intent intent = new Intent(action);
        intent.setComponent(componentName);

        return PendingIntent.getService(this, 0, intent, 0);
    }

    private void rendererRemoteViews() {
        if (mMusics != null && mMusics.length > 0) {
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(mMusics[mIndex].getAbsolutePath());
            String songName = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            if (songName == null) {
                songName = Share.substringBefore(mMusics[mIndex].getName(), '-');
            }
            mRemoteViews.setTextViewText(R.id.notificationSongName, songName);
            String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            if (artist == null) {
                artist = Share.substringAfterLast(mMusics[mIndex].getName(), '-');
            }
            mRemoteViews.setTextViewText(R.id.notificationArtist, artist);
            if (metadataRetriever.getEmbeddedPicture() != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(
                        metadataRetriever.getEmbeddedPicture(),
                        0,
                        metadataRetriever.getEmbeddedPicture().length
                );
                mRemoteViews.setImageViewBitmap(R.id.notificationCover, bitmap);
            }
        }
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mRemoteViews.setImageViewResource(R.id.notificationPlayPause, R.drawable.ic_pause_white_24dp);
            } else {
                mRemoteViews.setImageViewResource(R.id.notificationPlayPause, R.drawable.ic_play_arrow_white_24dp);
            }
        }
    }

    private RemoteViews buildRemoteViews() {
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.player_notification);
        views.setOnClickPendingIntent(R.id.notificationStop, buildPendingIntent(ACTION_STOP));
        views.setOnClickPendingIntent(R.id.notificationFForward, buildPendingIntent(ACTION_NEXT));
        views.setOnClickPendingIntent(R.id.notificationPrevious, buildPendingIntent(ACTION_PREVIOUS));
        views.setOnClickPendingIntent(R.id.notificationPlayPause, buildPendingIntent(ACTION_PAUSE));
        return views;
    }

    @RequiresApi(api = VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Browser",
                NotificationManager.IMPORTANCE_LOW
        );
        getNotificationManager(this).createNotificationChannel(channel);
    }

    private void loadFiles(String filename) {
        File music = new File(filename);
        mMusics = music.getParentFile().listFiles(file -> file.isFile() && file.getName().endsWith(".mp3"));
        if (mMusics == null) return;
        for (int i = 0; i < mMusics.length; i++) {
            if (mMusics[i].getAbsolutePath().equals(filename)) {
                mIndex = i;
                break;
            }
        }
        play();
    }

    private void notifyChange() {
        mNotificationManager.notify(mNotificationId, buildNotification());
    }

    private void pause() {
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
        else
            mMediaPlayer.start();
        notifyChange();
    }

    private void play() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnTimedTextListener(this);
            mMediaPlayer.setOnTimedMetaDataAvailableListener(this);
            mMediaPlayer.setOnSeekCompleteListener(this);
            mMediaPlayer.setOnPreparedListener(this);
            if (VERSION.SDK_INT >= VERSION_CODES.P) {
                mMediaPlayer.setOnMediaTimeDiscontinuityListener(this::onMediaTimeDiscontinuity);
            }
            mMediaPlayer.setOnInfoListener(this);
            mMediaPlayer.setOnErrorListener(this);
            mMediaPlayer.setOnCompletionListener(this);
            mMediaPlayer.setOnBufferingUpdateListener(this);
        } else {
            mMediaPlayer.reset();
        }
        try {
            mMediaPlayer.setDataSource(mMusics[mIndex].getAbsolutePath());


            mMediaPlayer.prepare();
        } catch (IOException e) {
            Toast.makeText(this, "无法播放： " + mMusics[mIndex].getAbsolutePath(), Toast.LENGTH_LONG).show();
        }

    }

    private void playNext(boolean previous) {
        if (previous) {
            if (mIndex - 1 < 0) {
                mIndex = 0;
            } else {
                mIndex--;
            }
        } else {
            if (mIndex + 1 < mMusics.length) {
                mIndex++;
            } else {
                mIndex = 0;
            }
        }
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(mMusics[mIndex].getAbsolutePath());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopPlay() {
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {

    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playNext(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Browser::PlayerWakelockTag");
        mWakeLock.acquire();

        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            createNotificationChannel();
        }
        mNotificationId = hashCode();

        startForeground(mNotificationId, buildNotification());
        mNotificationManager = getNotificationManager(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopPlay();
        if (mWakeLock != null) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Log.e("TAG/", "Debug: onError, \n" + i + " " + i1);
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
        notifyChange();
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            return START_NOT_STICKY;
        }
        switch (action) {
            case ACTION_STOP:
                actionStop();
                break;
            case ACTION_FILES:
                loadFiles(intent.getStringExtra(EXTRA_FILENAME));
                break;
            case ACTION_NEXT:
                playNext(false);
                break;
            case ACTION_PAUSE:
                pause();
                break;
            case ACTION_PREVIOUS:
                playNext(true);
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTimedMetaDataAvailable(MediaPlayer mediaPlayer, TimedMetaData timedMetaData) {

    }

    @Override
    public void onTimedText(MediaPlayer mediaPlayer, TimedText timedText) {

    }
}
