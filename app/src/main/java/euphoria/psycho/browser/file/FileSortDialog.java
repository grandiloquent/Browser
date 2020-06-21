package euphoria.psycho.browser.file;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import euphoria.psycho.browser.R;

public class FileSortDialog {

    private class SortAdapter extends BaseAdapter {
        private final List<String> mTypeList;
        private final int mSelectedIndex;

        private SortAdapter(List<String> typeList, int selectedIndex) {
            mTypeList = typeList;
            mSelectedIndex = selectedIndex;
        }

        @Override
        public int getCount() {
            return mTypeList.size();
        }

        @Override
        public String getItem(int i) {
            return mTypeList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = view;
            if (v == null) {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_sort_list, viewGroup, false);
            }
            LinearLayout linearLayout = v.findViewById(R.id.sort_layout);
            RadioButton radioButton = v.findViewById(R.id.sort_type);

            radioButton.setOnClickListener(r -> {
                if (radioButton.isChecked()) {

                    radioButton.isChecked();

                    Log.e("TAG/", "Debug: getView, \n");

                    radioButton.setChecked(false);
                    return;
                }
                radioButton.setChecked(true);
            });

            TextView textView = v.findViewById(R.id.sort_text);

            textView.setText(mTypeList.get(i));
            if (this.mSelectedIndex == i) {
                radioButton.setChecked(true);
            } else {
                radioButton.setChecked(false);
            }
            return v;
        }
    }

    private Context mContextTheme;
    private Dialog mDialog;
    private Activity mActivity;

    protected FileSortDialog(@NonNull Activity activity) {
        this.mActivity = activity;
        this.mContextTheme = new ContextThemeWrapper(activity, R.style.Theme_DeviceDefault_Light);
    }

    public void addSortDialog(String[] typeList, int selectedIndex) {
        View view = View.inflate(mContextTheme, R.layout.file_sort_dialog, null);
        ListView listView = view.findViewById(R.id.list_view_sort);
        listView.setAdapter(new SortAdapter(Arrays.asList(typeList), selectedIndex));
        listView.setSelection(selectedIndex);
        this.mDialog = new AlertDialog.Builder(this.mActivity)
                .setTitle(this.mActivity.getString(R.string.sort))
                .setView(view)
                .create();

        // this.mDialog.getWindow().setGravity(17);
        this.mDialog.show();
    }

}
