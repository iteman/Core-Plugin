/*******************************************************************************
 * Copyright (c) 2013 The PDT Extension Group (https://github.com/pdt-eg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.pdtextensions.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.IType;

/**
 * This class provides information about the type as a PHP namespace.
 *
 * @since 0.20.0
 */
public class PHPNamespace {
	private final static String NAMESPACE_SEPARATOR = "\\"; //$NON-NLS-1$
	private IType type;

	public PHPNamespace(IType type) {
		Assert.isNotNull(type);

		this.type = type;
	}

	public boolean inResourceWithSameName() throws CoreException {
		return inResourceWithSameName(type.getResource(), type.getElementName());
	}

	private static boolean inResourceWithSameName(IResource resource, String typeName) throws CoreException {
		if (resource instanceof IFolder) {
			return resource.getName().equals(typeName.substring(typeName.lastIndexOf(NAMESPACE_SEPARATOR) + 1));
		} else {
			throw new CoreException(new Status(IStatus.ERROR, PEXCorePlugin.PLUGIN_ID, "The resource is not a folder")); //$NON-NLS-1$
		}
	}
}
