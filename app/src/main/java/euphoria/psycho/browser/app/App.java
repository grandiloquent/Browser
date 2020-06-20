package euphoria.psycho.browser.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import euphoria.psycho.browser.base.Share;

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
        Share.initApplicationContext(this);

    }
}

