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

import android.content.Context;

/**
 * This class is repsonsible for Web Content
 */
public interface WebContentManager {

	/**
	 * Shows a webpage with the given url if a markerobject is selected
	 * (mixlistview, mixoverlay).
	 */
	void loadWebPage(String url, Context context) throws Exception;

	/**
	 * Checks if the url can be opened by another intent activity, instead of
	 * the webview This method searches for possible intents that can be used
	 * instead. I.E. a mp3 file can be forwarded to a mediaplayer.
	 * 
	 * @param url
	 *            the url to process
	 * @param view
	 * @return
	 */
	boolean processUrl(String url, Context ctx);

}