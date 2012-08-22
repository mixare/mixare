/*
 * Copyright (C) 2012- Peer internet solutions
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

import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;

public class Plugin {

	private PluginStatus pluginStatus;
	private ServiceInfo serviceInfo;
	private String lable;
	private Drawable logo;
	private PluginType pluginType;
	private PluginConnection pluginConnection;

	public Plugin(PluginStatus pluginStatus, ServiceInfo serviceInfo, String lable, Drawable logo, PluginType pluginType) {
		this.setPluginStatus(pluginStatus);
		this.setServiceInfo(serviceInfo);
		this.setLable(lable);
		this.setLogo(logo);
		this.setPluginType(pluginType);
		this.setPluginConnection(pluginType.getPluginConnection());
	}

	public PluginStatus getPluginStatus() {
		return pluginStatus;
	}

	public void setPluginStatus(PluginStatus pluginStatus) {
		this.pluginStatus = pluginStatus;
	}
	
	public ServiceInfo getServiceInfo() {
		return serviceInfo;
	}

	public void setServiceInfo(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	public String getLable() {
		return lable;
	}

	public void setLable(String lable) {
		this.lable = lable;
	}

	public Drawable getLogo() {
		return logo;
	}

	public void setLogo(Drawable logo) {
		this.logo = logo;
	}
	
	public PluginType getPluginType() {
		return pluginType;
	}
	
	public void setPluginType(PluginType pluginType) {
		this.pluginType = pluginType;
	}
	
	public PluginConnection getPluginConnection() {
		if (pluginConnection == null) {
			pluginConnection = pluginType.getPluginConnection();
		}
		return pluginConnection;
	}
	
	public void setPluginConnection(PluginConnection pluginConnection) {
		this.pluginConnection = pluginConnection;
	}

	public Plugin clone() {
		Plugin plugin = new Plugin(this.getPluginStatus(), this.getServiceInfo(), this.getLable(), this.getLogo(), this.getPluginType());
		plugin.setPluginConnection(this.getPluginConnection());
		return plugin;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		
		if ( o == null || o.getClass() != this.getClass() ) return false;
		
		Plugin plugin = (Plugin) o;
		
		return this.getLable() == plugin.getLable() && this.getPluginType() == plugin.getPluginType() && this.getServiceInfo() == this.getServiceInfo() && this.getPluginStatus() == plugin.getPluginStatus();
	}
}