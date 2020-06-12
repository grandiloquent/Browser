package euphoria.psycho.browser.widget;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * The vertical dimension groups.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({VerticalDisplayStyle.FLAT, VerticalDisplayStyle.REGULAR})
public @interface VerticalDisplayStyle {
    int FLAT = 0;
    int REGULAR = 1;
}