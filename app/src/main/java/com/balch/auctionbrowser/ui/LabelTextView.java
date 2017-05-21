package com.balch.auctionbrowser.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.balch.auctionbrowser.R;

public class LabelTextView extends LinearLayout {
    private TextView labelTextView;
    private TextView valueTextView;

    public LabelTextView(Context context) {
        super(context);
        initializeLayout(null);
    }

    public LabelTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeLayout(attrs);
    }

    public LabelTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeLayout(attrs);
    }

    private void initializeLayout(AttributeSet attrs) {

        inflate(getContext(), R.layout.label_text_view, this);
        this.labelTextView = (TextView)findViewById(R.id.label_text_view_label);
        this.valueTextView = (TextView)findViewById(R.id.label_text_view_value);

        if (attrs != null){
            TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.LabelTextView,
                    0, 0);

            try {
                String labelText = typedArray.getString(R.styleable.LabelTextView_label_text);
                if (labelText != null) {
                    this.labelTextView.setText(labelText);
                }
            } finally {
                typedArray.recycle();
            }
        }

    }

    public void setValue(String value) {
        this.valueTextView.setText(value);
    }
}
