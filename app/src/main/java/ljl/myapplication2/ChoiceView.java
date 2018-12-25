package ljl.myapplication2;

import android.content.Context;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.TextView;

public class ChoiceView extends FrameLayout implements Checkable {
    private TextView mTextView;
    private RadioButton mRadioButton;

    public ChoiceView(Context context) {
        super(context);
        View.inflate(context,R.layout.radio_item,this);
        mTextView = findViewById(R.id.child_text);
        mRadioButton = findViewById(R.id.child_radio);
    }
    public void setText(String text) {
        mTextView.setText(text);
    }
    @Override
    public boolean isChecked() {
        return mRadioButton.isChecked();
    }
    @Override
    public void toggle() {
        mRadioButton.toggle();
    }
    @Override
    public void setChecked(boolean checked) {
        mRadioButton.setChecked(checked);
    }
}
