package com.insoline.pnd.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.insoline.pnd.R;
import com.insoline.pnd.common.BaseActivity;
import com.insoline.pnd.common.BaseApplication;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogFileActivity extends BaseActivity {

    private ScrollView scrollView;
    private TextView txtViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_file);
        scrollView = findViewById(R.id.viewerScrollView);
        txtViewer = findViewById(R.id.txtViewer);

        String fileName = getIntent().getStringExtra("fileName");
        loadFiles(fileName);
    }

    public void closeViewer(View v) {
        txtViewer.setText("");

        Activity activity = getBaseApplication().getActivity(ConfigActivity.class);
        if (activity != null) {
            ((ConfigActivity) activity).showDebugWindow(true);
        }
        finish();
    }

    public void moveToUp(View v) {
        scrollView.fullScroll(View.FOCUS_UP);
    }

    public void moveToDown(View v) {
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

    private void loadFiles(String fileName) {
        txtViewer.setText("");
        try {
            FileInputStream fis = new FileInputStream(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String str;
            while ((str = reader.readLine()) != null) {
                txtViewer.append(str + "\n");
            }
            reader.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
