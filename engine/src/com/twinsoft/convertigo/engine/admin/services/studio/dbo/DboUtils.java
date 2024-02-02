/*
 * Copyright (c) 2001-2024 Convertigo SA.
 * 
 * This program  is free software; you  can redistribute it and/or
 * Modify  it  under the  terms of the  GNU  Affero General Public
 * License  as published by  the Free Software Foundation;  either
 * version  3  of  the  License,  or  (at your option)  any  later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;  without even the implied warranty of
 * MERCHANTABILITY  or  FITNESS  FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */

package com.twinsoft.convertigo.engine.admin.services.studio.dbo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.IContainerOrdered;
import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.beans.core.RequestableStep;
import com.twinsoft.convertigo.beans.core.TestCase;
import com.twinsoft.convertigo.beans.steps.SequenceStep;
import com.twinsoft.convertigo.beans.steps.TransactionStep;
import com.twinsoft.convertigo.beans.core.DatabaseObject.ExportOption;
import com.twinsoft.convertigo.beans.variables.RequestableVariable;
import com.twinsoft.convertigo.beans.variables.StepVariable;
import com.twinsoft.convertigo.beans.variables.TestCaseVariable;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.EngineException;
import com.twinsoft.convertigo.engine.ObjectWithSameNameException;
import com.twinsoft.convertigo.engine.admin.services.studio.Utils;
import com.twinsoft.convertigo.engine.helpers.WalkHelper;

public class DboUtils {

	static protected DatabaseObject findDbo(String id) throws Exception {
		return Utils.getDbo(id);
	}

	static protected boolean canCut(DatabaseObject dbo) {
		return DboFactory.isCuttable(dbo);
	}

	static protected boolean acceptDbo(DatabaseObject targetDatabaseObject, DatabaseObject databaseObject,
			boolean includeSpecials) {
		if (targetDatabaseObject.getQName().startsWith(databaseObject.getQName())) {
			return false;
		}
		if (!DboFactory.acceptDbo(targetDatabaseObject, databaseObject, includeSpecials)) {
			return false;
		}
		return true;
	}

	static protected DatabaseObject createDbo(JSONObject jsonData, DatabaseObject parentDbo) throws Exception {
		if (jsonData.has("type")) {
			var type = jsonData.getString("type");
			if (type.equals("paletteData")) {
				return createDboFromPalette(jsonData);
			} else if (type.equals("treeData")) {
				return createDboFromTree(jsonData, parentDbo);
			}
		}
		return null;
	}

	static private DatabaseObject createDboFromPalette(JSONObject jsonData) throws Exception {
		DatabaseObject dbo = null;

		JSONObject jsonItem = jsonData.getJSONObject("data");

		var dboClassName = jsonItem.getString("classname");
		var dboType = jsonItem.getString("type");
		var dboId = jsonItem.getString("id");

		// case Bean
		if (dboType.equals("Dbo")) {
			dbo = (DatabaseObject) Class.forName(dboClassName).getConstructor().newInstance();
		}
		// case ionBean
		else if (dboType.equals("Ion")) {
			var kind = dboId.split(" ")[0];
			if (kind.equals("ngx")) {
				var ionBeanName = dboId.split(" ")[1];
				com.twinsoft.convertigo.beans.ngx.components.dynamic.Component component = null;
				component = com.twinsoft.convertigo.beans.ngx.components.dynamic.ComponentManager
						.getComponentByName(ionBeanName);
				if (component != null) {
					dbo = com.twinsoft.convertigo.beans.ngx.components.dynamic.ComponentManager
							.createBeanFromHint(component);
				}
			}
		}

		return dbo;
	}

	static private DatabaseObject createDboFromTree(JSONObject jsonData, DatabaseObject parentDbo) throws Exception {
		JSONObject jsonItem = jsonData.getJSONObject("data");
		var dboId = jsonItem.getString("id");
		DatabaseObject dbo = findDbo(dboId);
		if (dbo != null) {
			return DboFactory.createDbo(parentDbo, dbo);
		}
		return null;
	}

	static protected Object read(Node node) throws Exception {
		Class<?> objectClass = null;
		Object object = null;
		Element element = (Element) node;
		String objectClassName = element.getAttribute("classname");
		try {
			objectClass = Class.forName(objectClassName);
			Method readMethod = objectClass.getMethod("read", new Class[] { Node.class });
			object = readMethod.invoke(null, new Object[] { node });
		} catch (Exception e) {
			throw new EngineException("Unable to read object", e);
		}
		return object;
	}

	static protected void xmlCut(Document document, String id) throws Exception {
		Element element = document.createElement("dbo");
		element.setAttribute("id", id);
		document.getDocumentElement().appendChild(element);
	}

	static protected void xmlCopy(Document document, DatabaseObject dbo) throws Exception {
		final Element rootElement = document.getDocumentElement();

		new WalkHelper() {
			protected Element parentElement = rootElement;

			@Override
			protected void walk(DatabaseObject databaseObject) throws Exception {
				Element parentElement = this.parentElement;

				Element element = databaseObject.toXml(document, ExportOption.bIncludeVersion);
				parentElement.appendChild(element);

				this.parentElement = element;
				super.walk(databaseObject);
				this.parentElement = parentElement;
			}
		}.init(dbo);
	}

	static protected Object xmlPaste(Node node, DatabaseObject parentDbo) throws Exception {
		Object object = read(node);
		if (object instanceof DatabaseObject) {
			DatabaseObject databaseObject = (DatabaseObject) object;
			String dboName = databaseObject.getName();

			// Special case of project
			if (databaseObject instanceof Project) {
				return databaseObject;
			}

			if (!DboFactory.acceptDboAsSuch(parentDbo, databaseObject)) {
				throw new EngineException("You cannot paste to a " + parentDbo.getClass().getSimpleName()
						+ " a database object of type " + databaseObject.getClass().getSimpleName());
			}

			boolean bContinue = true;
			boolean bIncName = false;
			// long oldPriority = databaseObject.priority;

			// Verify if a child object with same name exist and change name
			while (bContinue) {
				if (bIncName) {
					dboName = DatabaseObject.incrementName(dboName);
					databaseObject.setName(dboName);
				}

				databaseObject.hasChanged = true;
				databaseObject.bNew = true;

				try {
					new WalkHelper() {
						boolean root = true;
						boolean find = false;

						@Override
						protected boolean before(DatabaseObject dbo, Class<? extends DatabaseObject> dboClass) {
							boolean isInstance = dboClass.isInstance(databaseObject);
							find |= isInstance;
							return isInstance;
						}

						@Override
						protected void walk(DatabaseObject dbo) throws Exception {
							if (root) {
								root = false;
								super.walk(dbo);
								if (!find) {
									// ignore: we must accept special paste: e.g. transaction over sequence
								}
							} else {
								if (databaseObject.getName().equalsIgnoreCase(dbo.getName())) {
									throw new ObjectWithSameNameException(
											"Unable to paste the object because an object with the same name already exists in target.");
								}
							}
						}

					}.init(parentDbo);
					bContinue = false;
				} catch (ObjectWithSameNameException owsne) {
					bIncName = true;
				} catch (EngineException ee) {
					throw ee;
				} catch (Exception e) {
					throw new EngineException("Exception in paste", e);
				}
			}

			if (parentDbo instanceof IContainerOrdered) {
				databaseObject.priority = databaseObject.getNewOrderValue();
			}
			parentDbo.add(databaseObject);

			NodeList childNodes = node.getChildNodes();
			int len = childNodes.getLength();

			Node childNode;
			String childNodeName;

			for (int i = 0; i < len; i++) {
				childNode = childNodes.item(i);
				if (childNode.getNodeType() != Node.ELEMENT_NODE) {
					continue;
				}
				childNodeName = childNode.getNodeName();
				if (!(childNodeName.equalsIgnoreCase("property")) && !(childNodeName.equalsIgnoreCase("handlers"))
						&& !(childNodeName.equalsIgnoreCase("wsdltype")) && !(childNodeName.equalsIgnoreCase("docdata"))
						&& !(childNodeName.equalsIgnoreCase("dnd"))) {
					xmlPaste(childNode, databaseObject);
				}
			}

			databaseObject.isImporting = false; // needed
			databaseObject.isSubLoaded = true;
			return databaseObject;
		}
		return null;
	}

	static protected boolean changeBeanName(JSONArray ids, DatabaseObject dbo, Object oldValue, Object newValue, String update) {
		if (dbo == null || newValue == null || newValue.toString().isBlank()) {
			return false;
		}

		// first rename dbo
		try {
			String oldQName = dbo.getFullQName();
			dbo.setName((String) newValue);
			dbo.hasChanged = true;
			ids.put(oldQName);
		} catch (Exception e) {
			Engine.logEngine.error("Failed to rename " + dbo.getClass().getName() + " " + dbo.getQName(), e);
			return false;
		}

		// if nothing else to rename return
		if (update.equals("UPDATE_NONE") || update.isBlank()) {
			return true;
		}

		// then propagate rename to other beans
		List<String> projectNames = null;
		if (update.equals("UPDATE_LOCAL")) {
			projectNames = new ArrayList<String>();
			projectNames.add(dbo.getProject().getName());
		} else if (update.equals("UPDATE_ALL")) {
			projectNames = Engine.theApp.databaseObjectsManager.getAllProjectNamesList(true);
		}

		if (projectNames != null) {
			WalkHelper walker = getRenameHelper(ids, dbo, oldValue, newValue, update);
			for (String projectName : projectNames) {
				Project project;
				try {
					project = (Project) Engine.theApp.databaseObjectsManager.getDatabaseObjectByQName(projectName);
					walker.init(project);
				} catch (Exception e) {
					Engine.logEngine.error("Failed to propagate rename of " + dbo.getClass().getName() + " " + dbo.getQName(), e);
				}
			}
		}
		
		return true;
	}

	static private WalkHelper getRenameHelper(JSONArray ids, DatabaseObject dbo, Object oldValue, Object newValue, String update) {
		return new WalkHelper() {
			private void setDboName(JSONArray ids, DatabaseObject dbo, Object newValue) {
				try {
					String oldQName = dbo.getFullQName();
					dbo.setName((String) newValue);
					dbo.hasChanged = true;
					ids.put(oldQName);
				} catch (EngineException e) {
					Engine.logEngine.warn("Failed to rename " + dbo.getClass().getName() + " " + dbo.getQName(), e);
				}
			}

			@Override
			protected void walk(DatabaseObject databaseObject) throws Exception {
				boolean isLocalProject = dbo.getProject().equals(databaseObject.getProject());
				boolean isSameValue = databaseObject.getName().equals(oldValue);
				boolean shouldUpdate = update.equals("UPDATE_ALL") || (update.equals("UPDATE_LOCAL") && isLocalProject);

				if (shouldUpdate) {
					if (isSameValue) {
						// A RequestableVariable changed its name
						if (dbo instanceof RequestableVariable) {
							RequestableVariable requestableVariable = (RequestableVariable) dbo;
							String rqname = requestableVariable.getParent().getQName();

							if (databaseObject instanceof TestCaseVariable) {
								TestCaseVariable testCaseVariable = (TestCaseVariable) databaseObject;
								TestCase testCase = (TestCase) testCaseVariable.getParent();
								String tqname = testCase.getParent().getQName();
								if (rqname.equals(tqname)) {
									setDboName(ids, testCaseVariable, newValue);
								}
							} else if (databaseObject instanceof StepVariable) {
								StepVariable stepVariable = (StepVariable) databaseObject;
								RequestableStep requestableStep = (RequestableStep) stepVariable.getParent();
								boolean isTransactionStep = requestableStep instanceof TransactionStep;
								String sqname = isTransactionStep
										? ((TransactionStep) requestableStep).getSourceTransaction()
										: ((SequenceStep) requestableStep).getSourceSequence();
								if (rqname.equals(sqname)) {
									setDboName(ids, stepVariable, newValue);
								}
							}
						}
					} else {
						super.walk(databaseObject);
					}
				}
			}
		};
	}
}