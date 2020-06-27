package euphoria.psycho.browser.video;

import android.view.View;

class ApiHelper {
    public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_HIDE_NAVIGATION =
            hasField(View.class, "SYSTEM_UI_FLAG_HIDE_NAVIGATION");
    public static final boolean HAS_VIEW_SYSTEM_UI_FLAG_LAYOUT_STABLE =
            hasField(View.class, "SYSTEM_UI_FLAG_LAYOUT_STABLE");

    private static boolean hasField(Class<?> klass, String fieldName) {
        try {
            klass.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }
}
