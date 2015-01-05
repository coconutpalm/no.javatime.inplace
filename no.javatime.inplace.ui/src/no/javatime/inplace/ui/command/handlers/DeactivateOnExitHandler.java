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
package no.javatime.inplace.ui.command.handlers;

import no.javatime.inplace.dl.preferences.intface.CommandOptions;
import no.javatime.inplace.region.intface.InPlaceException;

/**
 * Checked menu item to deactivate all bundles on IDE shutdown
 */
public class DeactivateOnExitHandler extends AbstractOptionsHandler {

	public static String commandId = "no.javatime.inplace.command.deactivate";

	@Override
	protected void storeValue(Boolean value) throws InPlaceException {

		CommandOptions cmdStore = getOptionsService();
		cmdStore.setIsDeactivateOnExit(value);
		
	}

	@Override
	protected boolean getStoredValue() throws InPlaceException {

		CommandOptions cmdStore = getOptionsService();
		return cmdStore.isDeactivateOnExit();
	}

	@Override
	public String getCommandId() {

		return commandId;
	}
}
