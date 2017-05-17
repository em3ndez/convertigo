package com.twinsoft.convertigo.engine.admin.services.studio.database_objects;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.ClassUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.engine.AuthenticatedSessionManager.Role;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.admin.services.XmlService;
import com.twinsoft.convertigo.engine.admin.services.at.ServiceDefinition;
import com.twinsoft.convertigo.engine.util.CachedIntrospector;

@ServiceDefinition(
	name = "Set",
	roles = { Role.WEB_ADMIN, Role.PROJECT_DBO_CONFIG },
	parameters = {},
	returnValue = "the state of saving properties"
)
public class Set extends XmlService {
	
	private Object getPropertyValue(DatabaseObject object, String propertyName, String propertyValue)
			throws TransformerException {
		return object.compileProperty(propertyName, propertyValue);
	}

	protected void getServiceResult(HttpServletRequest request, Document document) throws Exception {
		Element root = document.getDocumentElement();
		String[] qnames = request.getParameterValues("qnames[]");
		
		// Remove duplicates if someone sends the qname more than once
		java.util.Set<String> uniqueQnames = new HashSet<>(Arrays.asList(qnames));
		
		for (String objectQName : uniqueQnames) {
			// Create the response : success or fail
			Element response = document.createElement("response");
			response.setAttribute("qname", objectQName);

			try {
				String value = request.getParameter("value");
				String property = request.getParameter("property");
	
				DatabaseObject dbo = Engine.theApp.databaseObjectsManager.getDatabaseObjectByQName(objectQName);
				
				// Check if we try to update project name
				if (dbo instanceof Project && "name".equals(property)) {
					Project project = (Project) dbo;
					String objectNewName = getPropertyValue(dbo, property, value).toString();
					
					Engine.theApp.databaseObjectsManager.renameProject(project, objectNewName);
				}
	
				BeanInfo bi = CachedIntrospector.getBeanInfo(dbo.getClass());
				PropertyDescriptor[] propertyDescriptors = bi.getPropertyDescriptors();
				
				boolean propertyFound = false;
				for (int i = 0; !propertyFound && i < propertyDescriptors.length; ++i) {
					String propertyName = propertyDescriptors[i].getName();
					
					// Find the property we want to change
					if (propertyFound = propertyName.equals(property)) {
						Method setter = propertyDescriptors[i].getWriteMethod();
						
						Class<?> propertyTypeClass = propertyDescriptors[i].getReadMethod().getReturnType();
						if (propertyTypeClass.isPrimitive()) {
							propertyTypeClass = ClassUtils.primitiveToWrapper(propertyTypeClass);
						}
						
						try {
							String propertyValue = getPropertyValue(dbo, propertyName, value).toString();
							Object oPropertyValue = com.twinsoft.convertigo.engine.admin.services.database_objects.Set.createObject(propertyTypeClass, propertyValue);
			
							if (dbo.isCipheredProperty(propertyName)) {
								Method getter = propertyDescriptors[i].getReadMethod();
								String initialValue = (String) getter.invoke(dbo, (Object[]) null);
								
								if (oPropertyValue.equals(initialValue) || 
										DatabaseObject.encryptPropertyValue(initialValue).equals(oPropertyValue)) {
									oPropertyValue = initialValue;
								}
								else {
									dbo.hasChanged = true;
								}
							}
							
							// Update property value
							if (oPropertyValue != null) {
								Object args[] = { oPropertyValue };
								setter.invoke(dbo, args);
							}
							
						}
						catch (IllegalArgumentException e) {}
					}
				}
				
				// Invalid given property parameter
				if (!propertyFound) {
					throw new IllegalArgumentException("Property '" + property
							+ "' not found for object '" + dbo.getQName() + "'");
				}
				
				response.setAttribute("state", "success");
				response.setAttribute("message", "Property " + property + " has been successfully updated.");
				
				Element elt = dbo.toXml(document, property);
				elt.setAttribute("name", dbo.toString());
				elt.setAttribute("hasChanged", Boolean.toString(dbo.hasChanged));
	
				response.appendChild(elt);
			}
			catch (Exception e) {
				Engine.logAdmin.error("Error during saving the properties!\n"+e.getMessage());
				response.setAttribute("state", "error");
				response.setAttribute("message", "Error during saving the properties!");
				Element stackTrace = document.createElement("stackTrace");
				stackTrace.setTextContent(e.getMessage());
				root.appendChild(stackTrace);
			}
			finally {
				root.appendChild(response);
			}
		}
	}

}