/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.runtime.core;

import java.text.MessageFormat;
import java.util.ArrayList;

/**
 * TTCN-3 default template
 *
 * @author Kristof Szabados
 * 
 */
public class TitanDefault_template extends Base_Template {
	private Default_Base single_value;

	// value_list part
	private ArrayList<TitanDefault_template> value_list;

	public TitanDefault_template() {
		//intentionally empty
	}

	public TitanDefault_template(final template_sel otherValue) {
		super(otherValue);

		checkSingleSelection(otherValue);
	}

	//originally has component parameter
	public TitanDefault_template(final int otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		if (otherValue != TitanComponent.NULL_COMPREF) {
			throw new TtcnError("Creating a template from an invalid default reference.");
		}

		single_value = null;
	}

	public TitanDefault_template(final Default_Base otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		single_value = otherValue;
	}

	public TitanDefault_template(final TitanDefault otherValue) {
		super(template_sel.SPECIFIC_VALUE);

		if (otherValue.default_ptr == TitanDefault.UNBOUND_DEFAULT) {
			throw new TtcnError("Creating a template from an unbound default reference.");
		}

		single_value = otherValue.default_ptr;
	}

	public TitanDefault_template(final TitanDefault_template otherValue) {
		super();

		copyTemplate(otherValue);
	}

	private void copyTemplate(final TitanDefault_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = otherValue.single_value;
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanDefault_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanDefault_template temp = new TitanDefault_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported default reference template.");
		}

		setSelection(otherValue);
	}

	//originally clean_up
	public void cleanUp() {
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			single_value = null;
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list.clear();
			value_list = null;
			break;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanDefault_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanDefault) {
			return assign((TitanDefault)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", otherValue));
	}

	@Override
	public TitanDefault_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanDefault_template) {
			return assign((TitanDefault_template)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", otherValue));
	}

	//originally operator=
	public TitanDefault_template assign(final template_sel otherValue) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator= has component parameter
	public TitanDefault_template assign(final int otherValue) {
		if ( otherValue != TitanComponent.NULL_COMPREF) {
			throw new TtcnError("Assignment of an invalid default reference to a template.");
		}

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = null;

		return this;
	}

	//originally operator=
	public TitanDefault_template assign(final Default_Base otherValue) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = otherValue;

		return this;
	}

	//originally operator=
	public TitanDefault_template assign(final TitanDefault otherValue) {
		if (otherValue.default_ptr == TitanDefault.UNBOUND_DEFAULT) {
			throw new TtcnError("Assignment of an unbound default reference to a template.");
		}

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = otherValue.default_ptr;

		return this;
	}

	//originally operator=
	public TitanDefault_template assign(final TitanDefault_template otherValue) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	@Override
	public TitanBoolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanDefault) {
			return match((TitanDefault) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to default", otherValue));
	}

	//originally match has component parameter
	public TitanBoolean match(final int otherValue) {
		return match(otherValue, false);
	}

	// originally match has component parameter
	public TitanBoolean match(final int otherValue, final boolean legacy) {
		if(otherValue == TitanComponent.NULL_COMPREF) {
			return new TitanBoolean(false);
		}

		return match((Default_Base) null);
	}

	// originally match
	public TitanBoolean match(final Default_Base otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final Default_Base otherValue, final boolean legacy) {
		if( otherValue == TitanDefault.UNBOUND_DEFAULT) {
			return new TitanBoolean(false);
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return new TitanBoolean(single_value == otherValue);
		case OMIT_VALUE:
			return new TitanBoolean(false);
		case ANY_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			for(int i = 0 ; i < value_list.size(); i++) {
				if(value_list.get(i).match(otherValue, legacy).getValue()) {
					return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
				}
			}
			return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported default reference template.");
		}
	}

	// originally match
	public TitanBoolean match(final TitanDefault otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final TitanDefault otherValue, final boolean legacy) {
		if(! otherValue.isBound().getValue()) {
			return new TitanBoolean(false);
		}

		return match(otherValue.default_ptr);
	}

	// originally valueof
	public TitanDefault valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific default reference template.");
		}

		return new TitanDefault(single_value);
	}

	// originally set_type
	public void setType(final template_sel templateType, final int listLength) {
		if (templateType != template_sel.VALUE_LIST && templateType != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Setting an invalid list type for a default reference template.");
		}

		cleanUp();
		setSelection(templateType);
		value_list = new ArrayList<TitanDefault_template>(listLength);
	}

	// originally list_iem
	public TitanDefault_template listItem(final int listIndex) {
		if (!template_sel.VALUE_LIST.equals(templateSelection) &&
				!template_sel.COMPLEMENTED_LIST.equals(templateSelection)) {
			throw new TtcnError("Accessing a list element of a non-list default reference template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an default reference value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex > value_list.size()) {
			throw new TtcnError("Index overflow in an default reference value list template.");
		}

		return value_list.get(listIndex);
	}

	// originally is_present (with default parameter)
	public TitanBoolean isPresent() {
		return isPresent(false);
	}

	// originally is_present
	public TitanBoolean isPresent(final boolean legacy) {
		if (templateSelection == template_sel.UNINITIALIZED_TEMPLATE) {
			return new TitanBoolean(false);
		}

		return match_omit(legacy).not();
	}

	//originally match_omit (with default parameter)
	public TitanBoolean match_omit() {
		return match_omit(false);
	}

	public TitanBoolean match_omit(final boolean legacy) {
		if (is_ifPresent) {
			return new TitanBoolean(true);
		}

		switch (templateSelection) {
		case OMIT_VALUE:
		case ANY_OR_OMIT:
			return new TitanBoolean(true);
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			if (legacy) {
				// legacy behavior: 'omit' can appear in the value/complement list
				for (int i = 0; i < value_list.size(); i++) {
					if (value_list.get(i).match_omit().getValue()) {
						return new TitanBoolean(templateSelection == template_sel.VALUE_LIST);
					}
				}
				return new TitanBoolean(templateSelection == template_sel.COMPLEMENTED_LIST);
			}
			return new TitanBoolean(false);
		default:
			return new TitanBoolean(false);
		}
	}
}
