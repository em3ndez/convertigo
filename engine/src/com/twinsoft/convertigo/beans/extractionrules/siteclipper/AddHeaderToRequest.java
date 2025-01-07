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

package com.twinsoft.convertigo.beans.extractionrules.siteclipper;

import com.twinsoft.convertigo.beans.connectors.SiteClipperConnector.Shuttle;

public class AddHeaderToRequest extends AddHeader implements IRequestRule {

	private static final long serialVersionUID = 954421238020843645L;

	public AddHeaderToRequest() {
		super();
	}

	@Override
	public boolean applyOnRequest(Shuttle shuttle) {
		return addHeader(shuttle);
	}
}
