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

package eu.esdihumboldt.hale.io.gml.writer.internal.geometry;

import java.util.ArrayList;
import java.util.List;

import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import eu.esdihumboldt.hale.schemaprovider.model.AttributeDefinition;
import eu.esdihumboldt.hale.schemaprovider.model.SchemaElement;
import eu.esdihumboldt.hale.schemaprovider.model.TypeDefinition;

/**
 * Represents a path in a type definition hierarchy (regarding subtypes 
 * and properties)
 *
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$ 
 */
public class DefinitionPath {
	
	/**
	 * Downcast path element
	 */
	private static class DowncastElement implements PathElement {

		private final Name elementName;
		
		private final TypeDefinition type;
		
		/**
		 * Constructor
		 * 
		 * @param elementName the name of the element the downcast is applied to
		 * @param type the definition of the type that is downcast to
		 */
		public DowncastElement(Name elementName, TypeDefinition type) {
			super();
			this.elementName = elementName;
			this.type = type;
		}

		/**
		 * @see PathElement#getName()
		 */
		@Override
		public Name getName() {
			return elementName;
		}

		/**
		 * @see PathElement#getType()
		 */
		@Override
		public TypeDefinition getType() {
			return type;
		}

		/**
		 * @see PathElement#isProperty()
		 */
		@Override
		public boolean isProperty() {
			return false;
		}

		/**
		 * @see PathElement#isDowncast()
		 */
		@Override
		public boolean isDowncast() {
			return true;
		}

	}
	
	/**
	 * Sub-type path element
	 */
	private static class SubstitutionElement implements PathElement {

		private final SchemaElement element;
		
		/**
		 * Constructor
		 * 
		 * @param element the substitution element
		 */
		public SubstitutionElement(SchemaElement element) {
			this.element = element;
		}

		/**
		 * @see PathElement#getName()
		 */
		@Override
		public Name getName() {
			return element.getElementName();
		}

		/**
		 * @see PathElement#getType()
		 */
		@Override
		public TypeDefinition getType() {
			return element.getType();
		}

		/**
		 * @see PathElement#isProperty()
		 */
		@Override
		public boolean isProperty() {
			return false;
		}

		/**
		 * @see PathElement#isDowncast()
		 */
		@Override
		public boolean isDowncast() {
			return false;
		}

	}

	/**
	 * A property path element 
	 */
	private static class PropertyElement implements PathElement {
		
		private final AttributeDefinition attdef;

		/**
		 * Constructor
		 * 
		 * @param attdef the attribute definition
		 */
		public PropertyElement(AttributeDefinition attdef) {
			this.attdef = attdef;
		}

		/**
		 * @see PathElement#getName()
		 */
		public Name getName() {
			return new NameImpl(attdef.getNamespace(), attdef.getName());
		}
		
		/**
		 * @see PathElement#getType()
		 */
		public TypeDefinition getType() {
			return attdef.getAttributeType();
		}
		
		/**
		 * @see PathElement#isProperty()
		 */
		public boolean isProperty() {
			return true;
		}

		/**
		 * @see PathElement#isDowncast()
		 */
		@Override
		public boolean isDowncast() {
			return false;
		}

	}

	private final List<PathElement> steps = new ArrayList<PathElement>();
	
	private TypeDefinition lastType;
	
	private Name lastName;
	
	private GeometryWriter<?> geometryWriter;
	
	/**
	 * Create a definition path beginning with the given base path
	 * 
	 * @param basePath the base path
	 */
	public DefinitionPath(DefinitionPath basePath) {
		this(basePath.lastType, basePath.lastName);
		
		steps.addAll(basePath.getSteps());
	}

	/**
	 * Create an empty definition path
	 * 
	 * @param firstType the type starting the path 
	 * @param elementName the corresponding element name
	 */
	public DefinitionPath(TypeDefinition firstType, Name elementName) {
		super();
		
		lastType = firstType;
		lastName = elementName;
	}

	/**
	 * Add a substitution
	 * 
	 * @param element the substitution element
	 * 
	 * @return this path for chaining 
	 */
	public DefinitionPath addSubstitution(SchemaElement element) {
		// 1. sub-type must override previous sub-type
		// 2. sub-type must override a previous property XXX check this!!! or only the first?
		// XXX -> therefore removing the previous path element
		if (steps.size() > 0) {
			steps.remove(steps.size() - 1);
		}
		
		addStep(new SubstitutionElement(element));
		
		return this;
	}
	
	/**
	 * Add a downcast
	 * 
	 * @param subtype the definition of the sub-type that is to be cast to
	 * @return this path for chaining
	 */
	public DefinitionPath addDowncast(TypeDefinition subtype) {
		// 1. sub-type must override previous sub-type
		// 2. sub-type must override a previous property XXX check this!!! or only the first?
		// XXX -> therefore removing the previous path element
		Name elementName = getLastName();
		
		if (steps.size() > 0) {
			steps.remove(steps.size() - 1);
		}
		
		addStep(new DowncastElement(elementName, subtype));
		
		return this;
	}

	
	private void addStep(PathElement step) {
		steps.add(step);
		lastType = step.getType();
		lastName = step.getName();
	}

	/**
	 * Add a property
	 * 
	 * @param property the property definition
	 * 
	 * @return this path for chaining 
	 */
	public DefinitionPath addProperty(AttributeDefinition property) {
		addStep(new PropertyElement(property));
		
		return this;
	}

	/**
	 * @return the geometryWriter
	 */
	public GeometryWriter<?> getGeometryWriter() {
		return geometryWriter;
	}

	/**
	 * @param geometryWriter the geometryWriter to set
	 */
	public void setGeometryWriter(GeometryWriter<?> geometryWriter) {
		this.geometryWriter = geometryWriter;
	}

	/**
	 * @return the steps
	 */
	public List<PathElement> getSteps() {
		return steps;
	}
	
	/**
	 * Determines if the path is empty
	 * 
	 * @return if the path is empty
	 */
	public boolean isEmpty() {
		return steps.isEmpty();
	}

	/**
	 * Get the last type of the path. For empty paths this will be the type
	 * specified on creation
	 * 
	 * @return the last type
	 */
	public TypeDefinition getLastType() {
		return lastType;
	}
	
	/**
	 * Get the last name of the path. For empty paths this will be the name
	 * specified on creation
	 * 
	 * @return the last type
	 */
	public Name getLastName() {
		return lastName;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer result = null;
		for (PathElement step : steps) {
			if (result == null) {
				result = new StringBuffer();
			}
			else {
				result.append(", "); //$NON-NLS-1$
			}
			
			result.append(step.getName());
		}
		
		if (result == null) {
			return "empty"; //$NON-NLS-1$
		}
		else {
			return result.toString();
		}
	}

	/**
	 * Get the last path element
	 * 
	 * @return the last path element or <code>null</code> if it's empty
	 */
	public PathElement getLastElement() {
		if (steps.isEmpty()) {
			return null;
		}
		return steps.get(steps.size() - 1);
	}

}
