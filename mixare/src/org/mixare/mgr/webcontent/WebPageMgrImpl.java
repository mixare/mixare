/*
 * Copyright (C) 2012- Peer internet solutions 
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare.mgr.webcontent;

import java.util.List;

import org.mixare.MixContext;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;


class WebPageMgrImpl implements WebContentManager {

	protected MixContext mixContext;
	
	/**
	 * Shows a webpage with the given url when clicked on a marker.
	 */
	public void loadMixViewWebPage(String url) throws Exception {
		loadWebPage(url, mixContext.getActualMixView());
	}

	public WebPageMgrImpl(MixContext mixContext) {
       this.mixContext=mixContext;
	}
	
	/* (non-Javadoc)
	 * @see org.mixare.mgr.webcontent.WebContentManager#loadWebPage(java.lang.String, android.content.Context)
	 */
	public void loadWebPage(String url, Context context) throws Exception {
		WebView webview = new WebView(context);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setBuiltInZoomControls(true);
		
		final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};

		webview.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (!processUrl(url, mixContext.getActualMixView())) { // if the url could not be processed by
					 // another intent
					d.show();
					view.loadUrl(url);
				}
				return true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (url.endsWith("return")) {
					d.dismiss();
					mixContext.getActualMixView().setZoomLevel();
					mixContext.getActualMixView().refreshDownload();
					mixContext.getActualMixView().refresh();
				} else {
					super.onPageFinished(view, url);
				}
			}

		});

		DisplayMetrics displaymetrics = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels - 60;
		int width = displaymetrics.widthPixels - 20;

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
	    lp.copyFrom(d.getWindow().getAttributes());
	    lp.width = width;
	    lp.height = height;
	
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		int dialogHeight = d.getWindow().getAttributes().height;
		int dialogWidth = d.getWindow().getAttributes().width;
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				dialogWidth, dialogHeight,
				Gravity.BOTTOM));
		d.getWindow().setAttributes(lp);	

		if (!processUrl(url, mixContext.getActualMixView())) { // if the url could not be processed by
										 // another intent
			d.show();
			webview.loadUrl(url);
		}
	}

	/* (non-Javadoc)
     * @see org.mixare.mgr.webcontent.WebContentManager#processUrl(java.lang.String, android.content.Context)
     */
	public boolean processUrl(String url, Context ctx) {
		// get available packages from the given url
		List<ResolveInfo> resolveInfos = getAvailablePackagesForUrl(url, ctx);
		// filter the webbrowser > because the webview will replace it, using
		// google as simple url
		List<ResolveInfo> webBrowsers = getAvailablePackagesForUrl(
				"http://www.google.com", ctx);
		for (ResolveInfo resolveInfo : resolveInfos) {

			boolean found = false;
			for (ResolveInfo webBrowser : webBrowsers) {
				if (resolveInfo.activityInfo.packageName
						.equals(webBrowser.activityInfo.packageName)) {
					found = true;
					break;
				}
			}
			if (!found) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(url));
				intent.setClassName(resolveInfo.activityInfo.packageName,
						resolveInfo.activityInfo.name);
				ctx.startActivity(intent);
				return true;
			}
		}
		return false;
	}


	private List<ResolveInfo> getAvailablePackagesForUrl(String url, Context ctx) {
		PackageManager packageManager = ctx.getPackageManager();
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		return packageManager.queryIntentActivities(intent,
				PackageManager.GET_RESOLVED_FILTER);
	}

}
