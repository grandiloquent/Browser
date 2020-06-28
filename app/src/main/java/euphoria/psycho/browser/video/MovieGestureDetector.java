package euphoria.psycho.browser.video;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;

public class MovieGestureDetector extends PlayerViewGestureDetector {
    private final MovieControllerOverlay mMovieControllerOverlay;
    private static final int MAX_VIDEO_STEP_TIME = 60 * 1000;

    public MovieGestureDetector(Context context, MovieControllerOverlay movieControllerOverlay) {
        super(context);
        mMovieControllerOverlay = movieControllerOverlay;


    }

    @Override
    public void adjustBrightness(double adjustPercent) {

    }

    @Override
    public void adjustVideoPosition(double adjustPercent, boolean forwardDirection) {


        if (adjustPercent < -1.0f) {
            adjustPercent = -1.0f;
        } else if (adjustPercent > 1.0f) {
            adjustPercent = 1.0f;
        }

        double positiveAdjustPercent = Math.max(adjustPercent, -adjustPercent);
        int targetTime = (int) (MAX_VIDEO_STEP_TIME * adjustPercent * (positiveAdjustPercent / 0.1));

        if (targetTime < 0) {
            targetTime = 0;
        }
        mMovieControllerOverlay.onSeekBy(targetTime);
    }

    @Override
    public void adjustVolumeLevel(double adjustPercent) {

    }

    @Override
    public Rect getPlayerViewRect() {
        return mMovieControllerOverlay.getVideoRect();
    }

    @Override
    public void onDoubleTap() {

    }

    @Override
    public void onGestureDone() {

    }

    @Override
    public boolean onSingleTap() {
        return mMovieControllerOverlay.onSingleTap();
    }
}
