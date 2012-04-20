package org.mixare.plugin.connection;

import org.mixare.data.convert.DataConvertor;
import org.mixare.lib.service.IDataHandlerService;
import org.mixare.plugin.PluginConnection;
import org.mixare.plugin.RemoteDataHandler;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class DataHandlerServiceConnection extends PluginConnection implements
		ServiceConnection {

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		// get instance of the aidl binder
		IDataHandlerService iDataHandlerService = IDataHandlerService.Stub
				.asInterface(service);
		RemoteDataHandler rm = new RemoteDataHandler(iDataHandlerService);
		rm.buildDataHandler();
		DataConvertor.getInstance().addDataProcessor(rm);
		storeFoundPlugin();
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		DataConvertor.getInstance().clearDataProcessors();
	}

}
