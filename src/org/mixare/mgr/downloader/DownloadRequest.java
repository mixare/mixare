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

import org.mixare.data.DataSource;

public class DownloadRequest {
	private DataSource source;
	private String params;
	
	public DownloadRequest(DataSource source) {
		this(source, "");
	}

	public DownloadRequest(DataSource source, String params) {
		super();
		if (source==null){
			throw new IllegalArgumentException("DataSource is NULL");
		}
		if (!source.isWellFormed()){
			throw new IllegalArgumentException("DataSource is not well formed");
		}
		
		this.source = source;
		this.params = params;
	}

	public DataSource getSource() {
		return source;
	}

	public void setSource(DataSource source) {
		this.source = source;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}
	
	public String toString(){
		return " type: "
				+ getSource().getType() + ", params: "
				+ getParams() + ", url: "
				+ getSource().getUrl();
	}

}
