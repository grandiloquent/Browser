package euphoria.psycho.browser.widget;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The vertical dimension groups.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({VerticalDisplayStyle.FLAT, VerticalDisplayStyle.REGULAR})
public @interface VerticalDisplayStyle {
    int FLAT = 0;
    int REGULAR = 1;
}