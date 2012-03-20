package org.mixare.plugin;

public class PluginNotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public PluginNotFoundException() {
		super();
	}

	public PluginNotFoundException(String message) {
		super(message);
	}

}
