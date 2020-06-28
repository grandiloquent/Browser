/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package euphoria.psycho.browser.video;

import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import euphoria.psycho.browser.R;

/**
 * The playback controller for the Movie Player.
 */
public class MovieControllerOverlay extends CommonControllerOverlay implements
        AnimationListener { //

    private static final long START_HIDING_DELAY = 5000;
    private final Handler handler;
    private final Animation hideAnimation;
    private final Runnable startHidingRunnable;
    private boolean hidden;


    public MovieControllerOverlay(Context context) {
        super(context);

        handler = new Handler();
        startHidingRunnable = () -> startHiding();

        hideAnimation = AnimationUtils.loadAnimation(context, R.anim.player_out);
        hideAnimation.setAnimationListener(this);

        hide();
        setOnTouchListener(new MovieGestureDetector(getContext(), this));
    }


    public void onSeekBy(int targetTime) {
        mListener.onSeekBy(targetTime);
    }


    public boolean onSingleTap() {
        if (hidden) {
            show();
            return true;
        }
        return false;
    }

    private void cancelHiding() {
        handler.removeCallbacks(startHidingRunnable);
        mBackground.setAnimation(null);
        mTimeBar.setAnimation(null);
        //mPlayPauseReplayView.setAnimation(null);
    }

    private void maybeStartHiding() {
        cancelHiding();
        if (mState == State.PLAYING) {
            handler.postDelayed(startHidingRunnable, START_HIDING_DELAY);
        }
    }

    private void startHideAnimation(View view) {
        if (view.getVisibility() == View.VISIBLE) {
            view.startAnimation(hideAnimation);
        }
    }

    private void startHiding() {
        startHideAnimation(mBackground);
        startHideAnimation(mTimeBar);
        // startHideAnimation(mPlayPauseReplayView);
    }

    @Override
    protected void createTimeBar(Context context) {
        mTimeBar = new TimeBar(context, this);
    }

    @Override
    protected void updateViews() {
        if (hidden) {
            return;
        }
        super.updateViews();
    }

    @Override
    public void hide() {
        boolean wasHidden = hidden;
        hidden = true;
        super.hide();
        if (mListener != null && wasHidden != hidden) {
            mListener.onHidden();
        }
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        hide();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        // Do nothing.
    }

    @Override
    public void onAnimationStart(Animation animation) {
        // Do nothing.
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (hidden) {
            show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onScrubbingEnd(int time, int trimStartTime, int trimEndTime) {
        maybeStartHiding();
        super.onScrubbingEnd(time, trimStartTime, trimEndTime);
    }

    @Override
    public void onScrubbingMove(int time) {
        cancelHiding();
        super.onScrubbingMove(time);
    }

    // TimeBar listener

    @Override
    public void onScrubbingStart() {
        cancelHiding();
        super.onScrubbingStart();
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (super.onTouchEvent(event)) {
//            return true;
//        }
//

//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                cancelHiding();
//                if (mState == State.PLAYING || mState == State.PAUSED) {
//                    mListener.onPlayPause();
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                maybeStartHiding();
//                break;
//        }
//        return true;
//    }

    @Override
    public void show() {
        boolean wasHidden = hidden;
        hidden = false;
        super.show();
        if (mListener != null && wasHidden != hidden) {
            mListener.onShown();
        }
        maybeStartHiding();
    }
}
