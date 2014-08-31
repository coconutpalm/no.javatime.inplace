/*******************************************************************************
 * Copyright (c) 2011, 2012 JavaTime project and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	JavaTime project, Eirik Gronsund - initial implementation
 *******************************************************************************/
package no.javatime.util.messages.exceptions;

import no.javatime.util.messages.ExceptionMessage;

/**
 * Mediator without any functionality. For future use.
 */
public class LogException extends BaseException {

	/**
	 * Unique ID of this class
	 */
	public static String ID = LogException.class.getName();

	/**
	 * Unique ID for serialization of this class
	 */
	private static final long serialVersionUID = 945359163488802946L;

	public LogException() {
		super();
	}

	/**
	 * Logs exception
	 * 
	 * @param tex the current thrown exception
	 */
	public LogException(Throwable tex) {
		super(tex);
	}

	/**
	 * Logs exception and message based on message key
	 * 
	 * @param tex the current thrown exception
	 * @param key to access message from resource bundle
	 * @param substitutions message strings to insert into the retrieved message
	 */
	public LogException(Throwable tex, String key, Object... substitutions) {
		super(ExceptionMessage.getInstance().formatString(key, substitutions), tex);
	}

	/**
	 * Logs message based on message key
	 * 
	 * @param key to access message from resource bundle
	 * @param substitutions message strings to insert into the retrieved messages
	 */
	public LogException(String key, Object... substitutions) {
		super(ExceptionMessage.getInstance().formatString(key, substitutions));
	}
}
