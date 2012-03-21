package org.mixare.plugin;

/**
 * A custom exception that will be thrown when something "unexpected" happens while loading a plugin.
 * @author A. Egal
 */
public class PluginNotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

	public PluginNotFoundException() {
		super();
	}

	public PluginNotFoundException(String message) {
		super(message);
	}

}
