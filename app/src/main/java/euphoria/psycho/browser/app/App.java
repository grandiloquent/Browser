package euphoria.psycho.browser.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import euphoria.psycho.share.ContextUtils;
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        ContextUtils.initApplicationContext(this);
        ContextUtils.initialize(this);
    }
}