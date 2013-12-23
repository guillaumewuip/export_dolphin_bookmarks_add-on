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

        TextView textView = (TextView) findViewById(R.id.textView5);

        textView.setText(Html.fromHtml(
                "This app is "+
                "<a href=\"https://github.com/guillaumewuip/export_dolphin_bookmarks_add-on\">"+
                "published on Github under the MIT License</a> "+
                "by <a href=\"http://github.com/guillaumewuip\">Guillaume Wuip</a> <br>" +
                "Feel free to fork the project !"
            ));
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
