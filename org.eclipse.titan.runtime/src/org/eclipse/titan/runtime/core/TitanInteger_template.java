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
 * TTCN-3 integer template
 *
 * Not yet complete rewrite
 */
public class TitanInteger_template extends Base_Template {
	// int_val part
	// TODO maybe should be renamed in core
	private TitanInteger single_value;

	// value_list part
	private ArrayList<TitanInteger_template> value_list;

	// value range part
	private boolean min_is_present, max_is_present;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanInteger min_value, max_value;


	public TitanInteger_template () {
		//do nothing
	}

	public TitanInteger_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanInteger_template (final int otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);
	}

	public TitanInteger_template (final TitanInteger otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound integer value.");

		single_value = new TitanInteger(otherValue);
	}

	public TitanInteger_template (final TitanInteger_template otherValue) {
		copyTemplate(otherValue);
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
		case VALUE_RANGE:
			min_value = null;
			max_value = null;
			break;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	@Override
	public TitanInteger_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanInteger) {
			return assign((TitanInteger)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	@Override
	public TitanInteger_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanInteger_template) {
			return assign((TitanInteger_template)otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	//originally operator=
	public TitanInteger_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanInteger_template assign( final int otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);

		return this;
	}

	//originally operator=
	public TitanInteger_template assign( final TitanInteger otherValue ) {
		otherValue.mustBound("Assignment of an unbound integer value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanInteger(otherValue);

		return this;
	}

	//originally operator=
	public TitanInteger_template assign( final TitanInteger_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}

	private void copyTemplate(final TitanInteger_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanInteger(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanInteger_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanInteger_template temp = new TitanInteger_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_present = otherValue.min_is_present;
			min_is_exclusive = otherValue.min_is_exclusive;
			if(min_is_present) {
				min_value = new TitanInteger(otherValue.min_value);
			}
			max_is_present = otherValue.max_is_present;
			max_is_exclusive = otherValue.max_is_exclusive;
			if(max_is_present) {
				max_value = new TitanInteger(otherValue.max_value);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported integer template.");
		}

		setSelection(otherValue);
	}

	@Override
	public TitanBoolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanInteger) {
			return match((TitanInteger) otherValue, legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to integer", otherValue));
	}

	// originally match
	public TitanBoolean match(final TitanInteger otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final TitanInteger otherValue, final boolean legacy) {
		if(! otherValue.isBound().getValue()) {
			return new TitanBoolean(false);
		}

		switch (templateSelection) {
		case SPECIFIC_VALUE:
			return single_value.operatorEquals(otherValue);
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
		case VALUE_RANGE:{
			boolean lowerMatch = true;
			boolean upperMatch = true;
			if(min_is_present) {
				if(min_is_exclusive) {
					lowerMatch = min_value.isLessThan(otherValue).getValue();
				} else {
					lowerMatch = min_value.isLessThanOrEqual(otherValue).getValue();
				}
			}
			if(max_is_present) {
				if (max_is_exclusive) {
					upperMatch = max_value.isGreaterThan(otherValue).getValue();
				} else {
					upperMatch = max_value.isGreaterThanOrEqual(otherValue).getValue();
				}
			}
			return new TitanBoolean(lowerMatch && upperMatch);
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported integer template.");
		}
	}

	public TitanInteger valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific integer template.");
		}

		return new TitanInteger(single_value);
	}

	// originally set_type
	public void setType(final template_sel templateType) {
		setType(templateType, 0);
	}

	// originally set_type
	public void setType(final template_sel templateType, final int listLength) {
		cleanUp();
		switch (templateType) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			setSelection(templateType);
			value_list = new ArrayList<TitanInteger_template>(listLength);
			for(int i = 0; i < listLength; i++) {
				value_list.add(new TitanInteger_template());
			}
			break;
		case VALUE_RANGE:
			setSelection(template_sel.VALUE_RANGE);
			min_is_present = false;
			max_is_present = false;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		default:
			throw new TtcnError("Setting an invalid type for an integer template.");
		}
	}

	// originally list_iem
	public TitanInteger_template listItem(final int listIndex) {
		if (!template_sel.VALUE_LIST.equals(templateSelection) &&
				!template_sel.COMPLEMENTED_LIST.equals(templateSelection)) {
			throw new TtcnError("Accessing a list element of a non-list integer template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an integer value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex > value_list.size()) {
			throw new TtcnError("Index overflow in an integer value list template.");
		}

		return value_list.get(listIndex);
	}

	// originally set_min
	public void setMin(final int otherMinValue) {
		if (!template_sel.VALUE_RANGE.equals(templateSelection)) {
			throw new TtcnError("Integer template is not range when setting lower limit.");
		}

		if (max_is_present) {
			if (!max_value.isGreaterThanOrEqual(otherMinValue).getValue()) {
				throw new TtcnError("The lower limit of the range is greater than the upper limit in an integer template.");
			}
		}

		min_is_present = true;
		min_is_exclusive = false;
		min_value = new TitanInteger(otherMinValue);
	}

	// originally set_min
	public void setMin(final TitanInteger otherMinValue) {
		otherMinValue.mustBound("Using an unbound value when setting the lower bound in an integer range template.");

		if (!template_sel.VALUE_RANGE.equals(templateSelection)) {
			throw new TtcnError("Integer template is not range when setting lower limit.");
		}

		if (max_is_present) {
			if (!max_value.isGreaterThanOrEqual(otherMinValue).getValue()) {
				throw new TtcnError("The lower limit of the range is greater than the upper limit in an integer template.");
			}
		}

		min_is_present = true;
		min_is_exclusive = false;
		min_value = otherMinValue;
	}

	//originally set_min_exclusive
	public void setMinExclusive(final boolean minExclusive) {
		min_is_exclusive = minExclusive;
	}

	// originally set_max
	public void setMax(final int otherMaxValue) {
		if (!template_sel.VALUE_RANGE.equals(templateSelection)) {
			throw new TtcnError("Integer template is not range when setting upper limit.");
		}

		if (min_is_present) {
			if (min_value.isGreaterThan(otherMaxValue).getValue()) {
				throw new TtcnError("The upper limit of the range is smaller than the lower limit in an integer template.");
			}
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = new TitanInteger(otherMaxValue);
	}

	// originally set_max
	public void setMax(final TitanInteger otherMaxValue) {
		otherMaxValue.mustBound("Using an unbound value when setting the upper bound in an integer range template.");

		if (!template_sel.VALUE_RANGE.equals(templateSelection)) {
			throw new TtcnError("Integer template is not range when setting upper limit.");
		}

		if (max_is_present) {
			if (!max_value.isGreaterThan(otherMaxValue).getValue()) {
				throw new TtcnError("TThe upper limit of the range is smaller than the lower limit in an integer template.");
			}
		}

		max_is_present = true;
		max_is_exclusive = false;
		max_value = otherMaxValue;
	}

	//originally set_max_exclusive
	public void setMaxExclusive(final boolean maxExclusive) {
		max_is_exclusive = maxExclusive;
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

	// originally match_omit (with default parameter)
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

	public void log() {
		switch (templateSelection) {
		case SPECIFIC_VALUE: {
			String tmp_str;
			if (single_value.isNative()) {
				tmp_str = Integer.toString(single_value.getInt());
			} else {
				tmp_str = single_value.getBigInteger().toString();
			}
			TtcnLogger.log_event("%s", tmp_str);
			break;
			}
		case COMPLEMENTED_LIST:
			TtcnLogger.log_event_str("complement");
		case VALUE_LIST:
			TtcnLogger.log_char('(');
			for (int i = 0; i < value_list.size(); i++) {
				if (i > 0) {
					TtcnLogger.log_event_str(", ");
				}
				value_list.get(i).log();
			}
			TtcnLogger.log_char(')');
			break;
		case VALUE_RANGE:
			TtcnLogger.log_char('(');
			if (min_is_exclusive) {
				TtcnLogger.log_char('!');
			}
			if (min_is_present) {
				if (min_value.isNative()) {
					TtcnLogger.log_event("%s", Integer.toString(min_value.getInt()));
				} else {
					TtcnLogger.log_event("%s", min_value.getBigInteger().toString());
				}
			} else {
				TtcnLogger.log_event_str("-infinity");
			}
			TtcnLogger.log_event_str(" .. ");

			if (max_is_exclusive) {
				TtcnLogger.log_char('!');
			}
			if (max_is_present) {
				if (max_value.isNative()) {
					TtcnLogger.log_event("%s", Integer.toString(max_value.getInt()));
				} else {
					TtcnLogger.log_event("%s", max_value.getBigInteger().toString());
				}
			} else {
				TtcnLogger.log_event_str("infinity");
			}

			TtcnLogger.log_char(')');
			break;
		default:
			log_generic();
			break;
		}
		log_ifpresent();
	}
}
