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

package eu.esdihumboldt.hale.rcp.utils.definition.internal;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import eu.esdihumboldt.hale.rcp.utils.definition.AttributeEditorFactory;
import eu.esdihumboldt.hale.rcp.utils.definition.DefinitionLabelFactory;

/**
 * Service factory for table view related services
 *
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id: ServiceFactory.java 2733 2010-02-01 14:06:12Z stempler $ 
 */
public class ServiceFactory extends AbstractServiceFactory {

	/**
	 * @see AbstractServiceFactory#create(Class, IServiceLocator, IServiceLocator)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object create(Class serviceInterface, IServiceLocator parentLocator,
			IServiceLocator locator) {
		if (serviceInterface.equals(DefinitionLabelFactory.class)) {
			return new DefaultDefinitionLabelFactory();
		}
		else if (serviceInterface.equals(AttributeEditorFactory.class)) {
			return new DefaultAttributeEditorFactory();
		}
		
		return null;
	}

}
