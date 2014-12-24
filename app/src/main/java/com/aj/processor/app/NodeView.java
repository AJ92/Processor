package com.aj.processor.app;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;


public class NodeView extends FrameLayout {
    public NodeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public NodeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public NodeView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        View view = inflate(getContext(), R.layout.dialog_node, null);
        addView(view);
    }

    public void setNodeName(String name){
        TextView text = (TextView) findViewById(R.id.node_name);
        text.setText(name);
    }

    public void setNodeID(String id){
        TextView text = (TextView) findViewById(R.id.node_id);
        text.setText(id);
    }
}