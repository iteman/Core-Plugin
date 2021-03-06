/*******************************************************************************
 * Copyright (c) 2012 The PDT Extension Group (https://github.com/pdt-eg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.pdtextensions.core.util;

import org.eclipse.php.internal.debug.core.preferences.PHPexeItem;
import org.eclipse.php.internal.debug.core.preferences.PHPexes;
import org.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;
import org.eclipse.php.internal.debug.core.zend.communication.DebuggerCommunicationDaemon;
import org.pdtextensions.core.exception.ExecutableNotFoundException;


@SuppressWarnings("restriction")
public class LaunchUtil {
	
	public static String getPHPExecutable() throws ExecutableNotFoundException
	{
		return getPHPExecutable("");
	}
	
	public static String getPHPExecutable(String debugger) throws ExecutableNotFoundException {

		PHPexeItem phpExe = null;
		
		phpExe = PHPexes.getInstance().getDefaultItem(debugger);
		
		if (phpExe != null) {
			return phpExe.getExecutable().toString();
		}
		
		phpExe = PHPexes.getInstance().getDefaultItem(XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID);
		
		if (phpExe != null) {
			return phpExe.getExecutable().toString();
		}
		
		phpExe = PHPexes.getInstance().getDefaultItem(DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID);

		if (phpExe != null) {
			return phpExe.getExecutable().toString();
		}

		throw new ExecutableNotFoundException("Unable to find PHP executable");
	}
}
