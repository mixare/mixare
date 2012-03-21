package org.mixare.plugin;

import org.mixare.plugin.connection.BootStrapActivityConnection;
import org.mixare.plugin.connection.MarkerServiceConnection;
import org.mixare.plugin.connection.PluginConnection;

/**
 * This enum contains the plugin types that can be loaded, they also contain
 * the data needed to succesfully use or load the plugins.
 * @author A.Egal
 *
 */
public enum PluginType{
	
	MARKER(){
		
		public String getActionName() {
			return "org.mixare.plugin.marker";
		}
		
		public PluginConnection getPluginConnection() {
			PluginConnection pluginConnection = new MarkerServiceConnection();
			pluginConnection.setPluginType(this);
			return pluginConnection;
		}

		public Loader getLoader() {
			return Loader.Service;
		}		
	},
	BOOTSTRAP(){

		public String getActionName() {
			return "org.mixare.plugin.bootstrap";
		}

		public PluginConnection getPluginConnection() {
			PluginConnection pluginConnection = new BootStrapActivityConnection();
			pluginConnection.setPluginType(this);
			return pluginConnection;
		}

		public Loader getLoader() {
			return Loader.Activity;
		}
		
	};	
	
	/** The package name to find the plugin */
	public abstract String getActionName();
	/** The loader to know how to handle a plugin (activity / service) */
	public abstract Loader getLoader();
	/** Returns the instance of an activity plugin loader that can load activity plugins */
	public abstract PluginConnection getPluginConnection();
}	

/**
 * A loader enum, a activity loader means that the plugin is a activity, and it should be loaded
 * like an activity, A service loader is a plugin that can run in the background and is not visible.
 * @author A. Egal
 */
enum Loader {
	Activity,
	Service
}
