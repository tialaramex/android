package org.tlrmx.otp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/** This One Time ... */
public class BandCamp extends Activity
{
    private String secret = "replacewithyours";
    private String pin = "1111";

    private final static char digits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    private static StringBuilder byteArrayToHex(StringBuilder destination, byte[] source) {
        destination.ensureCapacity(destination.length() + 2 * source.length);
        for(int i = 0; i < source.length; ++i) {
            byte b = source[i];
            char c2 = digits[b & 0xf];
            char c1 = digits[((b & 0xf0) >>> 4) & 0xf];
            destination.append(c1);
            destination.append(c2);
        }
        return destination;
    }

    private String calculate() {
        Date now = new Date();
        long deciseconds = now.getTime() / 10000;
        String mush = "" + deciseconds + secret + pin;

        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Missing MD5 hash implementation", e);
        }

        md5.update(mush.getBytes());
        StringBuilder output = byteArrayToHex(new StringBuilder(), md5.digest());
        
        return output.toString().substring(0, 6);
    }

    TextView codeView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        codeView = (TextView) findViewById(R.id.code);
        setCode(calculate());
    }

    private void setCode(String code) {
        codeView.setText(code);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    /* this should change the secret */
    private void reset() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.reset:
            reset();
            return true;
        case R.id.quit:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
