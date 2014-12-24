/*
 * Copyright (c) 2001-2011 Convertigo SA.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 * $URL: $
 * $Author: $
 * $Revision: $
 * $Date: $
 */
package com.twinsoft.convertigo.beans.transactions.couchdb;

import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.twinsoft.convertigo.beans.variables.RequestableVariable;
import com.twinsoft.convertigo.engine.providers.couchdb.api.Document;

public class CreateDocumentTransaction extends AbstractDocumentTransaction {

	private static final long serialVersionUID = -9092966058078541838L;
	
	public CreateDocumentTransaction() {
		super();
	}
	
	@Override
	public CreateDocumentTransaction clone() throws CloneNotSupportedException {
		CreateDocumentTransaction clonedObject =  (CreateDocumentTransaction) super.clone();
		return clonedObject;
	}
	
	@Override
	public List<CouchDbParameter> getDeclaredParameters() {
		return Arrays.asList(new CouchDbParameter[] {var_database, var_document});
	}
	
	@Override
	protected String generateID() {
		return Document.generateID(doc_base_path);
	}
	
	@Override
	protected Object invoke() {
		try {
			// get whole document if one
			JsonElement jsond = toJson(getGson(), new JsonParser(), getParameterValue(var_document));
			JsonObject jsonDocument = jsond == null ? new JsonObject(): jsond.getAsJsonObject();
			
			// add document members from variables
			for (RequestableVariable variable : getVariablesList()) {
				String variableName = variable.getName();
				if (CouchDbParameter.contains(getDeclaredParameters(),variableName)) continue;
				JsonElement jsonv = toJson(getGson(), new JsonParser(), getParameterValue(variableName));
				if (jsonv != null) jsonDocument.add(variableName, jsonv);
			}
			
			addIdToDoc(jsonDocument);
			removeRevFromDoc(jsonDocument);
			
			String jsonString = jsonDocument.toString();
			return getCouchDBDocument().create(encode(jsonString));
		}
		catch (Throwable t) {
			throw new RuntimeException("Unable to create new document", t);
		}		
	}

	@Override
	public QName getComplexTypeAffectation() {
		return new QName(COUCHDB_XSD_NAMESPACE, "docCreateType");
	}
}

