package org.mixare.plugin;

public class PluginNotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;
	String message;

	public PluginNotFoundException() {
		super();
		message = "plugin not found";
	}

	public PluginNotFoundException(String message) {
		super(message);
		this.message = message;
	}

	public String getError() {
		return message;
	}

}
