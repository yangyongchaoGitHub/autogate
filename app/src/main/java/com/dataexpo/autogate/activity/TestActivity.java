package com.dataexpo.autogate.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.dataexpo.autogate.R;
import com.dataexpo.autogate.service.MainApplication;

public class TestActivity extends BascActivity {
    EditText et_1;
    EditText et_2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        et_1 = findViewById(R.id.et_test_input1);
        et_2 = findViewById(R.id.et_test_input2);
        findViewById(R.id.btn_test_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte b1 = Byte.parseByte(et_1.getText().toString());
                byte b2 = Byte.parseByte(et_2.getText().toString());
                MainApplication.getInstance().getService().testOption(b1, b2);
            }
        });
    }
}
