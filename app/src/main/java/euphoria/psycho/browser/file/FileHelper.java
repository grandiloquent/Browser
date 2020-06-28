package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.RequiresApi;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.app.SampleDownloadActivity;
import euphoria.psycho.browser.app.ServerActivity;
import euphoria.psycho.browser.app.TwitterHelper;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;
import euphoria.psycho.browser.music.MusicPlaybackService;
import euphoria.psycho.browser.video.MovieActivity;
import euphoria.psycho.browser.widget.SelectionDelegate;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static euphoria.psycho.browser.file.FileConstantsHelper.*;

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

    public static void cleanDirectory(final File directory) throws IOException {
        final File[] files = verifiedListFiles(directory);

        IOException exception = null;
        for (final File file : files) {
            try {
                forceDelete(file);
            } catch (final IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    @RequiresApi(api = VERSION_CODES.O)
    public static void copyDirectory(final File srcDir, final File destDir,
                                     final FileFilter filter, final boolean preserveFileDate) throws IOException {
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            final File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<>(srcFiles.length);
                for (final File srcFile : srcFiles) {
                    final File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
    }

    @RequiresApi(api = VERSION_CODES.O)
    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    @RequiresApi(api = VERSION_CODES.O)
    public static void copyDirectory(final File srcDir, final File destDir,
                                     final boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

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


    public static Pair<Integer, String>[] createFunctionsMenuItems(Context context) {
        return new Pair[]{
                Pair.create(R.drawable.ic_film, context.getString(R.string.video_server)),
                Pair.create(R.drawable.ic_twitter, context.getString(R.string.twitter)),
                Pair.create(R.drawable.ic_youtube, context.getString(R.string.youtube)),
                Pair.create(R.drawable.ic_translate, context.getString(R.string.youdao)),
                Pair.create(R.drawable.ic_g_translate, context.getString(R.string.google)),
        };
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
                .setMessage(fileManager.getActivity().getString(R.string.question_delete_file_item_message, Share.substringAfterLast(item.getUrl(), '/')))
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

    public static void deleteDirectory(final File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        if (!isSymlink(directory)) {
            cleanDirectory(directory);
        }

        if (!directory.delete()) {
            final String message =
                    "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
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
        new Thread(() -> {
            try {
                CharSequence twitterUrl = Share.getClipboardString();
                if (twitterUrl != null) {
                    String id = Share.substringAfterLast(twitterUrl.toString(), "/");
                    if (Share.isDigits(id)) {
                        List<TwitterVideo> twitterVideos = TwitterHelper.extractTwitterVideo(id);
                        activity.runOnUiThread(() -> {
                            dialog.dismiss();
                            TwitterHelper.showDialog(twitterVideos, activity);
                        });
                    }
                }
            } catch (Exception e) {
                activity.runOnUiThread(() -> {
                    dialog.dismiss();
                    Share.showExceptionDialog(activity, e);
                });
            }
        }).start();
    }

    public static void extractZipFile(FileManager fileManager, FileItem item) {
        File targetDirectory = new File(fileManager.getDirectory(), Share.substringBeforeLast(item.getTitle(), '.'));
        if (!targetDirectory.exists()) {
            targetDirectory.mkdir();
        }

        NativeHelper.extractToDirectory(item.getUrl(), targetDirectory.getAbsolutePath());
        fileManager.getFileAdapter().initialize();
    }

    public static void forceDelete(final File file) throws IOException {
//        final Counters.PathCounters deleteCounters;
//        try {
//            deleteCounters = PathUtils.delete(file.toPath());
//        } catch (IOException e) {
//            throw new IOException("Unable to delete file: " + file, e);
//        }
//
//        if (deleteCounters.getFileCounter().get() < 1 && deleteCounters.getDirectoryCounter().get() < 1) {
//            // didn't find a file to delete.
//            throw new FileNotFoundException("File does not exist: " + file);
//        }
//
        file.delete();
    }

    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError ignored) {
            }
        }
        return null;
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
        String extension = Share.substringAfterLast(file.getName(), '.');
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
            case "zip":
                return TYPE_ZIP;
            default:
                return TYPE_OTHERS;
        }
    }

    public static String getSDPath() {
        return sSDPath;
    }

    public static void initialize(Context context) {
        sIsHasSD = (sSDPath = getExternalStoragePath(context)) != null;
    }

    public static boolean isMusic(File f) {
        return sMusicPattern.matcher(f.getName()).find();
    }

    @RequiresApi(api = VERSION_CODES.O)
    public static boolean isSymlink(final File file) {
        Objects.requireNonNull(file, "file");
        return Files.isSymbolicLink(file.toPath());
    }

    public static boolean isVideo(File f) {
        return sVideoPattern.matcher(f.getName()).find();
    }

    @RequiresApi(api = VERSION_CODES.O)
    public static void moveDirectory(final File srcDir, final File destDir) throws IOException {
        if (!srcDir.isDirectory()) {
            throw new IOException("Source '" + srcDir + "' is not a directory");
        }
        if (destDir.exists()) {
            throw new IOException("Destination '" + destDir + "' already exists");
        }
        final boolean rename = srcDir.renameTo(destDir);
        if (!rename) {
            if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath() + File.separator)) {
                throw new IOException("Cannot move directory: " + srcDir + " to a subdirectory of itself: " + destDir);
            }
            copyDirectory(srcDir, destDir);
            deleteDirectory(srcDir);
            if (srcDir.exists()) {
                throw new IOException("Failed to delete original directory '" + srcDir +
                        "' after copy to '" + destDir + "'");
            }
        }
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
            Intent movieActivity = new Intent(activity, MovieActivity.class);
            movieActivity.setData(Uri.fromFile(new File(fileItem.getUrl())));
            activity.startActivity(movieActivity);
            return;
        }

        String url = fileItem.getUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(url)),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        Share.substringAfterLast(url, '.')
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
        AlertDialog dialog = new AlertDialog.Builder(fileManager.getActivity())
                .setTitle(R.string.rename_file_item)
                .setView(editText)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    File src = new File(item.getUrl());
                    String targetFileName = editText.getText().toString();
                    File target = new File(fileManager.getDirectory(), targetFileName);
                    src.renameTo(target);
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
                String extension = Share.substringAfterLast(fileItem.getUrl(), ".");
                for (FileItem f : items) {
                    if (Share.substringAfterLast(f.getUrl(), ".").equals(extension)) {
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
            stringBuilder.append(Share.formatFileSize(p.first))
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

    private static void checkEqualSizes(final File srcFile, final File destFile, final long srcLen, final long dstLen)
            throws IOException {
        if (srcLen != dstLen) {
            throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile
                    + "' Expected length: " + srcLen + " Actual: " + dstLen);
        }
    }

    private static int countFiles(File[] files) {
        int i = 0;
        for (int j = 0; j < files.length; j++) {
            if (!files[j].getName().startsWith("."))
                i++;
        }
        return i;
    }

    @RequiresApi(api = VERSION_CODES.O)
    private static void doCopyDirectory(final File srcDir, final File destDir, final FileFilter filter,
                                        final boolean preserveFileDate, final List<String> exclusionList)
            throws IOException {
        // recurse
        final File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (destDir.isDirectory() == false) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (destDir.canWrite() == false) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (final File srcFile : srcFiles) {
            final File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
                } else {
                    doCopyFile(srcFile, dstFile, preserveFileDate);
                }
            }
        }

        // Do this last, as the above has probably affected directory metadata
        if (preserveFileDate) {
            destDir.setLastModified(srcDir.lastModified());
        }
    }

    @RequiresApi(api = VERSION_CODES.O)
    private static void doCopyFile(final File srcFile, final File destFile, final boolean preserveFileDate)
            throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        final Path srcPath = srcFile.toPath();
        final Path destPath = destFile.toPath();
        final long newLastModifed = preserveFileDate ? srcFile.lastModified() : destFile.lastModified();
        Files.copy(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);

        // TODO IO-386: Do we still need this check?
        checkEqualSizes(srcFile, destFile, Files.size(srcPath), Files.size(destPath));
        // TODO IO-386: Do we still need this check?
        checkEqualSizes(srcFile, destFile, srcFile.length(), destFile.length());

        destFile.setLastModified(newLastModifed);
    }

    private static String getExternalStoragePath(Context context) {
        StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            if (result == null) return null;
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                Object removableObject = isRemovable.invoke(storageVolumeElement);
                if (removableObject == null) return null;
                boolean removable = (Boolean) removableObject;
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static File[] verifiedListFiles(final File directory) throws IOException {
        if (!directory.exists()) {
            final String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            final String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }
        return files;
    }
    //


}