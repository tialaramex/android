package org.tlrmx.otp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class HelloAndroid extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TextView tv = new TextView(this);
        tv.setText("Hello Nick, Welcome to Android");
        setContentView(tv);
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
