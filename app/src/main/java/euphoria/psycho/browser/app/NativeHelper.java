package euphoria.psycho.browser.app;

public class NativeHelper {

    static {
        System.loadLibrary("native-lib");
    }

    public static native void startServer(String host, String port, String rootDirectory);

    public static native String youdao(String query, boolean isEnglishToChinese, boolean isTranslate);

    public static native String google(String query, boolean isEnglishToChinese);

    public static native boolean deleteFileSystem(String path);
}
