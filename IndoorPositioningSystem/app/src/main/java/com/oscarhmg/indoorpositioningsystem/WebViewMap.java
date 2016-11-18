package com.oscarhmg.indoorpositioningsystem;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by user on 17/11/2016.
 */
public class WebViewMap extends Activity {
    private final static String URLMAP = "https://worker-dot-navigator-cloud.appspot.com/map";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view_map);
        WebView webView = (WebView)findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(URLMAP);
    }

}
