package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.view.WindowManager.LayoutParams;
import android.webkit.MimeTypeMap;
import android.widget.EditText;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.core.graphics.PathUtils;
import androidx.core.util.Pair;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet;
import euphoria.psycho.browser.app.BottomSheet.OnClickListener;
import euphoria.psycho.browser.app.FunctionsMenu;
import euphoria.psycho.browser.app.NativeHelper;
import euphoria.psycho.browser.app.SampleDownloadActivity;
import euphoria.psycho.browser.app.ServerActivity;
import euphoria.psycho.browser.app.SettingsActivity;
import euphoria.psycho.browser.app.TwitterHelper;
import euphoria.psycho.browser.app.TwitterHelper.TwitterVideo;
import euphoria.psycho.browser.base.Share;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class FileHelper {
    public static final int SORT_BY_ASCENDING = 4;
    public static final int SORT_BY_DATA_MODIFIED = 2;
    public static final int SORT_BY_DESCENDING = 5;
    public static final int SORT_BY_NAME = 0;
    public static final int SORT_BY_SIZE = 1;
    public static final int SORT_BY_TYPE = 3;
    public static final int TYPE_APK = 0;
    public static final int TYPE_EXCEL = 1;
    public static final int TYPE_FOLDER = 2;
    public static final int TYPE_IMAGE = 3;
    public static final int TYPE_MUSIC = 4;
    public static final int TYPE_OTHERS = 5;
    public static final int TYPE_PDF = 6;
    public static final int TYPE_PPS = 7;
    public static final int TYPE_TEXT = 8;
    public static final int TYPE_VCF = 9;
    public static final int TYPE_VIDEO = 10;
    public static final int TYPE_WORD = 11;
    public static final int TYPE_ZIP = 12;
    private static boolean sIsHasSD;
    private static String sSDPath;
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
    static ExecutorService sSingleThreadExecutor;

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

    public static void copyDirectory(final File srcDir, final File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    public static void copyDirectory(final File srcDir, final File destDir,
                                     final boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

    public static void copySelections(FileManager fileManager) {
        List<FileItem> fileItems = fileManager.getSelectionDelegate().getSelectedItemsAsList();
        fileManager.getFileOperationManager().addToCopy(fileItems);
        fileManager.getSelectionDelegate().clearSelection();
    }

    public static Pair<Integer, String>[] createBottomSheetItems(Context context) {
        if (sIsHasSD) {
            return new Pair[]{
                    Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                    Pair.create(R.drawable.ic_sd_storage, context.getString(R.string.sd_storage)),
                    Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                    Pair.create(R.drawable.ic_info, context.getString(R.string.directory_info)),
                    Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
                    Pair.create(R.drawable.ic_more_vert, context.getString(R.string.more))
            };
        } else {
            return new Pair[]{
                    Pair.create(R.drawable.ic_storage, context.getString(R.string.storage)),
                    Pair.create(R.drawable.ic_create_new_folder, context.getString(R.string.create_new_folder)),
                    Pair.create(R.drawable.ic_info, context.getString(R.string.directory_info)),
                    Pair.create(R.drawable.ic_settings, context.getString(R.string.settings)),
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
                        youdaoChinese(activity);
                        break;
                    case R.drawable.ic_g_translate:
                        google(activity);
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
        };
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
            case "m4a":
            case "aac":
            case "flac":
            case "gsm":
            case "mid":
            case "xmf":
            case "mxmf":
            case "rtttl":
            case "rtx":
            case "ota":
            case "imy":
            case "mp3":
            case "wav":
            case "ogg":
                return TYPE_MUSIC;
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

    public static boolean isSymlink(final File file) {
        Objects.requireNonNull(file, "file");
        return Files.isSymbolicLink(file.toPath());
    }

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
        String url = fileItem.getUrl();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(url)),
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        Share.substringAfterLast(url, '.')
                ));
        activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.open)));
    }

    public static void selectSameType(FileManager fileManager) {
        List<FileItem> fileItems = fileManager.getSelectionDelegate().getSelectedItemsAsList();
        if (fileItems.size() > 0) {
            FileItem fileItem = fileItems.get(0);
            List<FileItem> items = fileManager.getFileAdapter().getFileItems();
            Set<FileItem> fileItemSet = new HashSet<>();
            if (fileItem.getType() == FileHelper.TYPE_FOLDER) {
                for (FileItem f : items) {
                    if (f.getType() == FileHelper.TYPE_FOLDER) {
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

    public static void showBottomSheet(Activity activity, Pair<Integer, String>[] items, FileManager fileManager) {
        BottomSheet bottomSheet = new BottomSheet(activity)
                .setOnClickListener(item -> {
                    switch (item.first) {
                        case R.drawable.ic_storage:
                            fileManager.openDirectory(Environment.getExternalStorageDirectory().getAbsolutePath());
                            break;
                        case R.drawable.ic_sd_storage:
                            fileManager.openDirectory(sSDPath);
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
                    }
                    fileManager.setBottomSheet(null);
                });
        fileManager.setBottomSheet(bottomSheet);
        bottomSheet.showDialog(items);
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

    private static void createNewDirectory(Activity activity, FileManager fileManager) {
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

    private static void google(Activity activity) {
        if (sSingleThreadExecutor == null)
            sSingleThreadExecutor = Executors.newSingleThreadExecutor();
        sSingleThreadExecutor.submit(() -> {
            CharSequence q = Share.getClipboardString();
            if (q == null) return;
            String query = q.toString().trim();
            String result = NativeHelper.google(query, false);
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Share.setClipboardString(result);
                            dialog.dismiss();
                        })
                        .show();
            });
        });
    }

    private static void showDirectoryInfo(Activity activity, String directory) {
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

    private static void youdaoChinese(Activity activity) {
        if (sSingleThreadExecutor == null)
            sSingleThreadExecutor = Executors.newSingleThreadExecutor();
        sSingleThreadExecutor.submit(() -> {
            CharSequence q = Share.getClipboardString();
            if (q == null) return;
            String query = q.toString().trim();
            String result = NativeHelper.youdao(query, true, query.contains(" "));
            activity.runOnUiThread(() -> {
                new AlertDialog.Builder(activity)
                        .setMessage(result)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Share.setClipboardString(result);
                            dialog.dismiss();
                        })
                        .show();
            });
        });
    }

}