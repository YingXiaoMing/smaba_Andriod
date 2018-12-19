package ljl.myapplication2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class SubjectAdapter extends BaseAdapter {
    private List<String> list;
    private Context context;
    private LayoutInflater inflater;
    private ChoiceView view;

    public SubjectAdapter(List<String> list, Context context) {
        this.list = list;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }
    public void SetClear() {
        view.setChecked(false);
    }
    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ChoiceView view;
        if(convertView == null) {
            view = new ChoiceView(context);
        }else {
            view = (ChoiceView)convertView;
        }
        view.setText((String) getItem(position));
        this.view = view;
        return view;
    }
}
