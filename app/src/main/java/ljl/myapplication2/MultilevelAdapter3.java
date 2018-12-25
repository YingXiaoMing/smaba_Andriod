package ljl.myapplication2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultilevelAdapter3 extends BaseAdapter {
    private Context context;
    private List<String> list;
    private LayoutInflater inflater;
    public static Map<Integer,Boolean> isSelected;

    public MultilevelAdapter3(Context context, List<String> list,String machineName) {
        this.list = list;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        isSelected = new HashMap<>();
        for (int i=0;i<list.size();i++) {
            if (list.get(i).equals(machineName)) {
                isSelected.put(i,true);
            }else {
                isSelected.put(i,false);
            }
        }
    }
    public void setClear() {
        for (int i = 0; i<list.size();i++) {
            isSelected.put(i,false);
        }
        notifyDataSetChanged();
    }

    public void setCheck(int position) {
        for (int i = 0; i<list.size();i++) {
            if (i == position) {
                isSelected.put(position,true);
            }else {
                isSelected.put(i,false);
            }
        }
        notifyDataSetChanged();
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MultilevelAdapter3.viewHolder viewHolder = null;
        if (viewHolder == null) {
            convertView = inflater.inflate(R.layout.radio_item,parent,false);
            viewHolder = new MultilevelAdapter3.viewHolder();
            viewHolder.title =  convertView.findViewById(R.id.child_text);
            viewHolder.checkBox =  convertView.findViewById(R.id.child_radio);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (MultilevelAdapter3.viewHolder) convertView.getTag();
        }
        viewHolder.title.setText(list.get(position));
        viewHolder.checkBox.setChecked(isSelected.get(position));
        return convertView;
    }

    public class viewHolder {
        public TextView title;
        public RadioButton checkBox;
    }
}
