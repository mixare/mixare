package org.mixare.plugin.connection;

import org.mixare.plugin.PluginLoader;
import org.mixare.plugin.PluginType;

/**
 * Main interface for activity and service connections
 * @author A.Egal
 */
public abstract class PluginConnection {

	PluginType pluginType;
	
	public void setPluginType(PluginType pluginType) {
		this.pluginType = pluginType;
	}
		
	public String getPluginName() {
		return pluginType.getActionName();
	}
	
	void storeFoundPlugin(){
		PluginLoader.getInstance().addFoundPluginToMap(pluginType.toString(), this);
	}
	
	void storeFoundPlugin(String pluginName){
		PluginLoader.getInstance().addFoundPluginToMap(pluginName, this);
	}
}
