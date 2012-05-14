package org.mixare.mgr.downloader;

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
	
	
	

}
