/*
 * Copyright (C) 2010- Peer internet solutions
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
package org.mixare;

import org.mixare.lib.MixContextInterface;
import org.mixare.lib.render.Matrix;
import org.mixare.mgr.datasource.DataSourceManager;
import org.mixare.mgr.datasource.DataSourceManagerFactory;
import org.mixare.mgr.downloader.DownloadManager;
import org.mixare.mgr.downloader.DownloadManagerFactory;
import org.mixare.mgr.location.LocationFinder;
import org.mixare.mgr.webcontent.WebContentManager;
import org.mixare.mgr.webcontent.WebContentManagerFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

/**
 * Cares about location management and about the data (source, inputstream)
 */
public class MixContext extends ContextWrapper implements MixContextInterface {

	// TAG for logging
	public static final String TAG = "Mixare";

	private MixView mixView;

	private Matrix rotationM = new Matrix();
	
	/** Responsible for all download */
	private DownloadManager downloadManager;

	/** Responsible for all location tasks */
	private LocationFinder locationFinder;
	
	/** Responsible for data Source Management */
	private DataSourceManager dataSourceManager;
	
	/** Responsible for Web Content */
	private WebContentManager webContentManager;

	public MixContext(Context appCtx) {
		super(appCtx);
		this.mixView = (MixView) appCtx;

		// TODO: RE-ORDER THIS SEQUENCE... IS NECESSARY?
		getDataSourceManager().refreshDataSources();

		if (!getDataSourceManager().isAtLeastOneDatasourceSelected()) {
			rotationM.toIdentity();
		}
		
		getLocationFinder().findLocation(this);
	}


	public String getStartUrl() {
		Intent intent = ((Activity) mixView).getIntent();
		if (intent.getAction() != null
				&& intent.getAction().equals(Intent.ACTION_VIEW)) {
			return intent.getData().toString();
		} else {
			return "";
		}
	}

	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}
	
	/**
	 * Shows a webpage with the given url when clicked on a marker.
	 */
	public void loadMixViewWebPage(String url) throws Exception {
		// TODO: CHECK INTERFACE METHOD
		getWebContentManager().loadWebPage(url, mixView);
	}

	public void doResume(MixView mixView) {
		this.mixView = mixView;
	}

	public void updateSmoothRotation(Matrix smoothR) {
		synchronized (rotationM) {
			rotationM.set(smoothR);
		}
	}
	
	
	public DataSourceManager getDataSourceManager(){
		if (this.dataSourceManager == null){
			dataSourceManager= DataSourceManagerFactory.makeDataSourceManager(this);
		}
		return dataSourceManager;
	}
	
	public LocationFinder getLocationFinder(){
		if (this.locationFinder == null){
			locationFinder= new LocationFinder(this);
		}
		return locationFinder;
	}
	
	
	public DownloadManager getDownloadManager(){
		if (this.downloadManager == null){
			downloadManager= DownloadManagerFactory.makeDownloadManager(this);
			getLocationFinder().setDownloadManager(downloadManager);
		}
		return downloadManager;
	}
	
	
	public WebContentManager getWebContentManager(){
		if (this.webContentManager == null){
			webContentManager= WebContentManagerFactory.makeWebContentManager(this);
		}
		return webContentManager;
	}
	
	public MixView getActualMixView(){
		return this.mixView;
	}
	
	public ContentResolver getContentResolver(){
		return this.mixView.getContentResolver();
	} 

}
