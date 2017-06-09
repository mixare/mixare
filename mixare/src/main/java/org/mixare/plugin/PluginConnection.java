/*
 * Copyright (C) 2012- Peer internet solutions & Finalist IT Group
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
package org.mixare.plugin;

/**
 * Main interface for activity and service connections -> in package org.mixare.plugin.connection
 * @author A.Egal
 */
public abstract class PluginConnection {

	private PluginType pluginType;
	
	public void setPluginType(PluginType pluginType) {
		this.pluginType = pluginType;
	}
	
	public PluginType getPluginType() {
		return pluginType;
	}
		
	public String getPluginName() {
		return pluginType.getActionName();
	}
	
	protected void storeFoundPlugin(){
		PluginLoader.getInstance().addFoundPluginToMap(pluginType.toString(), this);
	}
	
	protected void storeFoundPlugin(String pluginName){
		PluginLoader.getInstance().addFoundPluginToMap(pluginName, this);
	}
}
