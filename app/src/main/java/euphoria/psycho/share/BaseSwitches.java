
package euphoria.psycho.share;

/**
 * Contains all of the command line switches that are specific to the base/
 * portion of Chromium on Android.
 */
public abstract class BaseSwitches {
    // Default country code to be used for search engine localization.
    public static final String DEFAULT_COUNTRY_CODE_AT_INSTALL = "default-country-code";
    // Force disabling of low-end device mode when set.
    public static final String DISABLE_LOW_END_DEVICE_MODE = "disable-low-end-device-mode";
    // Adds additional thread idle time information into the trace event output.
    public static final String ENABLE_IDLE_TRACING = "enable-idle-tracing";
    // Force low-end device mode when set.
    public static final String ENABLE_LOW_END_DEVICE_MODE = "enable-low-end-device-mode";
    // Enables the reached code profiler.
    public static final String ENABLE_REACHED_CODE_PROFILER = "enable-reached-code-profiler";
    // Block ChildProcessMain thread of render process service until a Java debugger is attached.
    // To pause even earlier: am set-debug-app org.chromium.chrome:sandbox_process0
    // However, this flag is convenient when you don't know the process number, or want
    // all renderers to pause (set-debug-app applies to only one process at a time).
    public static final String RENDERER_WAIT_FOR_JAVA_DEBUGGER = "renderer-wait-for-java-debugger";

    // Prevent instantiation.
    private BaseSwitches() {}
}
