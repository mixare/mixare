package org.mixare.mgr.downloader;

/**
 * This class establishes a connection and downloads the data for each entry in
 * its todo list one after another.
 */
public interface DownloadManager {

	enum DownloadManagerState {
		OnLine, OffLine, Connection, Confused
	}

	void purgeLists();

	String submitJob(DownloadRequest job);

	boolean isReqComplete(String jobId);

	DownloadResult getReqResult(String jobId);

	String getActiveReqId();

	DownloadResult getNextResult();

	Boolean isDone();

	void switchOn();

	void switchOff();

	DownloadManagerState getState();

}