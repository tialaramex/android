package org.tlrmx.otp;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.CountDownTimer;
import android.content.Context;
import android.content.DialogInterface;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

/** This One Time ... */
public class BandCamp extends Activity
{
    private byte[] secret;
    private String textSecret = null; /* only set when key is changed */
    private String pin = "1111";

    private SecureRandom sr = new SecureRandom();

    /* technically it isn't the filename which is secret but its contents */
    private static final String SECRET_FILENAME = "hannigan";

    private static final int NEW_SECRET_DIALOG = 1;

    private final static char digits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /** now a 30 second timer */
    private class CodeDisplayTimer extends CountDownTimer
    {
        private BandCamp target = null;
        /* progress timer */

        public void onTick(long millisUntilFinished) {
            /* use 110% here, so there's a moment of "hang" which feels better */
            target.cdView.setFraction( 1.1f - ((float) millisUntilFinished) / 30000.0f);
        }

        public void onFinish() {
            target.cdView.setFraction(1.0f);
            target.refresh();
        }

        public CodeDisplayTimer(BandCamp target) {
            super(30000 - (new Date().getTime() % 10000), 500);
            this.target = target;
        }
    }

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

    private void save(String code) {
        try {
            FileOutputStream fos = openFileOutput(SECRET_FILENAME, Context.MODE_PRIVATE);
            fos.write(code.getBytes());
            fos.close();
        } catch (IOException x) {
            /* ignore */
        }
    }

    private void load() {
        secret = "0123456789abcdef".getBytes(); /* actually this is an acceptable default */
        try {
            FileInputStream fis = openFileInput(SECRET_FILENAME);
            fis.read(secret, 0, 16);
            fis.close();
        } catch (IOException x) {
            /* ignore */
        }
    }

    public void refresh() {
        setCode(calculate());
    }

    /** this precise calculation is needed for mOTP */
    private String calculate() {
        Date now = new Date();
        long deciseconds = now.getTime() / 10000;
        String time = "" + deciseconds;

        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Missing MD5 hash implementation", e);
        }

        md5.update(time.getBytes());
        md5.update(secret);
        md5.update(pin.getBytes());
        StringBuilder output = byteArrayToHex(new StringBuilder(), md5.digest());
        
        return output.toString().substring(0, 6);
    }

    TextView codeView;
    CountdownView cdView;
    CodeDisplayTimer timer;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        load();
        codeView = (TextView) findViewById(R.id.code);
        cdView = new CountdownView(this);
        ViewGroup group = (ViewGroup) findViewById(R.id.group);
        group.addView(cdView);
        refresh();
    }

    private void setCode(String code) {
        codeView.setText(code);
        if (timer != null) {
            timer.cancel();
        }
        timer = new CodeDisplayTimer(this);
        timer.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(id) {
        case NEW_SECRET_DIALOG:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Secret key: " + textSecret)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
            dialog = builder.create();

            break;
        default:
            dialog = null;
        }

        return dialog;
    }


    private void alert(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private void reset() {
        byte[] bytes = new byte[8];
        sr.nextBytes(bytes);
        textSecret = byteArrayToHex(new StringBuilder(), bytes).toString();
        save(textSecret);
        secret = textSecret.getBytes();
        refresh();
        removeDialog(NEW_SECRET_DIALOG); /* in case there's an old one with the previous key */
        showDialog(NEW_SECRET_DIALOG);
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

    private class CountdownView extends View {
        public CountdownView(Context context) {
            super(context);
        }

        float fraction = 0.0f;

        public void setFraction(float fraction) {
            if (fraction < 0.0f) {
                this.fraction = 0.0f;
            } else if (fraction > 1.0f) {
                this.fraction = 1.0f;
            } else {
                this.fraction = fraction;
            }
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.GRAY);
            canvas.drawRect(85, 5, 245, 35, paint);
            paint.setColor(Color.RED);
            canvas.drawRect(90, 10, 90 + (int) (150.0 * fraction) , 30, paint);
        }
    }
}

/* vi:set ts=8 sts=4 sw=4 et: */
