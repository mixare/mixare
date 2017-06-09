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
package org.mixare.mgr.downloader;

/**
 * This class establishes a connection and downloads the data for each entry in
 * its todo list one after another.
 */
public interface DownloadManager {

	/**
	 * Possible state of this Manager
	 */
	enum DownloadManagerState {
		OnLine, //manage downlad request 
		OffLine, // No OnLine
		Downloading, //Process some Download Request
		Confused // Internal state not congruent
	}

	/**
	 * Reset all Request and Responce
	 */
	void resetActivity();

	/**
	 * Submit new DownloadRequest
	 * 
	 * @param job
	 * @return reference Of Job or null if job is rejected
	 */
	String submitJob(DownloadRequest job);

	/**
	 * Get result of job if exist, null otherwise
	 * 
	 * @param jobId reference of Job
	 * @return result 
	 */
	DownloadResult getReqResult(String jobId);

	/**
	 * Pseudo Iterator on results 
	 * @return actual Download Result
	 */
	DownloadResult getNextResult();

	/**
	 * Gets the number of downloaded results
	 * @return the number of results
	 */
	int getResultSize();
	
	/**
	 * check if all Download request is done
	 *  
	 * @return BOOLEAN
	 */
	Boolean isDone();

	/**
	 * Request to active the service
	 */
	void switchOn();

	/**
	 * Request to deactive the service
	 */
	void switchOff();

	/**
	 * Request state of service
	 * @return
	 */
	DownloadManagerState getState();

}