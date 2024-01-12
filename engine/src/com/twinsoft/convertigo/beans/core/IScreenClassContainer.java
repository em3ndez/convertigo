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

package com.twinsoft.convertigo.beans.core;

import java.util.List;

import com.twinsoft.convertigo.engine.EngineException;

public interface IScreenClassContainer<SC extends ScreenClass> {

	public SC getDefaultScreenClass();
	
	public void setDefaultScreenClass(ScreenClass defaultScreenClass) throws EngineException;
	
	public List<SC> getAllScreenClasses();

	public void add(DatabaseObject databaseObject) throws EngineException;
	
	public SC getCurrentScreenClass() throws EngineException;
	
	public SC getScreenClassByName(String screenClassName);
	
	public SC newScreenClass();
}
