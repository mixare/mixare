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

import org.json.JSONObject;
import org.mixare.data.Json;

import android.util.Log;


public class DownloadManager implements Runnable {

	private boolean stop = false, pause = false, proceed = false;
	public static int NOT_STARTED = 0, CONNECTING = 1, CONNECTED = 2, PAUSED = 3, STOPPED = 4;
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
	public int checkForConnection(){
		return state;
	}

	private void sleep(long ms){
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
		try {
			if(ctx.getHttpGETInputStream(request.url)!=null){
				
				is = ctx.getHttpGETInputStream(request.url);
				String tmp = ctx.getHttpInputString(is);
				//String ss = ctx.unescapeHTML(tmp, 0);

				//Log.d("JSON", ss);
				JSONObject root = null;
				root = new JSONObject(tmp);
				Json layer = new Json();
				
				Log.i(MixView.TAG, "loading JSON data");				

				layer.load(root);
				result.obj = layer;
				
				result.format = request.format;
				result.error = false;
				result.errorMsg = null;

			ctx.returnHttpInputStream(is);
			is = null;
			}

		} catch (Exception ex) {
			result.obj = null;
			result.error = true;
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
		String jobId = "ID_" + (id++);
		todoList.put(jobId, job);

		return jobId;
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

}

class DownloadRequest {
	int format;
	String url;
	String params;
}

class DownloadResult {
	int format;
	Object obj;

	boolean error;
	String errorMsg;
	DownloadRequest errorRequest;
}