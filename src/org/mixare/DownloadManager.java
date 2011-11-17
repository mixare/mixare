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

import java.io.StringReader;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONException;
import org.json.JSONObject;
import org.mixare.data.DataHandler;
import org.mixare.data.DataSource;
import org.mixare.data.Json;
import org.mixare.data.XMLHandler;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import android.os.AsyncTask;
import android.util.Log;

/**
 * This class establishes a connection and downloads the data for
 * each entry in its todo list one after another.
 */
public class DownloadManager extends AsyncTask<DownloadRequest, Integer, DownloadResult> {

	MixContext ctx;
	MixView mview;
  private static final String TAG = "DownloadManager";

	public DownloadManager(MixContext ctx) {
		this.ctx = ctx;
	}

	@Override
	protected DownloadResult doInBackground(DownloadRequest... params) {
		DownloadRequest request;
		DownloadResult result = new DownloadResult();

		try {
			request = params[0];
			String tmp = ctx.openURL(request.source.getUrl(), request.params);

			Json layer = new Json();

			// try loading JSON DATA
			try {

				Log.v(TAG, "try to load JSON data");
				JSONObject root = new JSONObject(tmp);
				List<Marker> markers = layer.load(root,request.source);
				result.setMarkers(markers);
				result.source = request.source;
				result.error = false;
				result.errorMsg = null;
			}
			catch (JSONException e) {

				Log.v(TAG, "no JSON data");
				Log.v(TAG, "try to load XML data");

				try {
					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					//Document doc = builder.parse(is);d
					Document doc = builder.parse(new InputSource(new StringReader(tmp)));

					//Document doc = builder.parse(is);

					XMLHandler xml = new XMLHandler();

					Log.i(TAG, "loading XML data");	


					List<Marker> markers = xml.load(doc, request.source);

					result.setMarkers(markers);

					result.source = request.source;
					result.error = false;
					result.errorMsg = null;
				} catch (Exception e1) {
					e1.printStackTrace();
				}				
			}
		}
		catch (Exception ex) {
			result.errorMsg = ex.getMessage();
			ex.printStackTrace();
		}

		return result;
	}

	protected void onPostExecute(DownloadResult result) {
		MixView mview = ctx.mixView;
		DataHandler dataHandler = mview.dataView.getDataHandler();
		dataHandler.addMarkers(result.getMarkers());
	}
}

class DownloadRequest {
	public DataSource source;
	String params;
}

class DownloadResult {
	public DataSource source;
	String params;
	List<Marker> markers;

	boolean error;
	String errorMsg="";
	DownloadRequest errorRequest;


	public List<Marker> getMarkers() {
		return markers;
	}
	public void setMarkers(List<Marker> markers) {
		this.markers = markers;
	}

}
