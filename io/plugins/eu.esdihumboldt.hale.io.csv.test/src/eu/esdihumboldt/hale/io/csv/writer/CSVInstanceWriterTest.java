/*
 * Copyright (c) 2014 Data Harmonisation Panel
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Data Harmonisation Panel <http://www.dhpanel.eu>
 */

package eu.esdihumboldt.hale.io.csv.writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.content.IContentType;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import au.com.bytecode.opencsv.CSVReader;
import eu.esdihumboldt.cst.test.TransformationExample;
import eu.esdihumboldt.cst.test.TransformationExamples;
import eu.esdihumboldt.hale.common.core.HalePlatform;
import eu.esdihumboldt.hale.common.core.io.Value;
import eu.esdihumboldt.hale.common.core.io.report.IOReport;
import eu.esdihumboldt.hale.common.core.io.supplier.FileIOSupplier;
import eu.esdihumboldt.hale.common.instance.io.InstanceWriter;
import eu.esdihumboldt.hale.common.instance.model.InstanceCollection;
import eu.esdihumboldt.hale.common.schema.model.Schema;
import eu.esdihumboldt.hale.common.test.TestUtil;
import eu.esdihumboldt.hale.io.csv.InstanceTableIOConstants;
import eu.esdihumboldt.hale.io.csv.reader.internal.CSVSchemaReader;
import eu.esdihumboldt.hale.io.csv.writer.internal.CSVInstanceWriter;

/**
 * Test class for {@link CSVInstanceWriter}
 * 
 * @author Yasmina Kammeyer
 */
public class CSVInstanceWriterTest {

	/**
	 * a temporary folder to safely store tmp files. Will be deleted after test
	 * (successfully or not)
	 */
	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	/**
	 * Wait for needed services to be running
	 */
	@BeforeClass
	public static void waitForServices() {
		TestUtil.startConversionService();
	}

	/**
	 * Test - write simple data, without nested properties and useSchema=false
	 * 
	 * @throws Exception , if an error occurs
	 */
	@Test
	public void testWriteSimpleSchema() throws Exception {

		TransformationExample example = TransformationExamples
				.getExample(TransformationExamples.SIMPLE_ASSIGN);
		// alternative the data could be generated by iterating through the
		// exemple project's source data:
		// eu.esdihumboldt.cst.test/src/testdata/simpleassign/instance1.xml
		String propertyNames = "a1,b1,c1,id";
		String firstDataRow = "a10,b10,c10,id0";
		// header size
		int numberOfColumns = 4; // in the example t1.xsd: elements + attribute
		int numberOfRows = 3;
		char sep = ',';

		File tmpFile = tmpFolder.newFile("csvTestWriteSimpleSchema.csv");

		assertTrue("Csv Export was not successful.", writeCsvToFile(tmpFile, true, false,
				Value.of(sep), null, null, example.getSourceInstances()));

		CSVReader reader = new CSVReader(new FileReader(tmpFile), sep);
		List<String[]> rows = reader.readAll();

		reader.close();

		assertEquals("Not enough rows.", numberOfRows, rows.size());

		// Check no. of columns ###
		int countCols = rows.get(0).length;
		assertEquals("Not enough columns written in the output file.", numberOfColumns, countCols);

		// Check header ###
		Iterator<String[]> row = rows.iterator();

		String[] header = row.next();
		assertEquals("There are not enough entries.", numberOfColumns, header.length);

		for (int i = 0; i < header.length; i++) {
			assertTrue("The header of the csv file does not contain all properties.",
					propertyNames.contains(header[i]));
		}

		String[] dataRow = row.next();
		for (int i = 0; i < dataRow.length; i++) {
			assertTrue("The first data row of the csv file does not contain all properties.",
					firstDataRow.contains(dataRow[i]));
		}
	}

	/**
	 * Test - write simple data, without nested properties and useSchema=true -
	 * test if order/number of column persisted from original schema when in the
	 * instance an attribute has no values
	 * 
	 * @throws Exception , if an error occurs
	 */
	@Test
	public void testWriteSimpleSchemaColOrder() throws Exception {

		// create an example schema with 3 properties and an instance with two
		// missing values
		Schema schema = CSVInstanceWriterTestUtil.createExampleSchema();
		InstanceCollection instance = CSVInstanceWriterTestUtil
				.createExampleInstancesNoPopulation(schema);

		String propertyNames = "name,population,country";
		String firstDataRow = "Darmstadt,,";

		// header size
		int numberOfColumns = 3; // counting only the properties
									// ('name,population,country'),
									// not the type ('city')
		int numberOfRows = 3; // counting also the header
		char sep = ',';

		File tmpFile = tmpFolder.newFile("csvTestWriteSimpleSchema.csv");

		assertTrue("Csv Export was not successful.",
				writeCsvToFile(tmpFile, true, true, Value.of(sep), null, null, instance));

		CSVReader reader = new CSVReader(new FileReader(tmpFile), sep);
		List<String[]> rows = reader.readAll();

		reader.close();

		assertEquals("Not enough rows.", numberOfRows, rows.size());

		// Check no. of columns
		int countCols = rows.get(0).length;
		assertEquals("Not enough columns written in the output file.", numberOfColumns, countCols);
		// Check the order of the columns
		String[] originalColumns = propertyNames.split(",");
		int j = 0;
		for (String s : originalColumns) {
			assertEquals("The order of the columns is not equal to the original source file", s,
					rows.get(0)[j]);
			j++;
		}
		// Check header
		Iterator<String[]> row = rows.iterator();

		String[] header = row.next();
		assertEquals("There are not enough entries.", numberOfColumns, header.length);

		for (int i = 0; i < header.length; i++) {
			assertTrue("The header of the csv file does not contain all properties.",
					propertyNames.contains(header[i]));
		}

		String[] dataRow = row.next();
		for (int i = 0; i < dataRow.length; i++) {
			assertTrue("The first data row of the csv file does not contain all properties.",
					firstDataRow.contains(dataRow[i]));
		}
	}

	/**
	 * Test - write simple data, without nested properties
	 * 
	 * @throws Exception , if an error occurs
	 */
	@Test
	public void testWriteSimpleSchemaDelimiter() throws Exception {

		TransformationExample example = TransformationExamples
				.getExample(TransformationExamples.SIMPLE_ASSIGN);

		// alternative the data could be generated by iterating through the
		// exempleproject's source data
		String propertyNames = "id,a1,b1,c1";
		String firstDataRow = "id0,a10,b10,c10";
		// header size
		int numberOfEntries = 4;
		int numberOfRows = 3;
		char sep = '\t';
		char quo = '\'';
		char esc = '"';

		File tmpFile = tmpFolder.newFile("csvTestWriteSimpleSchemaDelimiter.csv");

		assertTrue("Csv Export was not successful.", writeCsvToFile(tmpFile, true, false,
				Value.of(sep), Value.of(quo), Value.of(esc), example.getSourceInstances()));

		CSVReader reader = new CSVReader(new FileReader(tmpFile), sep, quo, esc);
		List<String[]> rows = reader.readAll();
		//
		reader.close();

		assertEquals("Not enough rows.", numberOfRows, rows.size());

		// Check header ###
		Iterator<String[]> row = rows.iterator();

		String[] header = row.next();
		assertEquals("There are not enough entries.", numberOfEntries, header.length);

		for (int i = 0; i < header.length; i++) {
			assertTrue("The header of the csv file do not contain all properties.",
					propertyNames.contains(header[i]));
		}

		String[] dataRow = row.next();
		for (int i = 0; i < dataRow.length; i++) {
			assertTrue(
					"The first data row of the csv file do not contain all properties. Miss on : "
							+ dataRow[i],
					firstDataRow.contains(dataRow[i]));
		}
	}

	/**
	 * Test - write data of complex schema and analyze result
	 * 
	 * @throws Exception , if an error occurs
	 */
	@Test
	public void testWriteComplexSchema() throws Exception {

		TransformationExample example = TransformationExamples
				.getExample(TransformationExamples.SIMPLE_COMPLEX);
		// alternative the data could be generated by iterating through the
		// exempleproject's source data
		String propertyNames = "id,name,details.age,details.income,details.address.street,details.address.city";
		String firstDataRow = "id0,name0,age0,income0,street0,city0,street1,city1";
		// String secondDataRow =
		// "id1,name1,age1,income1,street2,city2,street3,city3";
		int numberOfEntries = 6;
		int numberOfRows = 3;
		char sep = ',';

		File tmpFile = tmpFolder.newFile("csvTestWriteComplexSchema.csv");

		assertTrue("Csv Export was not successful.", writeCsvToFile(tmpFile, true, false,
				Value.of(sep), null, null, example.getSourceInstances()));

		CSVReader reader = new CSVReader(new FileReader(tmpFile), sep);
		List<String[]> rows = reader.readAll();
		//
		reader.close();

		assertEquals("Not enough rows.", numberOfRows, rows.size());

		// Check header ###
		Iterator<String[]> row = rows.iterator();

		String[] header = row.next();

		assertEquals("There are not enough entries.", numberOfEntries, header.length);

		for (int i = 0; i < header.length; i++) {
			assertTrue("The header of the csv file do not contain all properties.",
					propertyNames.contains(header[i]));
			// This is for debug purposes to check which properties are missing
			// propertyNames = propertyNames.replaceFirst(header[i], "");
		}

		String[] dataRow = row.next();
		for (int i = 0; i < dataRow.length; i++) {
			assertTrue("The first data row of the csv file do not contain all properties.",
					firstDataRow.contains(dataRow[i]));
			// This is for debug purposes ...
			// firstDataRow = firstDataRow.replaceFirst(dataRow[i], "");
		}

	}

	private boolean writeCsvToFile(File tmpFile, boolean solveNestedProperties, boolean useSchema,
			Value sep, Value quo, Value esc, InstanceCollection instances) throws Exception {
		// set instances to xls instance writer
		InstanceWriter writer = new CSVInstanceWriter();
		IContentType contentType = HalePlatform.getContentTypeManager()
				.getContentType("eu.esdihumboldt.hale.io.csv");
		writer.setParameter(InstanceTableIOConstants.USE_SCHEMA, Value.of(useSchema));
		writer.setParameter(InstanceTableIOConstants.SOLVE_NESTED_PROPERTIES,
				Value.of(solveNestedProperties));
		writer.setParameter(CSVSchemaReader.PARAM_SEPARATOR, sep);
		writer.setParameter(CSVSchemaReader.PARAM_QUOTE, quo);
		writer.setParameter(CSVSchemaReader.PARAM_ESCAPE, esc);
		writer.setInstances(instances);
		// write instances to a temporary CSV file
		writer.setTarget(new FileIOSupplier(tmpFile));
		writer.setContentType(contentType);
		IOReport report = writer.execute(null);
		return report.isSuccess();
	}
}
