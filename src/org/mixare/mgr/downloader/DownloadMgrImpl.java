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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.mixare.MixContext;
import org.mixare.MixView;
import org.mixare.data.convert.DataConvertor;
import org.mixare.lib.marker.Marker;
import org.mixare.mgr.HttpTools;

import android.util.Log;


class DownloadMgrImpl implements Runnable, DownloadManager {

	private boolean stop = false, pause = false, proceed = false;
	private static int NOT_STARTED = 0, CONNECTING = 1, CONNECTED = 2,	PAUSED = 3, STOPPED = 4;
	private int state = NOT_STARTED;

	private int id = 0;
	private HashMap<String, DownloadRequest> todoList = new HashMap<String, DownloadRequest>();
	private HashMap<String, DownloadResult> doneList = new HashMap<String, DownloadResult>();
	InputStream is;

	private String currJobId = null;

	MixContext ctx;
	private Thread downloadThread;

	public DownloadMgrImpl(MixContext ctx) {
		this.ctx = ctx;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#run()
	 */
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

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#checkForConnection()
	 */
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
		result.setError(true);
		try {
			if (ctx != null 
				&& request != null
				//TODO: POSSIBLE BUG  NO STREAM CLOSED !! 
				&& HttpTools.getHttpGETInputStream(request.source.getUrl(),  ctx.getActualMixView().getContentResolver() ) != null ){

				is = HttpTools.getHttpGETInputStream(request.source.getUrl() + request.params, ctx.getActualMixView().getContentResolver());
				String tmp = HttpTools.getHttpInputString(is);

				// try loading Marker data

				List<Marker> markers = DataConvertor.getInstance().load(
						request.source.getUrl(), tmp, request.source);
				result.setMarkers(markers);

				result.setDataSource(request.source);
				result.setError(false);
				HttpTools.returnHttpInputStream(is);
				is = null;
			}
		} catch (Exception ex) {
			result.setError(ex,request);
			try {
				HttpTools.returnHttpInputStream(is);
			} catch (Exception ignore) {

			}
			ex.printStackTrace();
		}
		currJobId = null;
		return result;
	}



	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#purgeLists()
	 */
	public synchronized void purgeLists() {
		todoList.clear();
		doneList.clear();
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#submitJob(org.mixare.mgr.downloader.DownloadRequest)
	 */
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

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#isReqComplete(java.lang.String)
	 */
	public synchronized boolean isReqComplete(String jobId) {
		return doneList.containsKey(jobId);
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#getReqResult(java.lang.String)
	 */
	public synchronized DownloadResult getReqResult(String jobId) {
		DownloadResult result = doneList.get(jobId);
		doneList.remove(jobId);

		return result;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#getActiveReqId()
	 */
	public String getActiveReqId() {
		return currJobId;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#pause()
	 */
	public void pause() {
		pause = true;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#restart()
	 */
	public void restart() {
		pause = false;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#stop()
	 */
	public void stop() {
		stop = true;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#getNextResult()
	 */
	public synchronized DownloadResult getNextResult() {
		if (!doneList.isEmpty()) {
			String nextId = doneList.keySet().iterator().next();
			DownloadResult result = doneList.get(nextId);
			doneList.remove(nextId);
			return result;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#isDone()
	 */
	public Boolean isDone() {
		return todoList.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.mixare.mgr.downloader.DownloadManager#goOnline()
	 */
	public void switchOn() {
		downloadThread = new Thread(this);
		downloadThread.start();
	}

	
	public void switchOff() {
		// TODO Auto-generated method stub
		
	}

	
	public void swithToPause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DownloadManagerState getState() {
		// TODO Auto-generated method stub
		return null;
	}
}


