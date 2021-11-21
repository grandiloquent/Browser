package euphoria.psycho.browser.file;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.indicator.progresspie.ProgressPieIndicator;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.github.piasy.biv.view.GlideImageViewFactory;

import euphoria.psycho.browser.R;

public class ImageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BigImageViewer.initialize(GlideImageLoader.with(getApplicationContext()));
        setContentView(R.layout.image_activity);
        BigImageView bigImageView = findViewById(R.id.mBigImage);
        bigImageView.setProgressIndicator(new ProgressPieIndicator());
        bigImageView.setImageViewFactory(new GlideImageViewFactory());
        bigImageView.showImage(getIntent().getData());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BigImageViewer.imageLoader().cancelAll();
    }
}
