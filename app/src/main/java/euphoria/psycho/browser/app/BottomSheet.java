package euphoria.psycho.browser.app;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.browser.R;
public class BottomSheet {
    private final Context mContext;
    private BottomSheetAdapter mBottomSheetAdapter;
    private View mContainer;
    private BottomSheetDialog mDialog;
    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClicked(Pair<Integer, String> item) {
            mDialog.dismiss();
            if (mOnClickListener != null) mOnClickListener.onClicked(item);
        }
    };
    private OnClickListener mOnClickListener;
    private RecyclerView mRecyclerView;
    public BottomSheet(Context context) {
        mContext = context;
    }
    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
    public BottomSheet setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
        return this;
    }
    public void showDialog(Pair[] items) {
        if (mContainer == null) {
            mContainer = LayoutInflater.from(mContext).inflate(R.layout.bottomsheet_menu, null);
            mRecyclerView = mContainer.findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4));
            mBottomSheetAdapter = new BottomSheetAdapter(mListener, items);
            mRecyclerView.setAdapter(mBottomSheetAdapter);
            BottomSheetDialog dialog = new BottomSheetDialog(mContext);
            dialog.setContentView(mContainer);
            mDialog = dialog;
        }
        mDialog.show();
    }
    public interface OnClickListener {
        void onClicked(Pair<Integer, String> item);
    }
}