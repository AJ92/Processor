package com.aj.processor.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.aj.processor.app.R;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 10/11/13
 * Time: 1:35 PM
 */
public class StartActivity extends Activity implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {

    private Button b_start, b_apply, b_add, b_remove;
    private Switch b_debugLog, b_debugFrameLog, b_dupMarkers, b_frameDebug,
            b_prepFrame, b_contours, b_squares, b_markers, b_sampling,
            b_markerID, b_uncertain;
    private RadioGroup r_binMethod;
    private EditText threshold, trackMarker;

    private Bundle options;
    private ArrayList<Integer> trackers;
    private final String TAG = "StartActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        options = new Bundle();
        trackers = new ArrayList<Integer>();

        b_start = (Button) findViewById(R.id.start);
        b_start.setOnClickListener(this);
        b_apply = (Button) findViewById(R.id.apply);
        b_apply.setOnClickListener(this);
        b_add = (Button) findViewById(R.id.add);
        b_add.setOnClickListener(this);
        b_remove = (Button) findViewById(R.id.remove);
        b_remove.setOnClickListener(this);

        r_binMethod = (RadioGroup) findViewById(R.id.r_group);
        r_binMethod.setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.r_default)).setChecked(true);

        threshold = (EditText) findViewById(R.id.theshold);
        trackMarker = (EditText) findViewById(R.id.trackMarker);

        b_debugLog = (Switch) findViewById(R.id.debugLog);
        b_debugLog.setOnCheckedChangeListener(this);
        b_debugFrameLog = (Switch) findViewById(R.id.frameDebug);
        b_debugFrameLog.setOnCheckedChangeListener(this);
        b_debugFrameLog.setEnabled(false);

        b_dupMarkers = (Switch) findViewById(R.id.dupMarkers);
        b_dupMarkers.setOnCheckedChangeListener(this);
        b_uncertain = (Switch) findViewById(R.id.hamming);
        b_uncertain.setOnCheckedChangeListener(this);

        b_frameDebug = (Switch) findViewById(R.id.visualDebug);
        b_frameDebug.setOnCheckedChangeListener(this);
        b_prepFrame = (Switch) findViewById(R.id.prepFrame);
        b_prepFrame.setOnCheckedChangeListener(this);
        b_prepFrame.setEnabled(false);
        b_contours = (Switch) findViewById(R.id.contours);
        b_contours.setOnCheckedChangeListener(this);
        b_contours.setEnabled(false);
        b_squares = (Switch) findViewById(R.id.square);
        b_squares.setOnCheckedChangeListener(this);
        b_squares.setEnabled(false);
        b_markers = (Switch) findViewById(R.id.markers);
        b_markers.setOnCheckedChangeListener(this);
        b_markers.setEnabled(false);
        b_sampling = (Switch) findViewById(R.id.sampling);
        b_sampling.setOnCheckedChangeListener(this);
        b_sampling.setEnabled(false);
        b_markerID = (Switch) findViewById(R.id.marker_id);
        b_markerID.setOnCheckedChangeListener(this);
        b_markerID.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                Intent in = new Intent();
                in.setClass(getApplicationContext(), ImagineActivity.class);
                // Add all trackers:
                options.putIntegerArrayList("trackers", trackers);
                in.putExtras(options);
                startActivity(in);
                break;
            case R.id.apply:
                String thesh = threshold.getText().toString();
                if (!thesh.isEmpty())
                    options.putInt("threshold", Integer.valueOf(thesh));
                else
                    options.putInt("threshold", 100);
                break;
            case R.id.add:
                String add = trackMarker.getText().toString();
                if (!add.isEmpty())
                    trackers.add(Integer.valueOf(add));
                break;
            case R.id.remove:
                String remove = trackMarker.getText().toString();
                if (!remove.isEmpty())
                    trackers.remove(Integer.valueOf(remove));
                break;
            case R.id.addall:
                for(int i = 0; i < 255; i++){
                    trackers.add(new Integer(i));
                }
                break;
            default:
                Log.e(TAG, "Unknown button clicked!");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.debugLog:
                options.putBoolean("debugLog", isChecked);
                b_debugFrameLog.setEnabled(isChecked);
                break;
            case R.id.frameDebug:
                options.putBoolean("frameDebug", isChecked);
                break;
            case R.id.dupMarkers:
                options.putBoolean("dupMarkers", isChecked);
                break;
            case R.id.hamming:
                options.putBoolean("hamming", isChecked);
                break;
            case R.id.visualDebug:
                options.putBoolean("debugFrame", isChecked);
                b_prepFrame.setEnabled(isChecked);
                b_contours.setEnabled(isChecked);
                b_squares.setEnabled(isChecked);
                b_markers.setEnabled(isChecked);
                b_sampling.setEnabled(isChecked);
                b_markerID.setEnabled(isChecked);
                break;
            case R.id.prepFrame:
                options.putBoolean("prepFrame", isChecked);
                break;
            case R.id.contours:
                options.putBoolean("contours", isChecked);
                break;
            case R.id.square:
                options.putBoolean("poly", isChecked);
                break;
            case R.id.markers:
                options.putBoolean("marker", isChecked);
                break;
            case R.id.sampling:
                options.putBoolean("sample", isChecked);
                break;
            case R.id.marker_id:
                options.putBoolean("marker_id", isChecked);
                break;
            default:
                Log.e(TAG, "Unknown compoundbutton clicked!");
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.r_default:
                options.putInt("bin", 0);
                break;
            case R.id.r_adaptive:
                options.putInt("bin", 1);
                break;
            case R.id.r_canny:
                options.putInt("bin", 2);
                break;
            default:
                Log.d(TAG, "Unknown radio clicked!");
        }
    }
}