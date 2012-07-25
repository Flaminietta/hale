/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2011.
 */

package eu.esdihumboldt.hale.ui.views.data.internal.explore;

import java.util.Collection;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import eu.esdihumboldt.hale.common.core.report.Message;
import eu.esdihumboldt.hale.common.instance.model.Group;
import eu.esdihumboldt.hale.common.instance.model.Instance;
import eu.esdihumboldt.hale.common.instancevalidator.InstanceValidator;
import eu.esdihumboldt.hale.common.instancevalidator.report.InstanceValidationMessage;
import eu.esdihumboldt.hale.common.instancevalidator.report.InstanceValidationReport;
import eu.esdihumboldt.hale.common.schema.model.ChildDefinition;
import eu.esdihumboldt.hale.common.schema.model.Definition;
import eu.esdihumboldt.hale.common.schema.model.TypeDefinition;
import eu.esdihumboldt.util.Pair;

/**
 * Label provider for instances values in a tree based on a 
 * {@link InstanceContentProvider}.
 * @author Simon Templer
 */
public class InstanceValueLabelProvider extends StyledCellLabelProvider {
	
	private static final int MAX_STRING_LENGTH = 200;

	/**
	 * @see CellLabelProvider#update(ViewerCell)
	 */
	@Override
	public void update(ViewerCell cell) {
		TreePath treePath = cell.getViewerRow().getTreePath();
		Object element = treePath.getLastSegment();
		Definition<?> definition = null;

		Object value = ((Pair<?, ?>) element).getSecond();
		if(((Pair<?, ?>) element).getFirst() instanceof Definition){
		definition = (Definition<?>) ((Pair<?, ?>) element).getFirst();
		}
		

		InstanceValidationReport report = null;
		if (definition instanceof ChildDefinition<?>)
			report = InstanceValidator.validate(value, (ChildDefinition<?>) ((Pair<?, ?>) element).getFirst());
		else if (definition instanceof TypeDefinition)
			report = InstanceValidator.validate((Instance) value);

		boolean hasValue = false;
		if (value instanceof Instance) {
			hasValue = ((Instance) value).getValue() != null;
		}
		
		StyledString styledString;
		if (value == null) {
			styledString = new StyledString("no value", StyledString.DECORATIONS_STYLER);
		}
		else if (value instanceof Group && !hasValue) {
			styledString = new StyledString("+", StyledString.QUALIFIER_STYLER);
		}
		else {
			if (value instanceof Instance) {
				value = ((Instance) value).getValue();
			}
			//TODO some kind of conversion?
			String stringValue = value.toString();
			/*
			 * Values that are very large, e.g. string representations of very
			 * complex geometries lead to StyledCellLabelProvider.updateTextLayout
			 * taking a very long time, rendering the application unresponsive
			 * when the data views are displayed.
			 * As such, we reduce the string to a maximum size.
			 */
			if (stringValue.length() > MAX_STRING_LENGTH) {
				stringValue = stringValue.substring(0, MAX_STRING_LENGTH) + "...";
			}
			
			styledString = new StyledString(stringValue, null);
		}
		
		cell.setText(styledString.toString());
		cell.setStyleRanges(styledString.getStyleRanges());

		if (report != null && !report.getWarnings().isEmpty())
			cell.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK));
		
		super.update(cell);
	}

	/**
	 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
		InstanceValidationReport report;

		Object value = ((Pair<?, ?>) element).getSecond();
		Definition<?> definition = null;
		if(((Pair<?, ?>) element).getFirst() instanceof Definition){
			definition = (Definition<?>) ((Pair<?, ?>) element).getFirst();
		}

		if (definition instanceof ChildDefinition<?>)
			report = InstanceValidator.validate(value, (ChildDefinition<?>) ((Pair<?, ?>) element).getFirst());
		else if (definition instanceof TypeDefinition)
			report = InstanceValidator.validate((Instance) value);
		else
			return null;

		Collection<InstanceValidationMessage> warnings = report.getWarnings();

		if (warnings.isEmpty())
			return null;

		StringBuilder toolTip = new StringBuilder();
		for (Message m : warnings)
			toolTip.append(m.getFormattedMessage()).append('\n');

		return toolTip.substring(0, toolTip.length() - 1);
	}
	
}
