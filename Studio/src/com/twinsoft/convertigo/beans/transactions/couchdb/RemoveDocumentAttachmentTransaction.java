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

import com.twinsoft.convertigo.engine.util.ParameterUtils;

public class RemoveDocumentAttachmentTransaction extends AbstractDocumentTransaction {

	private static final long serialVersionUID = 251919320155109714L;

	public RemoveDocumentAttachmentTransaction() {
		super();
	}

	@Override
	public RemoveDocumentAttachmentTransaction clone() throws CloneNotSupportedException {
		RemoveDocumentAttachmentTransaction clonedObject =  (RemoveDocumentAttachmentTransaction) super.clone();
		return clonedObject;
	}
	
	@Override
	public List<CouchDbParameter> getDeclaredParameters() {
		return Arrays.asList(new CouchDbParameter[] {var_database, var_docid, var_filename});
	}
		
	@Override
	protected Object invoke() {
		try {
			String docId = ParameterUtils.toString(getParameterValue(var_docid));
			String attName = ParameterUtils.toString(getParameterValue(var_filename));
			return getCouchDBDocument().removeAttachment(docId, getDocLastRev(docId), attName);
		}
		catch (Throwable t) {
			throw new RuntimeException("Unable to remove attachment from document", t);
		}		
	}

}

