/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST.TTCN3.statements;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.titan.designer.AST.ASTVisitor;
import org.eclipse.titan.designer.AST.INamedNode;
import org.eclipse.titan.designer.AST.IType;
import org.eclipse.titan.designer.AST.IValue;
import org.eclipse.titan.designer.AST.IValue.Value_type;
import org.eclipse.titan.designer.AST.PortReference;
import org.eclipse.titan.designer.AST.ReferenceFinder;
import org.eclipse.titan.designer.AST.ReferenceFinder.Hit;
import org.eclipse.titan.designer.AST.Scope;
import org.eclipse.titan.designer.AST.Value;
import org.eclipse.titan.designer.AST.TTCN3.Expected_Value_type;
import org.eclipse.titan.designer.AST.TTCN3.types.PortTypeBody;
import org.eclipse.titan.designer.AST.TTCN3.types.Port_Type;
import org.eclipse.titan.designer.AST.TTCN3.values.Expression_Value;
import org.eclipse.titan.designer.AST.TTCN3.values.expressions.ExpressionStruct;
import org.eclipse.titan.designer.compiler.JavaGenData;
import org.eclipse.titan.designer.parsers.CompilationTimeStamp;
import org.eclipse.titan.designer.parsers.ttcn3parser.ReParseException;
import org.eclipse.titan.designer.parsers.ttcn3parser.TTCN3ReparseUpdater;

/**
 * @author Kristof Szabados
 * */
public final class Map_Statement extends Statement {
	public static final String BOTHENDSARETESTCOMPONENTPORTS = "Both endpoints of the mapping are test component ports";
	public static final String BOTHENDSARESYSTEMPORTS = "Both endpoints of the mapping are system ports";
	public static final String INCONSISTENTMAPPING1 = "The mapping between test component port type `{0}'' and system port type `{1}''"
			+ " is not consistent";
	public static final String INCONSISTENTMAPPING2 = "The mapping between system port type `{0}'' and test component port type `{1}'' "
			+ "is not consistent";
	public static final String INCONSISTENTMAPPING3 = "The mapping between port types `{0}'' and `{1}'' is not consistent";

	private static final String FULLNAMEPART1 = ".componentreference1";
	private static final String FULLNAMEPART2 = ".portreference1";
	private static final String FULLNAMEPART3 = ".componentreference2";
	private static final String FULLNAMEPART4 = ".portreference2";
	private static final String STATEMENT_NAME = "map";

	private final Value componentReference1;
	private final PortReference portReference1;
	private final Value componentReference2;
	private final PortReference portReference2;

	public Map_Statement(final Value componentReference1, final PortReference portReference1, final Value componentReference2,
			final PortReference portReference2) {
		this.componentReference1 = componentReference1;
		this.portReference1 = portReference1;
		this.componentReference2 = componentReference2;
		this.portReference2 = portReference2;

		if (componentReference1 != null) {
			componentReference1.setFullNameParent(this);
		}
		if (portReference1 != null) {
			portReference1.setFullNameParent(this);
		}
		if (componentReference2 != null) {
			componentReference2.setFullNameParent(this);
		}
		if (portReference2 != null) {
			portReference2.setFullNameParent(this);
		}
	}

	@Override
	/** {@inheritDoc} */
	public Statement_type getType() {
		return Statement_type.S_MAP;
	}

	@Override
	/** {@inheritDoc} */
	public String getStatementName() {
		return STATEMENT_NAME;
	}

	@Override
	/** {@inheritDoc} */
	public StringBuilder getFullName(final INamedNode child) {
		final StringBuilder builder = super.getFullName(child);

		if (componentReference1 == child) {
			return builder.append(FULLNAMEPART1);
		} else if (portReference1 == child) {
			return builder.append(FULLNAMEPART2);
		} else if (componentReference2 == child) {
			return builder.append(FULLNAMEPART3);
		} else if (portReference2 == child) {
			return builder.append(FULLNAMEPART4);
		}

		return builder;
	}

	@Override
	/** {@inheritDoc} */
	public void setMyScope(final Scope scope) {
		super.setMyScope(scope);
		if (componentReference1 != null) {
			componentReference1.setMyScope(scope);
		}
		if (componentReference2 != null) {
			componentReference2.setMyScope(scope);
		}
	}

	@Override
	/** {@inheritDoc} */
	public void check(final CompilationTimeStamp timestamp) {
		if (lastTimeChecked != null && !lastTimeChecked.isLess(timestamp)) {
			return;
		}

		IType portType1;
		IType portType2;
		PortTypeBody body1 = null;
		PortTypeBody body2 = null;
		boolean cref1IsTestcomponents = false;
		boolean cref1IsSystem = false;
		boolean cref2IsTestcomponent = false;
		boolean cref2IsSystem = false;

		portType1 = Port_Utility.checkConnectionEndpoint(timestamp, this, componentReference1, portReference1, true);
		if (portType1 != null) {
			body1 = ((Port_Type) portType1).getPortBody();
			if (body1.isInternal()) {
				componentReference1.getLocation().reportSemanticError(
						MessageFormat.format("Port type `{0}'' was marked as `internal''", portType1.getTypename()));
			}
			// sets the referenced assignment of this reference
			portReference1.getRefdAssignment(timestamp, false);
		}

		if (componentReference1 != null) {
			final IValue configReference1 = componentReference1.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			if (Value_type.EXPRESSION_VALUE.equals(configReference1.getValuetype())) {
				switch (((Expression_Value) configReference1).getOperationType()) {
				case MTC_COMPONENT_OPERATION:
					cref1IsTestcomponents = true;
					break;
				case SELF_COMPONENT_OPERATION:
					cref1IsTestcomponents = true;
					break;
				case COMPONENT_CREATE_OPERATION:
					cref1IsTestcomponents = true;
					break;
				case SYSTEM_COMPONENT_OPERATION:
					cref1IsSystem = true;
					break;
				default:
					break;
				}
			}
		}

		portType2 = Port_Utility.checkConnectionEndpoint(timestamp, this, componentReference2, portReference2, true);
		if (portType2 != null) {
			body2 = ((Port_Type) portType2).getPortBody();
			if (body2.isInternal()) {
				componentReference2.getLocation().reportSemanticError(
						MessageFormat.format("Port type `{0}'' was marked as `internal''", portType2.getTypename()));
			}
			// sets the referenced assignment of this reference
			portReference2.getRefdAssignment(timestamp, false);
		}

		if (componentReference2 != null) {
			final IValue configReference2 = componentReference2.getValueRefdLast(timestamp, Expected_Value_type.EXPECTED_DYNAMIC_VALUE, null);
			if (Value_type.EXPRESSION_VALUE.equals(configReference2.getValuetype())) {
				switch (((Expression_Value) configReference2).getOperationType()) {
				case MTC_COMPONENT_OPERATION:
					cref2IsTestcomponent = true;
					break;
				case SELF_COMPONENT_OPERATION:
					cref2IsTestcomponent = true;
					break;
				case COMPONENT_CREATE_OPERATION:
					cref2IsTestcomponent = true;
					break;
				case SYSTEM_COMPONENT_OPERATION:
					cref2IsSystem = true;
					break;
				default:
					break;
				}
			}
		}

		lastTimeChecked = timestamp;

		if (cref1IsTestcomponents && cref2IsTestcomponent) {
			location.reportSemanticError(BOTHENDSARETESTCOMPONENTPORTS);
			return;
		}

		if (cref1IsSystem && cref2IsSystem) {
			location.reportSemanticError(BOTHENDSARESYSTEMPORTS);
			return;
		}

		if (body1 == null || body2 == null || portType1 == null || portType2 == null) {
			return;
		}

		if (cref1IsTestcomponents || cref2IsSystem) {
			if (!body1.isMappable(timestamp, body2)) {
				location.reportSemanticError(MessageFormat.format(INCONSISTENTMAPPING1, portType1.getTypename(),
						portType2.getTypename()));
				body1.reportMappingErrors(timestamp, body2);
			}
		} else if (cref2IsTestcomponent || cref1IsSystem) {
			if (!body2.isMappable(timestamp, body1)) {
				location.reportSemanticError(MessageFormat.format(INCONSISTENTMAPPING2, portType1.getTypename(),
						portType2.getTypename()));
				body2.reportMappingErrors(timestamp, body1);
			}
		} else {
			// we don't know which one is the system port
			if (!body1.isMappable(timestamp, body2) && !body2.isMappable(timestamp, body1)) {
				location.reportSemanticError(MessageFormat.format(INCONSISTENTMAPPING3, portType1.getTypename(),
						portType2.getTypename()));
			}
		}
	}

	@Override
	/** {@inheritDoc} */
	public void updateSyntax(final TTCN3ReparseUpdater reparser, final boolean isDamaged) throws ReParseException {
		if (isDamaged) {
			throw new ReParseException();
		}

		if (componentReference1 != null) {
			componentReference1.updateSyntax(reparser, false);
			reparser.updateLocation(componentReference1.getLocation());
		}

		if (portReference1 != null) {
			portReference1.updateSyntax(reparser, false);
			reparser.updateLocation(portReference1.getLocation());
		}

		if (componentReference2 != null) {
			componentReference2.updateSyntax(reparser, false);
			reparser.updateLocation(componentReference2.getLocation());
		}

		if (portReference2 != null) {
			portReference2.updateSyntax(reparser, false);
			reparser.updateLocation(portReference2.getLocation());
		}
	}

	@Override
	/** {@inheritDoc} */
	public void findReferences(final ReferenceFinder referenceFinder, final List<Hit> foundIdentifiers) {
		if (componentReference1 != null) {
			componentReference1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (portReference1 != null) {
			portReference1.findReferences(referenceFinder, foundIdentifiers);
		}
		if (componentReference2 != null) {
			componentReference2.findReferences(referenceFinder, foundIdentifiers);
		}
		if (portReference2 != null) {
			portReference2.findReferences(referenceFinder, foundIdentifiers);
		}
	}

	@Override
	/** {@inheritDoc} */
	protected boolean memberAccept(final ASTVisitor v) {
		if (componentReference1 != null && !componentReference1.accept(v)) {
			return false;
		}
		if (portReference1 != null && !portReference1.accept(v)) {
			return false;
		}
		if (componentReference2 != null && !componentReference2.accept(v)) {
			return false;
		}
		if (portReference2 != null && !portReference2.accept(v)) {
			return false;
		}
		return true;
	}

	@Override
	/** {@inheritDoc} */
	public void generateCode(final JavaGenData aData, final StringBuilder source) {
		//FIXME this is actually more complex
		ExpressionStruct expression = new ExpressionStruct();

		//FIXME generate code for translation
		expression.expression.append("TTCN_Runtime.mapPort(");
		componentReference1.generateCodeExpression(aData, expression);
		expression.expression.append(", ");
		//FIXME actually _portref and based on component type
		portReference1.generateCode(aData, expression);
		expression.expression.append(".getName(), ");

		componentReference2.generateCodeExpression(aData, expression);
		expression.expression.append(", ");
		//FIXME actually _portref and based on component type
		portReference2.generateCode(aData, expression);
		expression.expression.append(".getName()");
		expression.expression.append(")");

		expression.mergeExpression(source);
	}
}
