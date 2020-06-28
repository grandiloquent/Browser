

package euphoria.psycho.share;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * The annotated function can be removed in release builds.
 *
 * Calls to this function will be removed if its return value is not used. If all calls are removed,
 * the function definition itself will be candidate for removal.
 * It works by indicating to Proguard that the function has no side effects.
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface RemovableInRelease {}
