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
import java.util.List;

/**
 * TTCN-3 universal charstring template
 *
 * @author Arpad Lovassy
 * @author Farkas Izabella Ingrid
 */
public class TitanUniversalCharString_template extends Restricted_Length_Template {

	private TitanUniversalCharString single_value;

	// value_list part
	private ArrayList<TitanUniversalCharString_template> value_list;

	// value range part
	private boolean min_is_set, max_is_set;
	private boolean min_is_exclusive, max_is_exclusive;
	private TitanUniversalChar min_value, max_value;

	//TODO: implement: pattern_value part for STRING_PATTERN case

	public TitanUniversalCharString_template () {
		//do nothing
	}

	public TitanUniversalCharString_template (final template_sel otherValue) {
		super(otherValue);
		checkSingleSelection(otherValue);
	}

	public TitanUniversalCharString_template (final String otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);
	}

	public TitanUniversalCharString_template (final TitanCharString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound charstring value.");

		single_value = new TitanUniversalCharString(otherValue);
	}

	public TitanUniversalCharString_template (final TitanCharString_Element otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound charstring value.");

		single_value = new TitanUniversalCharString(String.valueOf(otherValue.get_char()));
	}

	public TitanUniversalCharString_template (final TitanUniversalCharString otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound universal charstring value.");

		single_value = new TitanUniversalCharString(otherValue);
	}

	public TitanUniversalCharString_template (final TitanUniversalCharString_Element otherValue) {
		super(template_sel.SPECIFIC_VALUE);
		otherValue.mustBound("Creating a template from an unbound universal charstring value.");

		single_value = new TitanUniversalCharString(otherValue);
	}

	public TitanUniversalCharString_template (final TitanUniversalCharString_template otherValue) {
		copyTemplate(otherValue);
	}


	public TitanUniversalCharString_template(final TitanCharString_template otherValue) {
		copyTemplate(otherValue);
	}

	public TitanUniversalCharString_template (final template_sel selValue, final TitanCharString otherValue) {
		if (selValue != template_sel.STRING_PATTERN) {
			throw new TtcnError("Internal error: Initializing a universal charstring pattern template with invalid selection.");
		}
		//TODO implement string pattern 
	}

	public TitanUniversalCharString_template(final template_sel selValue, final TitanCharString otherValue, final boolean nocase) {
		if (selValue != template_sel.STRING_PATTERN) {
			throw new TtcnError("Internal error: Initializing a universal charstring pattern template with invalid selection.");
		}
		//TODO implement string pattern 
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
		case VALUE_RANGE:
			min_value = null;
			max_value = null;
		default:
			break;
		}
		templateSelection = template_sel.UNINITIALIZED_TEMPLATE;
	}

	// originally operator=
	@Override
	public TitanUniversalCharString_template assign(final Base_Type otherValue) {
		if (otherValue instanceof TitanUniversalCharString) {
			return assign((TitanUniversalCharString) otherValue);
		} else if (otherValue instanceof TitanCharString) {
			return assign((TitanCharString) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring", otherValue));
	}

	@Override
	public TitanUniversalCharString_template assign(final Base_Template otherValue) {
		if (otherValue instanceof TitanUniversalCharString_template) {
			return assign((TitanUniversalCharString_template) otherValue);
		} else if (otherValue instanceof TitanCharString_template) {
			return assign((TitanCharString_template) otherValue);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring template", otherValue));
	}

	//originally operator=
	public TitanUniversalCharString_template assign( final template_sel otherValue ) {
		checkSingleSelection(otherValue);
		cleanUp();
		setSelection(otherValue);

		return this;
	}

	//originally operator=
	public TitanUniversalCharString_template assign( final String otherValue ) {
		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	//originally operator=
	public TitanUniversalCharString_template assign( final TitanUniversalCharString otherValue ) {
		otherValue.mustBound("Assignment of an unbound universal charstring value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	public TitanUniversalCharString_template assign(final TitanUniversalCharString_Element otherValue) {
		otherValue.mustBound("Assignment of an unbound universal charstring element to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	public TitanUniversalCharString_template assign(final TitanCharString otherValue) {
		otherValue.mustBound("Assignment of an unbound charstring value to a template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(otherValue);

		return this;
	}

	public TitanUniversalCharString_template assign(final TitanCharString_Element otherValue) {
		otherValue.mustBound("Assignment of an unbound charstring element to a universal charstring template.");

		cleanUp();
		setSelection(template_sel.SPECIFIC_VALUE);
		single_value = new TitanUniversalCharString(String.valueOf(otherValue.get_char()));

		return this;
	}

	public TitanUniversalCharString_template assign(final TitanCharString_template otherValue) {
		cleanUp();
		copyTemplate(otherValue);

		return this;
	}

	//originally operator=
	public TitanUniversalCharString_template assign( final TitanUniversalCharString_template otherValue ) {
		if (otherValue != this) {
			cleanUp();
			copyTemplate(otherValue);
		}

		return this;
	}


	private void copyTemplate(final TitanCharString_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanUniversalCharString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanUniversalCharString_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanUniversalCharString_template temp = new TitanUniversalCharString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			if (!otherValue.min_is_set) {
				throw new TtcnError("The lower bound is not set when copying a charstring value range template to a universal charstring template.");
			}
			if (!otherValue.max_is_set) {
				throw new TtcnError("The upper bound is not set when copying a charstring value range template to a universal charstring template.");
			}
			min_is_set = true;
			max_is_set = true;
			min_is_exclusive = otherValue.min_is_exclusive;
			max_is_exclusive = otherValue.max_is_exclusive;
			min_value = new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.min_value.getAt(0).get_char());
			max_value =  new TitanUniversalChar((char) 0, (char) 0, (char) 0, otherValue.max_value.getAt(0).get_char());
			break;
		case STRING_PATTERN:
			// TODO: implement
			break;
		case DECODE_MATCH:

			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported charstring template to a universal charstring template.");
		}

		setSelection(otherValue);
	}

	private void copyTemplate(final TitanUniversalCharString_template otherValue) {
		switch (otherValue.templateSelection) {
		case SPECIFIC_VALUE:
			single_value = new TitanUniversalCharString(otherValue.single_value);
			break;
		case OMIT_VALUE:
		case ANY_VALUE:
		case ANY_OR_OMIT:
			break;
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			value_list = new ArrayList<TitanUniversalCharString_template>(otherValue.value_list.size());
			for(int i = 0; i < otherValue.value_list.size(); i++) {
				final TitanUniversalCharString_template temp = new TitanUniversalCharString_template(otherValue.value_list.get(i));
				value_list.add(temp);
			}
			break;
		case VALUE_RANGE:
			min_is_set = otherValue.min_is_set;
			min_is_exclusive = otherValue.min_is_exclusive;
			if(min_is_set) {
				min_value = new TitanUniversalChar(otherValue.min_value);
			}
			max_is_set = otherValue.max_is_set;
			max_is_exclusive = otherValue.max_is_exclusive;
			if(max_is_set) {
				max_value = new TitanUniversalChar(otherValue.max_value);
			}
			break;
		default:
			throw new TtcnError("Copying an uninitialized/unsupported universal charstring template.");
		}

		setSelection(otherValue);
	}

	// originally operator[](int index_value)
	public TitanUniversalCharString_Element getAt(final int index) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a universal charstring element of a non-specific universal charstring template.");
		}

		return single_value.getAt(index);
	}

	// originally operator[](const INTEGER& index_value)
	public TitanUniversalCharString_Element getAt(final TitanInteger index) {
		index.mustBound("Indexing a universal charstring template with an unbound integer value.");

		return getAt(index.getInt());
	}

	public TitanUniversalCharString_Element constGetAt(final int index) {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Accessing a universal charstring element of a non-specific universal charstring template.");
		}

		return single_value.constGetAt(index);
	}

	// originally operator[](const INTEGER& index_value)
	public TitanUniversalCharString_Element constGetAt(final TitanInteger index) {
		index.mustBound("Indexing a universal charstring template with an unbound integer value.");

		return constGetAt(index.getInt());
	}

	@Override
	public TitanBoolean match(final Base_Type otherValue, final boolean legacy) {
		if (otherValue instanceof TitanUniversalCharString) {
			return match((TitanUniversalCharString) otherValue, legacy);
		} else if (otherValue instanceof TitanCharString) {
			return match(new TitanUniversalCharString((TitanCharString) otherValue), legacy);
		}

		throw new TtcnError(MessageFormat.format("Internal Error: value `{0}'' can not be cast to universal charstring", otherValue));
	}

	// originally match
	public TitanBoolean match(final TitanUniversalCharString otherValue) {
		return match(otherValue, false);
	}

	// originally match
	public TitanBoolean match(final TitanUniversalCharString otherValue, final boolean legacy) {
		if(! otherValue.isBound().getValue()) {
			return new TitanBoolean(false);
		}

		final List<TitanUniversalChar> otherStr = otherValue.getValue();
		final int otherLen = otherStr.size();
		if (!match_length( otherValue.lengthOf().getInt())) {
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
			if (!min_is_set) {
				throw new TtcnError("The lower bound is not set when " +
						"matching with a universal charstring value range template.");
			}
			if (!max_is_set) {
				throw new TtcnError("The upper bound is not set when " +
						"matching with a universal charstring value range template.");
			}
			for (int i = 0; i < otherLen; i++) {
				final TitanUniversalChar uc = otherStr.get( i );
				if ( uc.lessThan( min_value ).getValue() || max_value.lessThan( uc ).getValue() ) {
					return new TitanBoolean(false);
				} else if ( ( min_is_exclusive && uc.operatorEquals( min_value ).getValue() ) || ( max_is_exclusive && uc.operatorEquals( max_value ).getValue() ) ) {
					return new TitanBoolean(false);
				}
			}
			return new TitanBoolean(true);
		}
		case STRING_PATTERN:{
			//TODO: implement
		}
		default:
			throw new TtcnError("Matching with an uninitialized/unsupported universal charstring template.");
		}
	}

	public TitanInteger lengthOf() {
		if (is_ifPresent) {
			throw new TtcnError("Performing lengthof() operation on a universal charstring template which has an ifpresent attribute.");
		}

		int min_length;
		boolean has_any_or_none;
		switch (templateSelection) {
		case SPECIFIC_VALUE:
			min_length = single_value.lengthOf().getInt();
			has_any_or_none = false;
			break;
		case OMIT_VALUE:
			throw new TtcnError("Performing lengthof() operation on a universal charstring template containing omit value.");
		case ANY_VALUE:
		case ANY_OR_OMIT:
		case VALUE_RANGE:
			min_length = 0;
			has_any_or_none = true; // max. length is infinity
			break;
		case VALUE_LIST: {
			// error if any element does not have length or the
			// lengths differ
			if (value_list.isEmpty()) {
				throw new TtcnError("Internal error: Performing lengthof() operation on a universal charstring template containing an empty list.");
			}
			final int item_length = value_list.get(0).lengthOf().getInt();
			for (int i = 1; i < value_list.size(); ++i) {
				if (value_list.get(i).lengthOf().getInt() != item_length) {
					throw new TtcnError("Performing lengthof() operation on a universal charstring template containing a value list with different lengths.");
				}
			}
			min_length = item_length;
			has_any_or_none = false;
			break;
		}
		case COMPLEMENTED_LIST:
			throw new TtcnError("Performing lengthof() operation on a universal charstring template containing complemented list.");
		case STRING_PATTERN:
			throw new TtcnError("Performing lengthof() operation on a universal charstring template containing a pattern is not allowed.");
		default:
			throw new TtcnError("Performing lengthof() operation on an uninitialized/unsupported universal charstring template.");
		}

		return new TitanInteger(check_section_is_single(min_length, has_any_or_none, "length", "a", "universal charstring template"));
	}

	public TitanUniversalCharString valueOf() {
		if (templateSelection != template_sel.SPECIFIC_VALUE || is_ifPresent) {
			throw new TtcnError("Performing a valueof or send operation on a non-specific universal charstring template.");
		}

		return single_value;
	}

	public void setType(final template_sel otherValue){
		setType(otherValue, 0);
	}

	public void setType(final template_sel otherValue, final int lenght) {
		cleanUp();
		switch (otherValue) {
		case VALUE_LIST:
		case COMPLEMENTED_LIST:
			setSelection(otherValue);
			value_list = new ArrayList<TitanUniversalCharString_template>(lenght);
			for (int i = 0; i < lenght; ++i) {
				value_list.add(new TitanUniversalCharString_template());
			}
			break;
		case VALUE_RANGE:
			setSelection(template_sel.VALUE_RANGE);
			min_is_set = false;
			max_is_set = false;
			min_is_exclusive = false;
			max_is_exclusive = false;
			break;
		case DECODE_MATCH:
			setSelection(template_sel.DECODE_MATCH);
			break;
		default:
			throw new TtcnError("Setting an invalid type for a universal charstring template.");
		}
	}

	public TitanUniversalCharString_template listItem(final int listIndex) {
		if (templateSelection != template_sel.VALUE_LIST &&
				templateSelection != template_sel.COMPLEMENTED_LIST) {
			throw new TtcnError("Accessing a list element of a non-list universal charstring template.");
		}
		if (listIndex < 0) {
			throw new TtcnError("Accessing an universal charstring value list template using a negative index (" + listIndex + ").");
		}
		if (listIndex >= value_list.size()) {
			throw new TtcnError("Index overflow in a universal charstring value list template.");
		}

		return value_list.get(listIndex);
	}

	public void setMin(final TitanUniversalCharString minValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range universal charstring template.");
		}

		minValue.mustBound("Setting an unbound value as lower bound in a universal charstring value range template.");
		final int length = minValue.lengthOf().getInt();
		if (length != 1) {
			throw new TtcnError("The length of the lower bound in a universal charstring value range template must be 1 instead of " + length);
		}

		min_is_set = true;
		min_is_exclusive = false;
		min_value = minValue.getAt(0).get_char();

		if (max_is_set && max_value.lessThan(min_value).getValue()) {
			throw new TtcnError("The lower bound in a universal charstring value range template is greater than the upper bound.");
		}
	}

	public void setMax(final TitanUniversalCharString maxValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound for a non-range universal charstring template.");
		}

		maxValue.mustBound("Setting an unbound value as upper bound in a universal charstring value range template.");
		final int length = maxValue.lengthOf().getInt();
		if (length != 1) {
			throw new TtcnError("The length of the upper bound in a universal charstring value range template must be 1 instead of " + length);
		}

		max_is_set = true;
		max_is_exclusive = false;
		max_value = maxValue.getAt(0).get_char();

		if (min_is_set && max_value.lessThan(min_value).getValue()) {
			throw new TtcnError("The upper bound in a universal charstring value range template is smaller than the lower bound.");
		}
	}

	public void setMin(final TitanCharString minValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound for a non-range universal charstring template.");
		}

		minValue.mustBound("Setting an unbound value as lower bound in a universal charstring value range template.");
		final int length = minValue.lengthOf().getInt();
		if (length != 1) {
			throw new TtcnError("The length of the lower bound in a universal charstring value range template must be 1 instead of " + length);
		}

		min_is_set = true;
		min_is_exclusive = false;
		min_value = new TitanUniversalChar( (char) 0, (char) 0, (char) 0, minValue.getAt(0).get_char());

		if (max_is_set && max_value.lessThan(min_value).getValue()) {
			throw new TtcnError("The lower bound in a universal charstring value range template is greater than the upper bound.");
		}
	}

	public void setMax(final TitanCharString maxValue) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound for a non-range universal charstring template.");
		}

		maxValue.mustBound("Setting an unbound value as upper bound in a universal charstring value range template.");
		final int length = maxValue.lengthOf().getInt();
		if (length != 1) {
			throw new TtcnError("The length of the upper bound in a universal charstring value range template must be 1 instead of " + length);
		}

		max_is_set = true;
		max_is_exclusive = false;
		max_value = new TitanUniversalChar( (char) 0, (char) 0, (char) 0, maxValue.getAt(0).get_char());

		if (min_is_set && max_value.lessThan(min_value).getValue()) {
			throw new TtcnError("The upper bound in a universal charstring value range template is smaller than the lower bound.");
		}
	}

	public void setMinExclusive(final boolean minExclusive) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the lower bound exclusiveness for a non-range universal charstring template.");
		}

		min_is_exclusive = minExclusive;
	}

	public void setMaxExclusive(final boolean maxExclusive) {
		if (templateSelection != template_sel.VALUE_RANGE) {
			throw new TtcnError("Setting the upper bound exclusiveness for a non-range universal charstring template.");
		}

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
