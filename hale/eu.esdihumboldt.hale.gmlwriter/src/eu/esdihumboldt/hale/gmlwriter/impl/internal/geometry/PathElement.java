/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */

package eu.esdihumboldt.hale.gmlwriter.impl.internal.geometry;

import org.opengis.feature.type.Name;

import eu.esdihumboldt.hale.schemaprovider.model.TypeDefinition;

/**
 * 
 *
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$ 
 */
public interface PathElement {

	/**
	 * Get the path element name. This is either a property or a subtype
	 * name
	 * 
	 * @return the element name
	 */
	public abstract Name getName();

	/**
	 * Get the path element type definition
	 * 
	 * @return the path element type definition
	 */
	public abstract TypeDefinition getType();

	/**
	 * Determines if this path element represents a property, otherwise it
	 * represents a sub-type
	 * 
	 * @return if this path element represents a property
	 */
	public abstract boolean isProperty();

}