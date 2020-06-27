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

import android.view.View;

public interface ControllerOverlay {

  /**
   * @return The overlay view that should be added to the player.
   */
  View getView();

  void setCanReplay(boolean canReplay);

  void setListener(Listener listener);

  void setTimes(int currentTime, int totalTime,
                int trimStartTime, int trimEndTime);

  void show();

  void showEnded();

  void showErrorMessage(String message);

  void showLoading();

  void showPaused();

  void showPlaying();

  interface Listener {
    void onHidden();

    void onPlayPause();

    void onReplay();

    void onSeekEnd(int time, int trimStartTime, int trimEndTime);

    void onSeekMove(int time);

    void onSeekStart();

    void onShown();
  }
}
