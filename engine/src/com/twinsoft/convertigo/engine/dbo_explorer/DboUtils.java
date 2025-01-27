/*
 * Copyright (c) 2001-2025 Convertigo SA.
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

package com.twinsoft.convertigo.engine.dbo_explorer;

import com.twinsoft.convertigo.beans.connectors.JavelinConnector;
import com.twinsoft.convertigo.beans.connectors.SiteClipperConnector;
import com.twinsoft.convertigo.beans.core.Connector;
import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.ScreenClass;
import com.twinsoft.convertigo.beans.core.Sequence;
import com.twinsoft.convertigo.beans.core.Step;
import com.twinsoft.convertigo.beans.core.Transaction;
import com.twinsoft.convertigo.beans.core.Variable;
import com.twinsoft.convertigo.beans.transactions.SiteClipperTransaction;

public class DboUtils {

	public static String getTechnology(DatabaseObject parentObject, Class<? extends DatabaseObject> objectClass) {
		String technology = null;

		DatabaseObject parent = parentObject;
		if (parent != null) {
			// case of Variable
			if (Variable.class.isAssignableFrom(objectClass)) {
				return technology = parent.getClass().getName();
			}

			// parent is a connector
			if (parent instanceof Connector) {
				return technology = ((Connector) parent).getClass().getName();
			}

			// parent is a sequence
			if (parent instanceof Sequence) {
				return technology = ((Sequence) parent).getClass().getName();
			}

			// parent is a step
			if (parent instanceof Step) {
				technology = "com.twinsoft.convertigo.beans.steps.BlockStep";
				if (getClassName(parent.getClass()).startsWith("XML")) {
					technology = parent.getClass().getName();
				}
				return technology;
			}

			// parent is a transaction
			if (parent instanceof Transaction) {
				if (parent instanceof SiteClipperTransaction) {
					return technology = "com.twinsoft.convertigo.beans.transactions.SiteClipperTransaction";
				}
			}

			// parent is a screenclass
			if (parent instanceof ScreenClass) {
				while ((parent = parent.getParent()) instanceof ScreenClass) {
					;
				}
				if (parent instanceof JavelinConnector)
					technology = ((JavelinConnector) parent).getEmulatorTechnology();
				if (parent instanceof SiteClipperConnector)
					technology = "com.twinsoft.convertigo.beans.screenclasses.SiteClipperScreenClass";
			}
		}
		return technology;
	}

	private static String getClassName(Class<?> c) {
		String FQClassName = c.getName();
		int firstChar;
		firstChar = FQClassName.lastIndexOf('.') + 1;
		if (firstChar > 0)
			FQClassName = FQClassName.substring(firstChar);
		return FQClassName;
	}


}
