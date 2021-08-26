package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.app.SampleDownloadActivity;
import euphoria.psycho.browser.app.ServerActivity;
import euphoria.psycho.browser.app.TwitterHelper;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.video.VideoActivity;
import euphoria.psycho.share.ContextUtils;
import euphoria.psycho.browser.music.MusicPlaybackService;
import euphoria.psycho.share.DialogUtils;
import euphoria.psycho.share.FormatUtils;
import euphoria.psycho.share.StringUtils;
import euphoria.psycho.share.ThreadUtils;
import euphoria.share.FileShare;
import euphoria.share.Logger;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static euphoria.psycho.browser.file.FileConstantsHelper.*;
import static euphoria.psycho.share.ContextUtils.getApplicationContext;
import static euphoria.psycho.share.StringUtils.substringAfterLast;

public class FileHelper {
    // https://developer.android.com/guide/topics/media/media-formats
    // 匹配文件名是否为音频格式

    /*
    ["apk",
"excel",
"folder",
"image",
"music",
"others",
"pdf",
"pps",
"text",
"vcf",
"video",
"word",
"zip"]
    * */
    private static List<String> sSortItems;


    public static void copySelection(FileManager fileManager, FileItem item) {
        List<FileItem> fileItems = new ArrayList<>();
        fileItems.add(item);
        fileManager.getFileOperationManager().addToCopy(fileItems);
        fileManager.getSelectionDelegate().clearSelection();
    }


    public static void copySelections(FileManager fileManager) {
        List<FileItem> fileItems = fileManager.getSelectionDelegate().getSelectedItemsAsList();
        fileManager.getFileOperationManager().addToCopy(fileItems);
        fileManager.getSelectionDelegate().clearSelection();
    }


    public static void createNewDirectory(Activity activity, FileManager fileManager) {
        EditText editText = new EditText(activity);
        editText.requestFocus();
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle(R.string.create_new_folder)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    String filename = editText.getText().toString();
                    File dir = new File(fileManager.getDirectory(), filename);
                    if (!dir.isDirectory()) {
                        dir.mkdir();
                    }
                    fileManager.refresh();
                    dialogInterface.dismiss();
                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    public static void cutSelection(FileManager fileManager, FileItem item) {
        List<FileItem> fileItems = fileManager.getSelectionDelegate().getSelectedItemsAsList();
        fileManager.getFileOperationManager().addToCut(fileItems);
        fileManager.getSelectionDelegate().clearSelection();
    }

    public static void cutSelections(FileManager fileManager) {
        List<FileItem> fileItems = fileManager.getSelectionDelegate().getSelectedItemsAsList();
        fileManager.getFileOperationManager().addToCut(fileItems);
        fileManager.getSelectionDelegate().clearSelection();
    }


    public static void delete(FileManager fileManager, FileItem item) {
        AlertDialog dialog = new AlertDialog.Builder(fileManager.getActivity())
                .setTitle(R.string.question_dialog_title)
                .setMessage(fileManager.getActivity().getString(R.string.question_delete_file_item_message, substringAfterLast(item.getUrl(), '/')))
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    NativeHelper.deleteFileSystem(item.getUrl());
                    fileManager.getSelectionDelegate().clearSelection();
                    fileManager.getFileAdapter().initialize();
                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.show();
    }


    public static void deleteSelections(FileManager fileManager) {
        AlertDialog dialog = new AlertDialog.Builder(fileManager.getActivity())
                .setTitle(R.string.question_dialog_title)
                .setMessage(fileManager.getActivity().getString(R.string.question_delete_selections_file_item_message,
                        fileManager.getSelectionDelegate().getSelectedItemsAsList().get(0).getTitle()))
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    for (FileItem fileItem : fileManager.getSelectionDelegate().getSelectedItems()) {
                        fileManager.getFileAdapter().markItemForRemoval(fileItem);
                    }
                    fileManager.getFileAdapter().removeItems();
                    fileManager.getSelectionDelegate().clearSelection();
                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.show();
    }

    public static void downloadFromUrl(Context context, String youtubeDlUrl, String downloadTitle, String fileName) {
        Uri uri = Uri.parse(youtubeDlUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(downloadTitle);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    public static void extractTwitterVideo(Activity activity) {
        ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMessage(activity.getText(R.string.extracting));
        dialog.show();
        ThreadUtils.postOnBackgroundThread(() -> {
            try {
                CharSequence twitterUrl = ContextUtils.getClipboardString();
                if (twitterUrl != null) {
                    String id = substringAfterLast(twitterUrl.toString(), "/");
                    if (StringUtils.isDigits(id)) {
                        List<TwitterVideo> twitterVideos = TwitterHelper.extractTwitterVideo(id);
                        activity.runOnUiThread(() -> {
                            dialog.dismiss();
                            TwitterHelper.showDialog(twitterVideos, activity);
                        });
                    }
                }
            } catch (Exception e) {
                ThreadUtils.postOnMainThread(() -> {
                    dialog.dismiss();
                    ContextUtils.showExceptionDialog(activity, e);
                });
            }
        });
    }

    public static void extractZipFile(FileManager fileManager, FileItem item) {
        Dialog progress = DialogUtils.buildProgressDialog(fileManager.getActivity(),
                fileManager.getActivity().getString(R.string.progress_dialog_extract_zip_title),
                fileManager.getActivity().getString(R.string.progress_dialog_extract_zip_content));
        progress.show();
        ThreadUtils.postOnBackgroundThread(() -> {
            String fileName = StringUtils.substringBeforeLast(item.getTitle(), '.');
            File targetDirectory = new File(fileManager.getDirectory(), fileName);
            boolean ret = true;
            if (!targetDirectory.exists()) {
                ret = targetDirectory.mkdir();
            }
            if (!ret) {
                progress.dismiss();
                return;
            }
            NativeHelper.extractToDirectory(item.getUrl(), targetDirectory.getAbsolutePath());
            ThreadUtils.postOnMainThread(() -> {
                progress.dismiss();
                fileManager.getFileAdapter().initialize();
            });
        });
    }


    public static long getFileSize(File file, boolean isShowHidden) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            return files == null ? 0 : (isShowHidden ? files.length : countFiles(files));
        }
        return file.length();
    }

    public static int getFileType(File file) {
        if (file.isDirectory()) {
            return TYPE_FOLDER;
        }
        if (isMusic(file)) {
            return TYPE_MUSIC;
        }
        String extension = substringAfterLast(file.getName(), '.');
        switch (extension) {
            case "apk":
                return TYPE_APK;
            case "excel":
                return TYPE_EXCEL;
            case "bmp":
            case "gif":
            case "jpg":
            case "png":
            case "webp":
            case "heic":
            case "heif":
            case "jpeg":
                return TYPE_IMAGE;
            case "pdf":
                return TYPE_PDF;
            case "pps":
                return TYPE_PPS;
            case "txt":
            case "html":
            case "sql":
            case "c":
            case "css":
            case "cs":
            case "cc":
            case "h":
            case "js":
                return TYPE_TEXT;
            case "vcf":
                return TYPE_VCF;
            case "3gp":
            case "mp4":
            case "webm":
            case "ts":
            case "mkv":
                return TYPE_VIDEO;
            case "word":
                return TYPE_WORD;
            case "epub":
            case "zip":
                return TYPE_ZIP;
            default:
                return TYPE_OTHERS;
        }
    }



    public static boolean isMusic(File f) {
        return sMusicPattern.matcher(f.getName()).find();
    }


    public static boolean isVideo(File f) {
        return sVideoPattern.matcher(f.getName()).find();
    }


    public static void openUrl(Activity activity, FileItem fileItem) {
        if (sMusicPattern.matcher(fileItem.getTitle()).find()) {
            Intent service = new Intent(activity, MusicPlaybackService.class);
            service.setAction(MusicPlaybackService.ACTION_FILES);
            service.putExtra(MusicPlaybackService.EXTRA_FILENAME, fileItem.getUrl());
            activity.startService(service);
            return;
        }
        if (sVideoPattern.matcher(fileItem.getTitle()).find()) {
            Intent movieActivity = new Intent(activity, VideoActivity.class);
            movieActivity.setData(Uri.fromFile(new File(fileItem.getUrl())));
            activity.startActivity(movieActivity);
            return;
        }
        String url = fileItem.getUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(url)),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        substringAfterLast(url, '.')
                ));
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.open)));
    }

    public static void rename(FileManager fileManager, FileItem item) {
        EditText editText = new EditText(fileManager.getActivity());
        editText.setText(item.getTitle());
        editText.requestFocus();
        String fileName = item.getTitle();
        int index = 0;
        if ((index = fileName.lastIndexOf(".")) != -1) {
            editText.setSelection(0, index);
        }
        AlertDialog dialog = new Builder(fileManager.getActivity())
                .setTitle(R.string.rename_file_item)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    File src = new File(item.getUrl());
                    Logger.d(String.format("rename: %s", src));
                    String targetFileName = editText.getText().toString();
                    File target = new File(fileManager.getDirectory(), targetFileName);
                    src.renameTo(target);
                    try {
                        DocumentsContract.renameDocument(fileManager.getActivity()
                                        .getContentResolver(),
                                Uri.parse(""), "");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    fileManager.getFileAdapter().initialize();
                }).setNegativeButton(android.R.string.cancel, (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                })
                .create();
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    public static void selectSameType(FileManager fileManager) {
        List<FileItem> fileItems = fileManager.getSelectionDelegate().getSelectedItemsAsList();
        if (fileItems.size() > 0) {
            FileItem fileItem = fileItems.get(0);
            List<FileItem> items = fileManager.getFileAdapter().getFileItems();
            Set<FileItem> fileItemSet = new HashSet<>();
            if (fileItem.getType() == TYPE_FOLDER) {
                for (FileItem f : items) {
                    if (f.getType() == TYPE_FOLDER) {
                        fileItemSet.add(f);
                    }
                }
            } else {
                String extension = substringAfterLast(fileItem.getUrl(), ".");
                for (FileItem f : items) {
                    if (substringAfterLast(f.getUrl(), ".").equals(extension)) {
                        fileItemSet.add(f);
                    }
                }
            }
            fileManager.getSelectionDelegate().setSelectedItems(fileItemSet);
        }
    }

    public static void showDirectoryInfo(Activity activity, String directory) {
//            Log.e("TAG/", "Debug: showDirectoryInfo, \n" + Files.walk(new File(Environment.getExternalStorageDirectory(),"Videos").toPath()).mapToLong(p -> p.toFile().length()).sum());
        File[] files = new File(directory).listFiles(File::isDirectory);
        if (files == null) return;
        List<Pair<Long, String>> pairList = new ArrayList<>();
        for (File f : files) {
            pairList.add(Pair.create(NativeHelper.dirSize(f.getAbsolutePath()), f.getName()));
        }
        Collections.sort(pairList, (o1, o2) -> {
            long a = o1.first - o2.first;
            if (a > 0) return -1;
            if (a < 0) return 1;
            return 0;
        });
        StringBuilder stringBuilder = new StringBuilder();
        for (Pair<Long, String> p : pairList) {
            stringBuilder.append(FormatUtils.formatFileSize(p.first))
                    .append(" = ")
                    .append(p.second)
                    .append('\n');
        }
        new AlertDialog.Builder(activity)
                .setMessage(stringBuilder.toString())
                .show();
    }

    public static void showSortDialog(Activity activity, FileManager fileManager) {
        if (sSortItems == null) {
            sSortItems = new ArrayList<>();
            // ["name","size","type","data_modified","ascending","descending"]
            sSortItems.add(activity.getString(R.string.sort_by_name));
            sSortItems.add(activity.getString(R.string.sort_by_size));
            sSortItems.add(activity.getString(R.string.sort_by_type));
            sSortItems.add(activity.getString(R.string.sort_by_data_modified));
            sSortItems.add(activity.getString(R.string.sort_by_ascending));
            sSortItems.add(activity.getString(R.string.sort_by_descending));
        }
        new Builder(activity)
                .setTitle(R.string.sort)
                .setItems(sSortItems.toArray(new String[0]), (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            fileManager.setSortType((fileManager.getSortType() & SORT_BY_ASCENDING) | SORT_BY_NAME);
                            break;
                        case 1:
                            fileManager.setSortType((fileManager.getSortType() & SORT_BY_ASCENDING) | SORT_BY_SIZE);
                            break;
                        case 2:
                            fileManager.setSortType((fileManager.getSortType() & SORT_BY_ASCENDING) | SORT_BY_TYPE);
                            break;
                        case 3:
                            fileManager.setSortType((fileManager.getSortType() & SORT_BY_ASCENDING) | SORT_BY_DATA_MODIFIED);
                            break;
                        case 4:
                            fileManager.setSortType((fileManager.getSortType() & 31) | SORT_BY_ASCENDING);
                            break;
                        case 5:
                            fileManager.setSortType((fileManager.getSortType() & 31));
                            break;
                    }
                    Log.e("TAG/", "Debug: showSortDialog, \n" + fileManager.getDirectory());
                    fileManager.sortBy();
                    //Log.e("TAG/", "Debug: showSortDialog, \n" + fileManager.getDirectory());
                    dialogInterface.dismiss();
                })
                .show();
    }

    public static void startVideoServer(Activity activity) {
        Intent intent = new Intent(activity, ServerActivity.class);
        activity.startActivity(intent);
    }

    public static void startYouTube(Activity activity) {
        Intent intent = new Intent(activity, SampleDownloadActivity.class);
        activity.startActivity(intent);
    }


    private static int countFiles(File[] files) {
        int i = 0;
        for (File file : files) {
            if (!file.getName().startsWith("."))
                i++;
        }
        return i;
    }


    public static void cleaningDirectory(Activity activity, FileManager fileManager) {
        File[] files = new File(fileManager.getDirectory()).listFiles(file -> file.isFile());
        if (files == null || files.length == 0) return;
        for (File f : files) {
            File p = new File(fileManager.getDirectory(), substringAfterLast(f.getName(), ".").toUpperCase());
            p.mkdir();
            p = new File(p, f.getName());
            if (!p.exists()) {
                f.renameTo(p);
            }
        }
        fileManager.refresh();
    }

    // Android 10+ 无法在内部储存根目录创建目录
    public static File getStaticResourceDirectory() {
        return new File(getApplicationContext().getExternalFilesDir(""), "FileServer");
    }
}