package com.dolphin.browser.extension.exportdolphinbookmarks;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textView = (TextView) findViewById('textView');

        textView.setText(Html.fromHtml(
                "<b>text3:</b>  Text with a " +
                        "<a href=\"http://www.google.com\">link</a> " +
                        "created in the Java source code using HTML."));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
