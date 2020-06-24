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
import android.graphics.Color;
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
import android.widget.RemoteViews;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.Builder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import euphoria.psycho.browser.R;

public class MusicPlaybackService extends Service implements
        OnTimedTextListener, OnTimedMetaDataAvailableListener, OnSeekCompleteListener, OnPreparedListener, OnErrorListener, OnCompletionListener, OnBufferingUpdateListener, OnInfoListener {

    public static final String EXTRA_FILENAME = "filename";
    private static final String ACTION_NEXT = "action_next";
    private static final String ACTION_PREVIOUS = "action_previous";
    private static final String ACTION_STOP = "action_stop";
    private static final String CHANNEL_ID = "Browser_channel_01";
    private long mNotificationPostTime;
    private int mNotificationId;
    private MediaPlayer mMediaPlayer;
    private int mIndex;
    private File[] mMusics;
    WakeLock mWakeLock;

    public static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void onMediaTimeDiscontinuity(@NonNull MediaPlayer mediaPlayer, @NonNull MediaTimestamp mediaTimestamp) {

    }

    private Notification buildNotification(String content) {
        Builder builder;
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            builder = new Builder(this, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }
        if (mNotificationPostTime == 0) {
            mNotificationPostTime = System.currentTimeMillis();
        }
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.player_notification);

        views.setOnClickPendingIntent(R.id.notificationStop, buildPendingIntent(ACTION_STOP));
        views.setOnClickPendingIntent(R.id.notificationFForward, buildPendingIntent(ACTION_NEXT));
        views.setOnClickPendingIntent(R.id.notificationPrevious, buildPendingIntent(ACTION_PREVIOUS));
        builder.setSmallIcon(R.drawable.ic_stat_music_note)
                .setContentTitle(getString(R.string.notification_music_title))
                .setWhen(mNotificationPostTime)
                .setCustomContentView(views)
//                .setColor(0xff403f4d)
//                .setColorized(true)


        ;

        return builder.build();

    }

    private PendingIntent buildPendingIntent(String action) {
        ComponentName componentName = new ComponentName(this, MusicPlaybackService.class);
        Intent intent = new Intent(action);
        intent.setComponent(componentName);

        return PendingIntent.getService(this, 0, intent, 0);
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
        }
        try {
            mMediaPlayer.setDataSource(mMusics[mIndex].getAbsolutePath());
            mMediaPlayer.prepare();
        } catch (IOException e) {
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
        startForeground(mNotificationId, buildNotification(
                "123"
        ));

        mMusics = new File("/storage/0000-0000/song").listFiles();
        play();
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
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mMediaPlayer.start();
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
                if (VERSION.SDK_INT >= VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE);
                } else {
                    stopForeground(true);
                }
                stopSelf();
                break;
            case ACTION_NEXT:
                playNext(false);
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
