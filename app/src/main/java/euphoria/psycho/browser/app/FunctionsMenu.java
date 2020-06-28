package euphoria.psycho.browser.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import euphoria.psycho.browser.R;
import euphoria.psycho.browser.app.BottomSheet.OnClickListener;
import euphoria.psycho.share.ContextUtils;
public class FunctionsMenu {
    private final PopupWindow mPopupWindow;
    private RecyclerView mRecyclerView;
    private View mContainer;
    private BottomSheetAdapter mBottomSheetAdapter;
    private final Context mContext;
    private final View mParent;
    private final OnClickListener mOnClickListener;
    public FunctionsMenu(Context context, View parent, OnClickListener onClickListener) {
        mContext = context;
        mPopupWindow = new PopupWindow(context);
        mParent = parent;
        mOnClickListener = onClickListener;
        Log.e("TAG/", "Debug: FunctionsMenu, \n" + ContextUtils.dpToPixel(8));
        mPopupWindow.setWidth(ContextUtils.getWidthPixels() - ContextUtils.getWidthPixels() / 6);
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setElevation(ContextUtils.dpToPixel(8));
    }
    private OnClickListener mListener = new OnClickListener() {
        @Override
        public void onClicked(Pair<Integer, String> item) {
            mPopupWindow.dismiss();
            if (mOnClickListener != null) mOnClickListener.onClicked(item);
        }
    };
    public void showDialog(Pair[] items) {
        if (mContainer == null) {
            mContainer = LayoutInflater.from(mContext).inflate(R.layout.bottomsheet_menu, null);
            mRecyclerView = mContainer.findViewById(R.id.recycler_view);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, 4));
            mBottomSheetAdapter = new BottomSheetAdapter(mListener, items);
            mRecyclerView.setAdapter(mBottomSheetAdapter);
            mPopupWindow.setContentView(mContainer);
        }
        mPopupWindow.showAtLocation(mParent, Gravity.CENTER, 0, 0);
    }
}