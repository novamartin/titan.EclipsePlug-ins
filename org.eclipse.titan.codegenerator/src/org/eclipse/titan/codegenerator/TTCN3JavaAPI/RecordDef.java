/******************************************************************************
 * Copyright (c) 2000-2016 Ericsson Telecom AB
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   
 *   Keremi, Andras
 *   Eros, Levente
 *   Kovacs, Gabor
 *
 ******************************************************************************/

package org.eclipse.titan.codegenerator.TTCN3JavaAPI;

import java.util.ArrayList;
import java.util.List;

public abstract class RecordDef extends StructuredTypeDef {
    public List<String> fieldsInOrder; //stores the order of fields of record type
    
    public RecordDef(){
    	fieldsInOrder = new ArrayList<String>();
    }
    
}