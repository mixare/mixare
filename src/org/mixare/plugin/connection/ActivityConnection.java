package org.mixare.plugin.connection;

import android.app.Activity;
import android.content.ServiceConnection;


/**
 * An interface for classes that are communicating with plugin Activities instead of Services.
 * @author A. Egal
 */
public interface ActivityConnection extends ServiceConnection{

	void startActivityForResult(Activity activity);
	
}
