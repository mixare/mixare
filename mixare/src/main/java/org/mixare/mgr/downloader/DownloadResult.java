/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package org.mixare.mgr.downloader;

import java.util.ArrayList;
import java.util.List;

import org.mixare.data.DataSource;
import org.mixare.lib.marker.Marker;



public class DownloadResult {
	
	private DataSource dataSource;
	private String params;
	private List<Marker> markers;

	private boolean error;
	private String errorMsg = "";
	private DownloadRequest errorRequest;
	private String idOfDownloadRequest;
	
	

	public DownloadResult() {
		super();
		this.dataSource = null;
		this.params = "";
		this.markers = new ArrayList<Marker>();
		this.error = true;
		this.errorMsg = "DUMMY OBJECT";
		this.errorRequest = null;
		this.idOfDownloadRequest="";
	}

	
	
	public String getIdOfDownloadRequest() {
		return idOfDownloadRequest;
	}



	public void setIdOfDownloadRequest(String idRequest) {
		idOfDownloadRequest = idRequest;
	}



	public List<Marker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<Marker> markers) {
		this.markers = markers;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource source) {
		this.dataSource = source;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
		if (!error){
			errorMsg="";
		}
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public DownloadRequest getErrorRequest() {
		return errorRequest;
	}

	public void setErrorRequest(DownloadRequest errorRequest) {
		this.errorRequest = errorRequest;
	}

	public void setError(Exception ex, DownloadRequest request) {
		error=true;
		errorMsg=ex.getMessage();
		errorRequest=request;
	}
	
	
	public void setAccomplish(String idOfDownloadRequest, List<Marker> markers, DataSource ds ) {
		setIdOfDownloadRequest(idOfDownloadRequest);
		setMarkers(markers);
		setDataSource(ds);
		setError(false);
		errorMsg="NO ERROR";
		errorRequest=null;
	}

}
