/*******************************************************************************
 * Copyright (c) 2013 The PDT Extension Group (https://github.com/pdt-eg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.pdtextensions.internal.corext.refactoring.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringCoreMessages;
import org.eclipse.dltk.internal.corext.refactoring.rename.RenameModelElementProcessor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
import org.pdtextensions.internal.corext.refactoring.Checks;

/**
 * @since 0.17.0
 */
@SuppressWarnings("restriction")
public class RenameFieldProcessor extends RenameModelElementProcessor {
	public static final String IDENTIFIER = "org.pdtextensions.internal.corext.refactoring.rename.renameFieldProcessor"; //$NON-NLS-1$

	public RenameFieldProcessor(IField field) {
		super(field, PHPLanguageToolkit.getDefault());
	}

	@Override
	public RefactoringStatus checkNewElementName(String newName) throws CoreException {
		return Checks.checkFieldName(newName);
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.RenameFieldRefactoring_name;
	}

	@Override
	public boolean isApplicable() throws CoreException {
	    return Checks.isAvailable(fModelElement);
	}

	@Override
	public boolean needsSavedEditors() {
		return true;
	}
}
