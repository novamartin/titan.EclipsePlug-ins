/******************************************************************************
 * Copyright (c) 2000-2017 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.titan.designer.AST;

/**
 * @author Kristof Szabados
 * */
public interface IOutlineElement {

	Identifier getIdentifier();
	String getOutlineText();

	/**
	 * Returns the name of the icon to be used in the outline view for this element.
	 * Must be the name of a gif file located in the icons folder.
	 * */
	String getOutlineIcon();

	/**
	 * @return those objects which should be displayed as the children
	 * of this particular object.
	 * */
	Object[] getOutlineChildren();

	/**
	 * Returns the category of this element. The category is a
	 * number used to allocate elements to bins; the bins are arranged
	 * in ascending numeric order. The elements within a bin are arranged
	 * via a second level sort criterion.
	 * <p>
	 * By default this method should return <code>0</code> if no category can be defined.
	 * Subclasses may reimplement this method to provide non-trivial categorization.
	 * </p>
	 *
	 * @return the category
	 */
	int category();
}
