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

/**
 * This class holds the downloaded markers and if a error occurred during the
 * download this class holds the error Information
 */
public class DownloadResult {
	
	private DataSource dataSource;
	private String params;
	private List<Marker> markers;

	private boolean error;
	private String errorMsg = "";
	private DownloadRequest errorRequest;
	private String idOfDownloadRequest;

	/**
	 * Create a new DownloadResult and initializes fields
	 */
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
	
	/**
	 * The id of the DownloadRequest which produced this DownloadResult
	 * @return Returns the id of the DownloadRequest
	 */
	public String getIdOfDownloadRequest() {
		return idOfDownloadRequest;
	}

	/**
	 * Sets the ID of the DownloadRequest which produced this DownloadResult
	 * @param idRequest The ID of the DownloadRequst
	 */
	public void setIdOfDownloadRequest(String idRequest) {
		idOfDownloadRequest = idRequest;
	}

	/**
	 * The list of Markers which got downloaded
	 * @return The list of Markers
	 */
	public List<Marker> getMarkers() {
		return markers;
	}

	/**
	 * Sets the List of Markers which got downloaded
	 * @param markers The List of Markers
	 */
	public void setMarkers(List<Marker> markers) {
		this.markers = markers;
	}

	/**
	 * Gets the DataSource which made the DownloadReqest
	 * @return The DataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Sets the DataSource which made the DownloadRequest
	 * @param source The DataSource
	 */
	public void setDataSource(DataSource source) {
		this.dataSource = source;
	}

	/**
	 * Returns the parameter used for the download
	 * @return The parameter
	 */
	public String getParams() {
		return params;
	}

	/**
	 * Sets the parameter of the DownloadRequest
	 * @param params The parameter used for the download
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * Indicated whether an error occurred or not
	 * @return True if an error occurred false if not
	 */
	public boolean isError() {
		return error;
	}

	/**
	 * Tells the DownloadResult whether an error occurred or not
	 * @param error True if an error occurred false if not
	 */
	public void setError(boolean error) {
		this.error = error;
		if (!error){
			errorMsg="";
		}
	}

	/**
	 * @return Returns the error message that the occurred during the download
	 */
	public String getErrorMsg() {
		return errorMsg;
	}

	/**
	 * Sets the error message of the download
	 * @param errorMsg The error message of the error
	 */
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	/**
	 * @return Returns the DownloadRequest which produced the error
	 */
	public DownloadRequest getErrorRequest() {
		return errorRequest;
	}

	/**
	 * Sets the request which produced the errors
	 * @param errorRequest
	 */
	public void setErrorRequest(DownloadRequest errorRequest) {
		this.errorRequest = errorRequest;
	}

	/**
	 * Sets this Download result to Error. Meaning that the download failed.
	 * @param ex The error message of the download
	 * @param request The request which produced the error
	 */
	public void setError(Exception ex, DownloadRequest request) {
		error=true;
		errorMsg=ex.getMessage();
		errorRequest=request;
	}
	
	/**
	 * Sets the DownloadResult to Accomplished
	 * @param idOfDownloadRequest The id of The DownloadRequest
	 * @param markers The List of markers which got downloaded
	 * @param ds The Datasource which made the request
	 */
	public void setAccomplish(String idOfDownloadRequest, List<Marker> markers, DataSource ds ) {
		setIdOfDownloadRequest(idOfDownloadRequest);
		setMarkers(markers);
		setDataSource(ds);
		setError(false);
		errorMsg="NO ERROR";
		errorRequest=null;
	}
}
