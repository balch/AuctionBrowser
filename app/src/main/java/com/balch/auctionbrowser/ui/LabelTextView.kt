package com.balch.auctionbrowser.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.R.layout.label_text_view
import com.balch.auctionbrowser.R.styleable.LabelTextView_label_text
import kotlinx.android.synthetic.main.label_text_view.view.*

class LabelTextView : LinearLayout {
    private val labelTextView: TextView by lazy {label_text_view_label}
    private val valueTextView: TextView by lazy {label_text_view_value}

    var value: String
        get() = valueTextView.text.toString()
        set(v) = setTextValue(v)

    constructor(context: Context) : super(context) {
        initializeLayout(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeLayout(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initializeLayout(attrs)
    }

    private fun initializeLayout(attrs: AttributeSet?) {

        View.inflate(context, label_text_view, this)

        if (attrs != null) {
            val typedArray = context.theme.obtainStyledAttributes(
                    attrs,
                    R.styleable.LabelTextView,
                    0, 0)

            try {
                val labelText = typedArray.getString(LabelTextView_label_text)
                if (labelText != null) {
                    this.labelTextView.text = labelText
                }
            } finally {
                typedArray.recycle()
            }
        }

    }

    private fun setTextValue(value: String) {
        this.valueTextView.text = value
    }
}
