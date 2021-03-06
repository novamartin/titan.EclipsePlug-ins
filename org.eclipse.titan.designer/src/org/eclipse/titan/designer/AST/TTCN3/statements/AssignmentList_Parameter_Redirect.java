/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.Location;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameter;
import org.eclipse.titan.designer.AST.TTCN3.types.SignatureFormalParameterList;
import org.eclipse.titan.designer.AST.TTCN3.types.Signature_Type;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * Represents the parameter redirection of a getcall/getreply operation.
 * <p>
 * Provided with assignment list notation.
 *
 * @author Kristof Szabados
 * */
public final class AssignmentList_Parameter_Redirect extends Parameter_Redirect {
	private static final String FULLNAMEPART = ".parameterassignments";

	private final Parameter_Assignments assignments;

	public AssignmentList_Parameter_Redirect(final Parameter_Assignments assignments) {
		this.assignments = assignments;

		if (assignments != null) {
			assignments.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (assignments == child) {
			return builder.append(FULLNAMEPART);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (assignments != null) {
			assignments.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void checkErroneous(final CompilationTimeStamp timestamp) {
		final HashMap<String, Parameter_Assignment> parameterMap = new HashMap<String, Parameter_Assignment>();
		for (int i = 0, size = assignments.getNofParameterAssignments(); i < size; i++) {
			final Parameter_Assignment assignment = assignments.getParameterAssignmentByIndex(i);
			final String name = assignment.getIdentifier().getName();
			if (parameterMap.containsKey(name)) {
				assignment.getLocation().reportSemanticError(
						MessageFormat.format("Duplicate redirect for parameter `{0}''", assignment.getIdentifier()
								.getDisplayName()));
				final Location otherLocation = parameterMap.get(name).getLocation();
				otherLocation.reportSemanticWarning(MessageFormat.format(
						"A variable entry for parameter `{0}'' is already given here", assignment.getIdentifier()
						.getDisplayName()));
			} else {
				parameterMap.put(name, assignment);
			}

			checkVariableReference(timestamp, assignment.getReference(), null);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp, final Signature_Type signature, final boolean isOut) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		final SignatureFormalParameterList parameterList = signature.getParameterList();
		if (parameterList.getNofParameters() == 0) {
			getLocation().reportSemanticError(MessageFormat.format(SIGNATUREWITHOUTPARAMETERS, signature.getTypename()));
			checkErroneous(timestamp);
			return;
		}

		boolean errorFlag = false;
		final HashMap<String, Parameter_Assignment> parameterMap = new HashMap<String, Parameter_Assignment>();
		for (int i = 0, size = assignments.getNofParameterAssignments(); i < size; i++) {
			final Parameter_Assignment assignment = assignments.getParameterAssignmentByIndex(i);
			final String name = assignment.getIdentifier().getName();
			if (parameterMap.containsKey(name)) {
				assignment.getLocation().reportSemanticError(
						MessageFormat.format("Duplicate redirect for parameter `{0}''", assignment.getIdentifier()
								.getDisplayName()));
				final Location otherLocation = parameterMap.get(name).getLocation();
				otherLocation.reportSemanticWarning(MessageFormat.format(
						"A variable entry for parameter `{0}'' is already given here", assignment.getIdentifier()
						.getDisplayName()));
				errorFlag = true;
			} else {
				parameterMap.put(name, assignment);
			}

			if (parameterList.hasParameterWithName(name)) {
				final SignatureFormalParameter parameterTemplate = parameterList.getParameterByName(name);
				if (isOut) {
					if (SignatureFormalParameter.ParamaterDirection.PARAM_IN == parameterTemplate.getDirection()) {
						final String message = MessageFormat.format(
								"Parameter `{0}'' of signature `{1}'' has `in'' direction", assignment
								.getIdentifier().getDisplayName(), signature.getTypename());
						assignment.getLocation().reportSemanticError(message);
						errorFlag = true;
					}
				} else {
					if (SignatureFormalParameter.ParamaterDirection.PARAM_OUT == parameterTemplate.getDirection()) {
						final String message = MessageFormat.format(
								"Parameter `{0}'' of signature `{1}'' has `out'' direction", assignment
								.getIdentifier().getDisplayName(), signature.getTypename());
						assignment.getLocation().reportSemanticError(message);
						errorFlag = true;
					}
				}

				checkVariableReference(timestamp, assignment.getReference(), parameterTemplate.getType());
			} else {
				assignment.getLocation().reportSemanticError(
						MessageFormat.format("Signature `{0}'' does not have parameter named `{1}''",
								signature.getTypename(), assignment.getIdentifier().getDisplayName()));
				errorFlag = true;
				checkVariableReference(timestamp, assignment.getReference(), null);
			}
		}

		if (!errorFlag) {
			// converting the AssignmentList to VariableList
			final Variable_Entries variableEntries = new Variable_Entries();
			final int upperLimit = isOut ? parameterList.getNofOutParameters() : parameterList.getNofInParameters();
			for (int i = 0; i < upperLimit; i++) {
				final SignatureFormalParameter parameter = isOut ? parameterList.getOutParameterByIndex(i) : parameterList
						.getInParameterByIndex(i);
				final String name = parameter.getIdentifier().getName();
				if (parameterMap.containsKey(name)) {
					variableEntries.add(new Variable_Entry(parameterMap.get(name).getReference()));
				} else {
					variableEntries.add(new Variable_Entry());
				}
			}
		}

		lastTimeChecked = timestamp;
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		assignments.updateSyntax(reparser, isDamaged);
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (assignments == null) {
			return;
		}

		assignments.findReferences(referenceFinder, foundIdentifiers);
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (assignments != null && !assignments.accept(v)) {
			return false;
		}
		return true;
	}
}
