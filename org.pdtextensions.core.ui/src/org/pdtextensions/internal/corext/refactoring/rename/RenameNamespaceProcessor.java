/*******************************************************************************
 * Copyright (c) 2013 The PDT Extension Group (https://github.com/pdt-eg)
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.pdtextensions.internal.corext.refactoring.rename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.SourceParserUtil;
import org.eclipse.dltk.core.search.IDLTKSearchConstants;
import org.eclipse.dltk.core.search.SearchEngine;
import org.eclipse.dltk.core.search.SearchMatch;
import org.eclipse.dltk.core.search.SearchParticipant;
import org.eclipse.dltk.core.search.SearchPattern;
import org.eclipse.dltk.core.search.SearchRequestor;
import org.eclipse.dltk.core.search.TypeReferenceMatch;
import org.eclipse.dltk.internal.corext.refactoring.RefactoringAvailabilityTester;
import org.eclipse.dltk.internal.corext.refactoring.changes.DynamicValidationRefactoringChange;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.eclipse.php.internal.core.PHPLanguageToolkit;
import org.eclipse.php.internal.core.compiler.ast.nodes.FullyQualifiedReference;
import org.eclipse.php.internal.core.compiler.ast.visitor.PHPASTVisitor;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.ReplaceEdit;
import org.pdtextensions.core.PHPNamespace;
import org.pdtextensions.core.PHPType;
import org.pdtextensions.core.ui.PEXUIPlugin;
import org.pdtextensions.core.ui.refactoring.IPHPRefactorings;
import org.pdtextensions.core.ui.refactoring.IRefactoringProcessorIds;
import org.pdtextensions.core.util.PDTModelUtils;
import org.pdtextensions.internal.corext.refactoring.Checks;
import org.pdtextensions.internal.corext.refactoring.RefactoringCoreMessages;

/**
 * @since 0.20.0
 */
@SuppressWarnings("restriction")
public class RenameNamespaceProcessor extends PHPRenameProcessor {
	public RenameNamespaceProcessor(IType modelElement) {
		super(modelElement);
	}

	@Override
	public RefactoringStatus checkNewElementName(String newName) throws CoreException {
		return Checks.checkTypeName(newName);
	}

	@Override
	public String getIdentifier() {
		return IRefactoringProcessorIds.RENAME_NAMESPACE_PROCESSOR;
	}

	@Override
	public String getProcessorName() {
		return RefactoringCoreMessages.RenameNamespaceRefactoring_name;
	}

	@Override
	public boolean needsSavedEditors() {
		return true;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		pm.beginTask(RefactoringCoreMessages.RenameRefactoring_checking, 1);

		try {
			DynamicValidationRefactoringChange result = new DynamicValidationRefactoringChange(createRefactoringDescriptor(), getProcessorName(), changeManager.getAllChanges());
			IResource resource = modelElement.getResource();
			if (resource instanceof IFolder && willRenameCU((IFolder) resource)) {
				result.add(new RenameResourceChange(resource.getFullPath(), getNewElementName()));
			}
			pm.worked(1);

			return result;
		} finally {
			changeManager.clear();
			pm.done();
		}
	}

	@Override
	protected IFile[] getChangedFiles() throws CoreException {
		List<IFile> result = new ArrayList<IFile>();
		result.addAll(Arrays.asList(super.getChangedFiles()));

		IResource resource = modelElement.getResource();
		if (resource instanceof IFolder && willRenameCU((IFolder) resource)) {
		}

		return result.toArray(new IFile[result.size()]);
	}

	@Override
	protected String getRefactoringId() {
		return IPHPRefactorings.RENAME_NAMESPACE;
	}

	@Override
	protected RefactoringStatus updateReferences(IProgressMonitor pm) throws CoreException {
		new SearchEngine().search(
			SearchPattern.createPattern(
				modelElement,
				IDLTKSearchConstants.REFERENCES,
				SearchPattern.R_FULL_MATCH | SearchPattern.R_ERASURE_MATCH,
				PHPLanguageToolkit.getDefault()
			),
			new SearchParticipant[]{ SearchEngine.getDefaultSearchParticipant() },
			SearchEngine.createWorkspaceScope(PHPLanguageToolkit.getDefault()),
			new SearchRequestor() {
				@Override
				public void acceptSearchMatch(final SearchMatch match) throws CoreException {
					if (match instanceof TypeReferenceMatch && match.getElement() instanceof IModelElement) {
						final ISourceModule module = (ISourceModule) ((IModelElement) match.getElement()).getAncestor(IModelElement.SOURCE_MODULE);
						if (module != null && RefactoringAvailabilityTester.isRenameAvailable(module)) {
							ModuleDeclaration moduleDeclaration = SourceParserUtil.getModuleDeclaration(module);
							if (moduleDeclaration != null) {
								try {
									moduleDeclaration.traverse(new PHPASTVisitor() {
										@Override
										public boolean visit(FullyQualifiedReference s) throws Exception {
											if (s.sourceStart() == match.getOffset() && s.sourceEnd() == match.getOffset() + match.getLength()) {
												IModelElement sourceElement = PDTModelUtils.getSourceElement(module, s.sourceStart(), s.matchLength());
												if (sourceElement != null) {
													IType sourceType = (IType) sourceElement.getAncestor(IModelElement.TYPE);
													if (sourceType != null && new PHPType(sourceType).isInstanceOf((IType) modelElement)) {
														int offset;
														if (s.getNamespace() == null) {
															offset = s.sourceStart();
														} else {
															if ("\\".equals(s.getNamespace().getName())) { //$NON-NLS-1$
																offset = s.getNamespace().sourceEnd();
															} else {
																offset = s.getNamespace().sourceEnd() + 1;
															}
														}

														try {
															addTextEdit(
																changeManager.get(module),
																getProcessorName(),
																new ReplaceEdit(offset, getCurrentElementName().length(), getNewElementName())
															);
														} catch (MalformedTreeException e) {
															// conflicting update -> omit text match
														}
													}
												}

												return false;
											}

											return true;
										}
									});
								} catch (Exception e) {
									throw new CoreException(new Status(IStatus.ERROR, PEXUIPlugin.PLUGIN_ID, e.getMessage(), e));
								}
							}
						}
					}
				}
			},
			new SubProgressMonitor(pm, 1000)
		);

		return new RefactoringStatus();
	}

	private boolean willRenameCU(IFolder folder) throws CoreException {
		if (folder.isLinked()) return false;
		if (!new PHPNamespace((IType) modelElement).inResourceWithSameName()) return false;

		return true;
	}
}
