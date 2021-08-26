package euphoria.psycho.browser.file;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.Environment;
import android.provider.Settings;
import android.util.Pair;
import android.view.inputmethod.InputMethodManager;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet;
import euphoria.psycho.browser.app.BottomSheet.OnClickListener;
import euphoria.psycho.browser.app.FunctionsMenu;
import euphoria.psycho.browser.app.JdActivity;
import euphoria.psycho.browser.app.SettingsActivity;
import euphoria.share.FileShare;

import static euphoria.psycho.browser.file.FileHelper.*;

public class BottomSheetHelper {
    public static Pair<Integer, String>[] createBottomSheetItems(Context context) {
        if (FileConstantsHelper.sIsHasSD) {
            return new Pair[]{
                    Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                    Pair.create(R.drawable.ic_sd_storage, context.getString(R.string.sd_storage)),
                    Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                    Pair.create(R.drawable.ic_info, context.getString(R.string.directory_info)),
                    Pair.create(R.drawable.ic_cleaning_service, context.getString(R.string.cleaning_service)),
                    Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
                    Pair.create(R.drawable.ic_sort, context.getString(R.string.sort)),
                    Pair.create(R.drawable.ic_action_keyboard, context.getString(R.string.change_input)),
                    Pair.create(R.drawable.ic_more_vert, context.getString(R.string.more))
            };
        } else {
            return new Pair[]{
                    Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                    Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                    Pair.create(R.drawable.ic_info, context.getString(R.string.directory_info)),
                    Pair.create(R.drawable.ic_cleaning_service, context.getString(R.string.cleaning_service)),
                    Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
                    Pair.create(R.drawable.ic_sort, context.getString(R.string.sort)),
                    Pair.create(R.drawable.ic_action_keyboard, context.getString(R.string.change_input)),
                    Pair.create(R.drawable.ic_more_vert, context.getString(R.string.more))
            };
        }
    }

    public static void createFunctionsMenu(Activity activity, FileManager fileManager) {
        FunctionsMenu functionsMenu = new FunctionsMenu(activity, fileManager.getView(), new OnClickListener() {
            @Override
            public void onClicked(Pair<Integer, String> item) {
                switch (item.first) {
                    case R.drawable.ic_twitter:
                        extractTwitterVideo(activity);
                        break;
                    case R.drawable.ic_youtube:
                        startYouTube(activity);
                        break;
                    case R.drawable.ic_film:
                        startVideoServer(activity);
                        break;
                    case R.drawable.ic_translate:
                        TranslatorHelper.youdaoChinese(activity);
                        break;
                    case R.drawable.ic_g_translate:
                        TranslatorHelper.google(activity);
                        break;
                    case R.drawable.ic_action_shopping_cart:
                        Intent jd = new Intent(activity, JdActivity.class);
                        jd.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivity(jd);
                        break;

                }
            }
        });
        functionsMenu.showDialog(createFunctionsMenuItems(activity));
    }

    public static Pair<Integer, String>[] createFunctionsMenuItems(Context context) {
        return new Pair[]{
                Pair.create(R.drawable.ic_film, context.getString(R.string.video_server)),
                Pair.create(R.drawable.ic_twitter, context.getString(R.string.twitter)),
                Pair.create(R.drawable.ic_youtube, context.getString(R.string.youtube)),
                Pair.create(R.drawable.ic_translate, context.getString(R.string.youdao)),
                Pair.create(R.drawable.ic_g_translate, context.getString(R.string.google)),
                Pair.create(R.drawable.ic_action_shopping_cart, context.getString(R.string.jd))
        };
    }

    public static void showBottomSheet(Activity activity, Pair<Integer, String>[] items, FileManager fileManager) {
        BottomSheet bottomSheet = new BottomSheet(activity)
                .setOnClickListener(item -> {
                    switch (item.first) {
                        case R.drawable.ic_storage:
                            fileManager.openDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
                            break;
                        case R.drawable.ic_sd_storage:
                            fileManager.openDirectory(FileShare.sSDPath);
                            break;
                        case R.drawable.ic_settings:
                            Intent settingsActivity = new Intent(activity, SettingsActivity.class);
                            activity.startActivity(settingsActivity);
                            break;
                        case R.drawable.ic_info:
                            showDirectoryInfo(activity, fileManager.getDirectory());
                            break;
                        case R.drawable.ic_cleaning_service:
                            cleaningDirectory(activity, fileManager);
                            break;
                        case R.drawable.ic_more_vert:
                            createFunctionsMenu(activity, fileManager);
                            break;
                        case R.drawable.ic_create_new_folder:
                            createNewDirectory(activity, fileManager);
                            break;
                        case R.drawable.ic_sort:
                            showSortDialog(activity, fileManager);
                            break;
                        case R.drawable.ic_action_keyboard:
                            //Intent intent = new Intent("");

                            // com.android.settings.Settings$LanguageAndInputSettingsActivity
                            //intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$EdgeModeSettingsActivity"));
                            //activity.startActivity(intent);

                            activity.getSystemService(InputMethodManager.class).showInputMethodPicker();
                            break;

                    }
                    fileManager.setBottomSheet(null);
                });
        fileManager.setBottomSheet(bottomSheet);
        bottomSheet.showDialog(items);
    }

}
