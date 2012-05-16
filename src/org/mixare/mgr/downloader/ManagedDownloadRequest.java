/*
 * Copyleft 2012 - Alessandro Staniscia
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

class ManagedDownloadRequest {
	
	private DownloadRequest content;
	
	private String uniqueKey;

	public ManagedDownloadRequest(final DownloadRequest content) {
		this.content = content;
		this.uniqueKey = "" + System.currentTimeMillis()+"_"+hashCode();
	}

	public DownloadRequest getOriginalRequest() {
		return content;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result
				+ ((uniqueKey == null) ? 0 : uniqueKey.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ManagedDownloadRequest other = (ManagedDownloadRequest) obj;
		return getOriginalRequest().getSource().getName().equals(other.getOriginalRequest().getSource().getName());
	}
	
	

}
