/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.android;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.android.Facebook.DialogListener;

public class FbDialog extends Dialog {

    static final int FB_BLUE = 0xFF6D84B4;
    static final float[] DIMENSIONS_LANDSCAPE = {460, 260};
    
    //static final float[] DIMENSIONS_PORTRAIT = {280, 420};
    static final float[] DIMENSIONS_PORTRAIT = {300, 430};
    
    
    static final FrameLayout.LayoutParams FILL = 
    					new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
    						ViewGroup.LayoutParams.FILL_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;
    static final String DISPLAY_STRING = "touch";
    static final String FB_ICON = "icon.png";
    
    private String mUrl;
    private DialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private TextView mTitle;
    
    public FbDialog(Context context, String url, DialogListener listener) {
        super(context);
        mUrl = url;
        mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");
        
        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);
        setUpTitle();
        setUpWebView();
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        float[] dimensions = display.getWidth() < display.getHeight() ?
        		DIMENSIONS_PORTRAIT : DIMENSIONS_LANDSCAPE;
        addContentView(mContent, new FrameLayout.LayoutParams(
        		(int) (dimensions[0] * scale + 0.5f),
        		(int) (dimensions[1] * scale + 0.5f)));
    }

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        //STOPPED USING FACEBOOK ICON IN TITLE, USING APP'S ICON INSTEAD
        Drawable icon = getContext().getResources().getDrawable(com.v1_4.mydiaryapp.com.R.drawable.icon);
        
        mTitle = new TextView(getContext());
        mTitle.setText("Share on Facebook");
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(FB_BLUE);        
        
        //MODIFIED THESE LINES
        mTitle.setTextSize(25); // ADDED SET TEXT SIZE
        mTitle.setGravity(Gravity.CENTER_VERTICAL);
        
        //TEMP SIZE TO ACCOUNT FOR LARGER ICON        
        //mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mTitle.setPadding(10 + PADDING, 10, 10, 10);

        
        mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
        mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
        mContent.addView(mTitle);
    }
    
    private void setUpWebView() {
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new FbDialog.FbWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mContent.addView(mWebView);
    }

    private class FbWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("ZZ: Facebook-WebView", "Redirect URL: " + url);
            if (url.startsWith(Facebook.REDIRECT_URI)) {
                Bundle values = Util.parseUrl(url);
                String error = values.getString("error_reason");
                if (error == null) {
                    mListener.onComplete(values);
                } else {
                    mListener.onFacebookError(new FacebookError(error));
                }
                FbDialog.this.dismiss();
                return true;
            } else if (url.startsWith(Facebook.CANCEL_URI)) {
                mListener.onCancel();
                FbDialog.this.dismiss();
                return true;
            } else if (url.contains(DISPLAY_STRING)) {
                return false;
            }
            // launch non-dialog URLs in a full browser
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); 
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(
                    new DialogError(description, errorCode, failingUrl));
            FbDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("ZZ: Facebook-WebView", "Webview loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }
            mSpinner.dismiss();
        }   
        
    }
}
