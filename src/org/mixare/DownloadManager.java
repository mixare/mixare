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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.mixare.data.DataSource;
import org.mixare.data.convert.DataConvertor;
import org.mixare.lib.marker.Marker;

import android.util.Log;

/**
 * This class establishes a connection and downloads the data for each entry in
 * its todo list one after another.
 */
public class DownloadManager implements Runnable {

	private boolean stop = false, pause = false, proceed = false;
	private static int NOT_STARTED = 0, CONNECTING = 1, CONNECTED = 2,
			PAUSED = 3, STOPPED = 4;
	private int state = NOT_STARTED;

	private int id = 0;
	private HashMap<String, DownloadRequest> todoList = new HashMap<String, DownloadRequest>();
	private HashMap<String, DownloadResult> doneList = new HashMap<String, DownloadResult>();
	InputStream is;

	private String currJobId = null;

	MixContext ctx;

	public DownloadManager(MixContext ctx) {
		this.ctx = ctx;
	}

	public void run() {
		String jobId;
		DownloadRequest request;
		DownloadResult result;

		stop = false;
		pause = false;
		proceed = false;
		state = CONNECTING;

		while (!stop) {
			jobId = null;
			request = null;
			result = null;

			// Wait for proceed
			while (!stop && !pause) {
				synchronized (this) {
					if (todoList.size() > 0) {
						jobId = getNextReqId();
						request = todoList.get(jobId);
						proceed = true;
					}
				}
				// Do proceed
				if (proceed) {
					state = CONNECTED;
					currJobId = jobId;

					result = processRequest(request);

					synchronized (this) {
						todoList.remove(jobId);
						doneList.put(jobId, result);
						proceed = false;
					}
				}
				state = CONNECTING;

				if (!stop && !pause)
					sleep(100);
			}

			// Do pause
			while (!stop && pause) {
				state = PAUSED;
				sleep(100);
			}
			state = CONNECTING;
		}
		// Do stop
		state = STOPPED;
	}

	public int checkForConnection() {
		return state;
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (java.lang.InterruptedException ex) {

		}
	}

	private String getNextReqId() {
		return todoList.keySet().iterator().next();
	}

	private DownloadResult processRequest(DownloadRequest request) {
		DownloadResult result = new DownloadResult();
		// assume an error until everything is fine
		result.error = true;
		try {
			if (ctx != null
					&& request != null
					&& ctx.getHttpGETInputStream(request.source.getUrl()) != null) {

				is = ctx.getHttpGETInputStream(request.source.getUrl()
						+ request.params);
				String tmp = ctx.getHttpInputString(is);

				// try loading Marker data

				List<Marker> markers = DataConvertor.getInstance().load(
						request.source.getUrl(), tmp, request.source);
				result.setMarkers(markers);

				result.source = request.source;
				result.error = false;
				result.errorMsg = null;
				ctx.returnHttpInputStream(is);
				is = null;
			}
		} catch (Exception ex) {
			result.errorMsg = ex.getMessage();
			result.errorRequest = request;

			try {
				ctx.returnHttpInputStream(is);
			} catch (Exception ignore) {
			}

			ex.printStackTrace();
		}

		currJobId = null;

		return result;
	}

	public synchronized void purgeLists() {
		todoList.clear();
		doneList.clear();
	}

	public synchronized String submitJob(DownloadRequest job) {
		if (job != null) {
			String jobId = "";
			// ensure that we only have one download per each datasource
			String currDSname = job.source.getName();
			boolean found = false;
			if (!todoList.isEmpty()) {
				for (String k : todoList.keySet()) {
					if (currDSname.equals(todoList.get(k).source.getName())) {
						found = true;
						jobId = k;
					}
				}
			}
			if (!found) {
				jobId = "ID_" + (id++);
				todoList.put(jobId, job);
				Log.i(MixView.TAG, "Submitted Job with " + jobId + ", type: "
						+ job.source.getType() + ", params: " + job.params
						+ ", url: " + job.source.getUrl());
			}
			return jobId;
		}
		return null;
	}

	public synchronized boolean isReqComplete(String jobId) {
		return doneList.containsKey(jobId);
	}

	public synchronized DownloadResult getReqResult(String jobId) {
		DownloadResult result = doneList.get(jobId);
		doneList.remove(jobId);

		return result;
	}

	public String getActiveReqId() {
		return currJobId;
	}

	public void pause() {
		pause = true;
	}

	public void restart() {
		pause = false;
	}

	public void stop() {
		stop = true;
	}

	public synchronized DownloadResult getNextResult() {
		if (!doneList.isEmpty()) {
			String nextId = doneList.keySet().iterator().next();
			DownloadResult result = doneList.get(nextId);
			doneList.remove(nextId);
			return result;
		}
		return null;
	}

	public Boolean isDone() {
		return todoList.isEmpty();
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
	String errorMsg = "";
	DownloadRequest errorRequest;

	public List<Marker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<Marker> markers) {
		this.markers = markers;
	}

}
