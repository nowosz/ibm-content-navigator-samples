package com.ibm.ecm.extension.react;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.ibm.ecm.extension.PluginLogger;
import com.ibm.ecm.extension.PluginResponseFilter;
import com.ibm.ecm.extension.PluginServiceCallbacks;
import com.ibm.json.java.JSONArray;
import com.ibm.json.java.JSONObject;

/**
 * Provides an abstract class that is extended to create a filter for responses
 * from a particular service. The response from the service is provided to the
 * filter in JSON format before it is returned to the web browser. The filter
 * can then modify that response, and the modified response is returned to the
 * web browser.
 */
public class ColumnsDisplayedResponseFilter extends PluginResponseFilter {

	/**
	 * Returns an array of the services that are extended by this filter.
	 * 
	 * @return A <code>String</code> array of names of the services. These are
	 *         the servlet paths or Struts action names.
	 */
	public String[] getFilteredServices() {
		return new String[] { "/p8/openFolder" };
	}

	/**
	 * Filters the response from the service.
	 * 
	 * @param serverType
	 *            A <code>String</code> that indicates the type of server that
	 *            is associated with the service. This value can be one or more
	 *            of the following values separated by commas:
	 *            <table border="1">
	 *            <tr>
	 *            <th>Server Type</th>
	 *            <th>Description</th>
	 *            </tr>
	 *            <tr>
	 *            <td><code>p8</code></td>
	 *            <td>IBM FileNet P8</td>
	 *            </tr>
	 *            <tr>
	 *            <td><code>cm</code></td>
	 *            <td>IBM Content Manager</td>
	 *            </tr>
	 *            <tr>
	 *            <td><code>od</code></td>
	 *            <td>IBM Content Manager OnDemand</td>
	 *            </tr>
	 *         	  <tr>
	 *         		<td><code>cmis</code></td>
	 *         		<td>Content Management Interoperability Services</td>
	 *         	  </tr>
	 *            <tr>
	 *            <td><code>common</code></td>
	 *            <td>For services that are not associated with a particular
	 *            server</td>
	 *            </tr>
	 *            </table>
	 * @param callbacks
	 *            An instance of the
	 *            <code>{@link com.ibm.ecm.extension.PluginServiceCallbacks PluginServiceCallbacks}</code>
	 *            class that contains functions that can be used by the service.
	 *            These functions provide access to plug-in configuration and
	 *            content server APIs.
	 * @param request
	 *            An <code>HttpServletRequest</code> object that provides the
	 *            request. The service can access the invocation parameters from
	 *            the request.
	 * @param jsonResponse
	 *            The <code>JSONObject</code> object that is generated by the
	 *            service. Typically, this object is serialized and sent as the
	 *            response. The filter modifies this object to change the
	 *            response that is sent.
	 * @throws Exception
	 *             For exceptions that occur when the service is running.
	 *             Information about the exception is logged as part of the
	 *             client logging and an error response is automatically
	 *             generated and returned.
	 */
	public void filter(String serverType, PluginServiceCallbacks callbacks, HttpServletRequest request, JSONObject jsonResponse) throws Exception {
		PluginLogger logger = callbacks.getLogger();
		logger.logEntry(this, "filter");
		
		String repositoryId = request.getParameter("repositoryId");
		String folderId = request.getParameter("docid"); 
		String teamspaceId = request.getParameter("workspaceId");
		String filterType = request.getParameter("filter_type");
		if ((teamspaceId == null || teamspaceId.isEmpty()) && (filterType == null || filterType.isEmpty()) && !Util.isRootFolder(callbacks, repositoryId, folderId)) {
			// Retrieve the selected columns chosen by the user.
			String userSettingsKey = "UserColumnSettings" + repositoryId;
			JSONObject savedColumns = Util.getUsersSavedColumns(callbacks, userSettingsKey);
			// Sort savedColumns here for use in sorting the response.
			if (savedColumns != null) {
				JSONArray savedColumnsSortOrder = getSortOrderList(savedColumns, logger); 
				
				// Get columns for details view from jsonResponse.
				JSONObject columns = (JSONObject) jsonResponse.get("columns");
				JSONArray columnCells = (JSONArray) columns.get("cells");
				//all the columns the user can choose to display
				JSONArray allColumnsOptions = (JSONArray) columnCells.get(0);	
				
				
				// Get properties for magazine view from jsonResponse.
				JSONArray magazineViewDefinition = (JSONArray) jsonResponse.get("magazineColumns");
				JSONArray magazineProperties = getFieldsToDisplay(magazineViewDefinition, "fieldsToDisplay");
				JSONArray magazineExtraFieldsToDisplay = getFieldsToDisplay(magazineViewDefinition, "extraFieldsToDisplay");
				
				
				// Make sure we keep necessary columns in Details View. i.e. Mimetype icon, Name, and Size.
				JSONObject necessaryColumns = new JSONObject();
				necessaryColumns.put("multiStateIcon", "multiStateIcon");
				necessaryColumns.put("mimeTypeIcon", "mimeTypeIcon");
				necessaryColumns.put("{NAME}", "{NAME}");
				
				// Store columns/properties that are already defined to be displayed in Browse view within the jsonResponse.
				// These will help identify additional columns, which were passed into the request filter, that we need to add.
				JSONArray detailsColumnsToFilterOut = new JSONArray();
				JSONArray magazinePropertiesToFilterOut = new JSONArray();
				JSONArray magazineExtraFieldsToFilterOut = new JSONArray();
				
				// Add {CLASS} to response if it's included in the users saved settings.
				if (savedColumns.containsKey("{CLASS}")) {
					JSONObject classItem = (JSONObject) savedColumns.get("{CLASS}");
					JSONObject classObject = new JSONObject();
					classObject.put("field","{CLASS}");
					classObject.put("name", "{CLASS}");
					classObject.put("filterable", false);
					classObject.put("sortable", false);
					classObject.put("nosort", true);
					classObject.put("width", "10.4em");
					boolean itemExists = false;
					for (int i = allColumnsOptions.size() - 1; i >= 0; i--) {
						JSONObject column = (JSONObject) allColumnsOptions.get(i);
						if (column.get("field").equals(classItem.get("id"))) {
							itemExists = true;
						}
					}
					if (!itemExists) {
						if ((Boolean) classItem.get("detailsView")) {
							allColumnsOptions.add(classObject);
						}
					}
					itemExists = false;
					for (int i = magazineProperties.size() - 1; i >= 0; i--) {
						JSONObject column = (JSONObject) magazineProperties.get(i);
						if (column.get("field").equals(classItem.get("id"))) {
							itemExists = true;
						}
					}
					if (!itemExists) {
						if ((Boolean) classItem.get("magazineView")) {
							magazineProperties.add(classObject);
						}
					}
				}
				
				
				// Parse the columns that will be showed in Details view filtering with the user's saved settings.
				for(int i = allColumnsOptions.size() - 1; i >= 0; i--) {
					// Get the object that defines this column and save the name in a string to use throughout the loop.
					JSONObject singleColumn = (JSONObject) allColumnsOptions.get(i);
					String columnName = (String) singleColumn.get("field");
					// If this column is one the user has saved, check if it's set to show in Details view.
					if (savedColumns.containsKey(columnName)) {
						
						JSONObject savedColumn = (JSONObject) savedColumns.get(columnName);
						// If the saved user setting indicates NOT to show in Details view, remove this column from the response.
						if (!(Boolean) savedColumn.get("detailsView")) {
							allColumnsOptions.remove(i);
						}
						// Save the column name for use later to use as reference. We know it already exists so won't re-add it later.
						detailsColumnsToFilterOut.add(savedColumn);
					}
					// If the column is not saved in the user settings, not a necessary column, and does not provide any other supporting data, remove it from the response.
					if (!savedColumns.containsKey(columnName) && !necessaryColumns.containsKey(columnName) && columnName != null) {
						allColumnsOptions.remove(i);
					}
				}
				// Parse Magazine view properties using the same logic as the above for loop.
				for(int i = magazineProperties.size() - 1; i >= 0; i--) {
					JSONObject singleProperty = (JSONObject) magazineProperties.get(i);
					String columnName = (String) singleProperty.get("field");
					
					if (savedColumns.containsKey(columnName)) {
						JSONObject savedColumn = (JSONObject) savedColumns.get(columnName);
						if (!(Boolean)savedColumn.get("magazineView")) {
							magazineProperties.remove(i);
						}
						magazinePropertiesToFilterOut.add(savedColumn);
					}
					
					if (!savedColumns.containsKey(columnName) && !necessaryColumns.containsKey(columnName) && columnName != null) {
						magazineProperties.remove(i);
					}
				}
				
				for (int i = magazineExtraFieldsToDisplay.size() - 1; i >=0; i-- ) {
					JSONObject singleProperty = (JSONObject) magazineExtraFieldsToDisplay.get(i);
					String propertyName = (String) singleProperty.get("field");
					
					if (savedColumns.containsKey(propertyName)) {
						JSONObject savedColumn = (JSONObject) savedColumns.get(propertyName);
						if (!(Boolean)savedColumn.get("magazineView")) {
							magazineExtraFieldsToDisplay.remove(i);
						}
						magazineExtraFieldsToFilterOut.add(savedColumn);
					}
					
					if (!savedColumns.containsKey(propertyName) && !necessaryColumns.containsKey(propertyName) && propertyName != null) {
						magazineExtraFieldsToDisplay.remove(i);
					}
				}
				
				
				
				// Add fields to the filter out list that are saved, but selected to be disabled on either Details or Magazine views.
				addDetailsFieldsToFilterOut(savedColumnsSortOrder, detailsColumnsToFilterOut);
				addMagazineFieldsToFilterOut(savedColumnsSortOrder, magazinePropertiesToFilterOut);
				addMagazineExtraFieldsToFilterOut(savedColumnsSortOrder, magazineExtraFieldsToFilterOut);
				
				// Use filter out lists to get fields that need to be added to the Browse view columns.
				JSONArray detailsColumnsToAdd = getFilteredArray(savedColumnsSortOrder, detailsColumnsToFilterOut);
				JSONArray magazinePropertiesToAdd = getFilteredArray(savedColumnsSortOrder, magazinePropertiesToFilterOut);
				JSONArray magazineExtraFieldsToAdd = getFilteredArray(savedColumnsSortOrder, magazineExtraFieldsToFilterOut);
				
				// Add non-default (added by this plugins request filter) properties to jsonResponse -> columns -> cells and magazineColumns -> fieldsToDisplay, which are not included in the cells definition.
				addDetailsFields(allColumnsOptions, detailsColumnsToAdd);
				addMagazineFields(magazineProperties, magazinePropertiesToAdd);
				addMagazineExtraFields(magazineExtraFieldsToDisplay, magazineExtraFieldsToAdd);
						
				// Set the order of the displayed properties.
				sortPropertiesByOrderValue(allColumnsOptions, savedColumnsSortOrder); 
				sortPropertiesByOrderValue(magazineProperties, savedColumnsSortOrder);
				sortPropertiesByOrderValue(magazineExtraFieldsToDisplay, savedColumnsSortOrder);
				
			}
		}
				
		logger.logExit(this, "filter");
	}
	
	private JSONArray getFieldsToDisplay(JSONArray magazineViewDefinition, String propertyName) {
		if (magazineViewDefinition != null && magazineViewDefinition.size() > 0) {
			int i;
			for(i = 0; i < magazineViewDefinition.size(); i++) {
				JSONObject element = (JSONObject) magazineViewDefinition.get(i);
				if (element.get("field") == "content") {
					break;
				}
			}
			JSONObject content = (JSONObject) magazineViewDefinition.get(i);
			return (JSONArray) content.get(propertyName);
		} else {
			return new JSONArray();
		}
	}

	private JSONArray getSortOrderList(JSONObject savedData, PluginLogger logger) {
		// Initialize an empty array with initial size.
		// This allows us to add an object at a specified index.
		JSONArray sortedSavedColumns = new JSONArray();
		for (int i=0; i<savedData.size(); i++) {
			JSONObject placeHolder = new JSONObject();
			sortedSavedColumns.add(placeHolder);
		}
		// Iterate through users saved settings data and add to array in the order they need to be shown.
		Collection<?> c = savedData.values();
		Iterator<?> itr = c.iterator();
		while (itr.hasNext()) {
			JSONObject element = (JSONObject) itr.next();
			// Set objects in the sort order array by value of their 'order' property, 0-n.
			Long lindex = (Long) element.get("order"); // Simple casting to get primitive int does not work.
			int index = lindex.intValue(); // Requires the extra step above to get the int.
			sortedSavedColumns.set(index, element);
		}
		return sortedSavedColumns;
	}
	
	// Method to rearrange the fields that will be displayed based @param order, which specifies the target order.
	private void sortPropertiesByOrderValue(JSONArray properties, JSONArray order) {
		for (int i=0; i < order.size(); i++) {
			// Get each field in the order array.
			JSONObject item = (JSONObject) order.get(i);
			String label = (String) item.get("id");
			for (int j = properties.size() - 1; j>=0; j--) {
				JSONObject prop = (JSONObject) properties.get(j);
				// Identify the corresponding field in the target array and rearrange.
				if (prop.get("field").equals(label)) {
					// Appends prop to the end of the array, then removes it from the original location.
					// Continually appending to the end of the array will provide the desired ordering.
					properties.add(prop);
					properties.remove(j);
				}
			}
		}
	}
	
	// Go through the users saved settings list and add fields that have detailsView set to false.
	// These will later be removed from the displayed fields definitions.
	private void addDetailsFieldsToFilterOut(JSONArray fullList, JSONArray detailsFilterOutList) {
		// Store all values that will be removed from the jsonResponse.
		for (int i=0; i < fullList.size(); i++) {
			JSONObject fullListItem = (JSONObject) fullList.get(i);
			if (!(Boolean)fullListItem.get("detailsView")) {
				detailsFilterOutList.add(fullListItem);
			}
		}
	}
	
	// Go through the users saved settings list and add fields that have magazineView set to false.
	// These will later be removed from the displayed fields definitions. 
	private void addMagazineFieldsToFilterOut(JSONArray fullList, JSONArray magazineFilterOutList) {
		// Store all values that will be removed from the jsonResponse.
		for (int i=0; i < fullList.size(); i++) {
			JSONObject fullListItem = (JSONObject) fullList.get(i);
			if (!(Boolean)fullListItem.get("magazineView")) {
				magazineFilterOutList.add(fullListItem);
			}
			// Remove properties with prefix 'Clb', they will be added to 'extraFieldsToDisplay'
			String id = (String) fullListItem.get("id");
			if (id.startsWith("Clb")) {
				magazineFilterOutList.add(fullListItem);
			}
		}
	}
	
	private void addMagazineExtraFieldsToFilterOut(JSONArray fullList, JSONArray magazineFilterOutList) {
		// Store all values that will be removed from the jsonResponse.
		for (int i=0; i < fullList.size(); i++) {
			JSONObject fullListItem = (JSONObject) fullList.get(i);
			if (!(Boolean)fullListItem.get("magazineView")) {
				magazineFilterOutList.add(fullListItem);
			}
			// Remove properties WITHOUT prefix 'Clb'
			String id = (String) fullListItem.get("id");
			if (!id.startsWith("Clb")) {
				magazineFilterOutList.add(fullListItem);
			}
		}
	}
	
	// Return an array, from @param fullList, that does not have elements specified in the @param filterOutList
	private JSONArray getFilteredArray(JSONArray fullList, JSONArray filterOutList) {
		JSONArray filteredArray = new JSONArray();
		for(int i=0; i < fullList.size(); i++) {
			if (!filterOutList.contains(fullList.get(i))) {
				filteredArray.add(fullList.get(i));
			}
		}
		return filteredArray;
	}
	
	// Add additional fields to display definitions in the jsonResponse for Details view.
	private void addDetailsFields(JSONArray fields, JSONArray addList) {
		for (int i=0; i < addList.size(); i++) {
			JSONObject item = (JSONObject) addList.get(i);
			JSONObject fieldToAdd = new JSONObject();
			fieldToAdd.put("filterable", false);
			fieldToAdd.put("width", "10.4em");
			fieldToAdd.put("sortable", true);
			fieldToAdd.put("field", item.get("id"));
			fieldToAdd.put("name", item.get("label"));
			
			fields.add(fieldToAdd);
		}
	}
	// Add additional fields to display definitions in the jsonResponse for Magazine view.
	private void addMagazineFields(JSONArray fields, JSONArray addList) {
		for (int i=0; i < addList.size(); i++) {
			JSONObject item = (JSONObject) addList.get(i);
			JSONObject fieldToAdd = new JSONObject();
			fieldToAdd.put("field", item.get("id"));
			fieldToAdd.put("displayName", item.get("label"));
			
			fields.add(fieldToAdd);
		}
	}
	
	private void addMagazineExtraFields(JSONArray fields, JSONArray addList) {
		for (int i=0; i < addList.size(); i++) {
			JSONObject item = (JSONObject) addList.get(i);
			String decoratorValue = "MagazineViewDecorator.contentCellDecorator" + item.get("id");
			JSONObject fieldToAdd = new JSONObject();
			fieldToAdd.put("field", item.get("id"));
			fieldToAdd.put("decorator", decoratorValue);
			
			fields.add(fieldToAdd);
		}
	}
	
}
