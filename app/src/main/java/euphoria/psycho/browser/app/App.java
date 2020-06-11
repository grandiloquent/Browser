package euphoria.psycho.browser.app;

import android.app.Application;
import android.content.Context;

import euphoria.psycho.browser.base.Share;

public class App extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        Share.initApplicationContext(this);
    }
}

