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

package eu.esdihumboldt.hale.gmlwriter.impl.internal.geometry.writers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.opengis.feature.type.Name;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import de.cs3d.util.logging.ALogger;
import de.cs3d.util.logging.ALoggerFactory;
import eu.esdihumboldt.hale.gmlwriter.impl.internal.geometry.DefinitionPath;
import eu.esdihumboldt.hale.gmlwriter.impl.internal.geometry.GeometryWriter;
import eu.esdihumboldt.hale.schemaprovider.model.TypeDefinition;

/**
 * Abstract geometry writer implementation
 *
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id$ 
 * @param <T> the geometry type
 */
public abstract class AbstractGeometryWriter<T extends Geometry> implements GeometryWriter<T> {
	
	private static final ALogger log = ALoggerFactory.getLogger(AbstractGeometryWriter.class);

	private final Class<T> geometryType;
	
	private final Set<Name> compatibleTypes = new HashSet<Name>();
	
	private final Set<Pattern> patterns = new HashSet<Pattern>();
	
	/**
	 * Constructor
	 * 
	 * @param geometryType the geometry type
	 */
	public AbstractGeometryWriter(Class<T> geometryType) {
		super();
		this.geometryType = geometryType;
	}

	/**
	 * @see GeometryWriter#getCompatibleTypes()
	 */
	@Override
	public Set<Name> getCompatibleTypes() {
		return Collections.unmodifiableSet(compatibleTypes);
	}
	
	/**
	 * Add a compatible type. A <code>null</code> namespace references the GML
	 * namespace.
	 * 
	 * @param typeName the type name
	 */
	public void addCompatibleType(Name typeName) {
		compatibleTypes.add(typeName);
	}

	/**
	 * @see GeometryWriter#getGeometryType()
	 */
	@Override
	public Class<T> getGeometryType() {
		return geometryType;
	}
	
	/**
	 * Add an encoding pattern.
	 * 
	 * @param pattern the pattern string
	 * @see Pattern#parse(String)
	 */
	public void addPattern(String pattern) {
		Pattern p = Pattern.parse(pattern);
		if (p.isValid()) {
			patterns.add(p);
		}
		else {
			log.warn("Ignoring invalid pattern: " + pattern);
		}
	}

	/**
	 * @see GeometryWriter#match(TypeDefinition, DefinitionPath, String)
	 */
	@Override
	public DefinitionPath match(TypeDefinition type, DefinitionPath basePath,
			String gmlNs) {
		// try to match each pattern
		for (Pattern pattern : patterns) {
			DefinitionPath path = pattern.match(type, basePath, gmlNs);
			if (path != null) {
				return path;
			}
		}
		
		return null;
	}
	
	/**
	 * Write coordinates into a posList or coordinates property
	 * 
	 * @param coordinates the coordinates to write
	 * @param elementType the type of the encompassing element
	 * @param gmlNs the GML namespace
	 */
	protected static void writeCoordinates(Coordinate[] coordinates,
			TypeDefinition elementType, String gmlNs) {
		// TODO Auto-generated method stub
		System.out.println("Writing coordinates not yet implemented");
	}

}
