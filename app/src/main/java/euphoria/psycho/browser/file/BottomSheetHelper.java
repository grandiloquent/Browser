package euphoria.psycho.browser.file;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Pair;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet;
import euphoria.psycho.browser.app.BottomSheet.OnClickListener;
import euphoria.psycho.browser.app.FunctionsMenu;
import euphoria.psycho.browser.app.SettingsActivity;

import static euphoria.psycho.browser.file.FileHelper.*;

public class BottomSheetHelper {
    public static Pair<Integer, String>[] createBottomSheetItems(Context context) {
        if (FileConstantsHelper.sIsHasSD) {
            return new Pair[]{
                    Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                    Pair.create(R.drawable.ic_sd_storage, context.getString(R.string.sd_storage)),
                    Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                    Pair.create(R.drawable.ic_info, context.getString(R.string.directory_info)),
                    Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
                    Pair.create(R.drawable.ic_sort, context.getString(R.string.sort)),
                    Pair.create(R.drawable.ic_more_vert, context.getString(R.string.more))
            };
        } else {
            return new Pair[]{
                    Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                    Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                    Pair.create(R.drawable.ic_info, context.getString(R.string.directory_info)),
                    Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
                    Pair.create(R.drawable.ic_sort, context.getString(R.string.sort)),
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

                }
            }
        });
        functionsMenu.showDialog(createFunctionsMenuItems(activity));
    }

    public static void showBottomSheet(Activity activity, Pair<Integer, String>[] items, FileManager fileManager) {
        BottomSheet bottomSheet = new BottomSheet(activity)
                .setOnClickListener(item -> {
                    switch (item.first) {
                        case R.drawable.ic_storage:
                            fileManager.openDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
                            break;
                        case R.drawable.ic_sd_storage:
                            fileManager.openDirectory(FileConstantsHelper.sSDPath);
                            break;
                        case R.drawable.ic_settings:
                            Intent settingsActivity = new Intent(activity, SettingsActivity.class);
                            activity.startActivity(settingsActivity);
                            break;
                        case R.drawable.ic_info:
                            showDirectoryInfo(activity, fileManager.getDirectory());
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
                    }
                    fileManager.setBottomSheet(null);
                });
        fileManager.setBottomSheet(bottomSheet);
        bottomSheet.showDialog(items);
    }

}
