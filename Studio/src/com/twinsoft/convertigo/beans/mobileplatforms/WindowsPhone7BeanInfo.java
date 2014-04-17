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
 * $URL: http://sourceus/svn/convertigo/CEMS_opensource/trunk/Studio/src/com/twinsoft/convertigo/beans/mobiledevices/WindowsPhoneBeanInfo.java $
 * $Author: julienda $
 * $Revision: 31301 $
 * $Date: 2012-08-03 17:52:41 +0200 (ven., 03 août 2012) $
 */

package com.twinsoft.convertigo.beans.mobileplatforms;

import java.beans.PropertyDescriptor;

import com.twinsoft.convertigo.beans.core.MySimpleBeanInfo;

public class WindowsPhone7BeanInfo extends MySimpleBeanInfo {

	public WindowsPhone7BeanInfo() {
		try {
			beanClass = WindowsPhone7.class;
			additionalBeanClass = com.twinsoft.convertigo.beans.core.MobilePlatform.class;

		    iconNameC16 = "/com/twinsoft/convertigo/beans/mobileplatforms/images/windowsphone_color_16x16.png";
		    iconNameC32 = "/com/twinsoft/convertigo/beans/mobileplatforms/images/windowsphone_color_32x32.png";

			resourceBundle = java.util.ResourceBundle.getBundle("com/twinsoft/convertigo/beans/mobileplatforms/res/WindowsPhone7");

			displayName = getExternalizedString("display_name");
			shortDescription = getExternalizedString("short_description");
			
			properties = new PropertyDescriptor[2];
			
			properties[0] = new PropertyDescriptor("windowsPhone7PublisherIDTitle", beanClass, "getWindowsPhone7PublisherIDTitle", "setWindowsPhone7PublisherIDTitle");
			properties[0].setDisplayName(getExternalizedString("property.windowsPhone7PublisherIDTitle.display_name"));
			properties[0].setShortDescription(getExternalizedString("property.windowsPhone7PublisherIDTitle.short_description"));
			properties[0].setExpert(true);
			
			properties[1] = new PropertyDescriptor("windowsPhone7PublisherID", beanClass, "getWindowsPhone7PublisherID", "setWindowsPhone7PublisherID");
			properties[1].setDisplayName(getExternalizedString("property.windowsPhone7PublisherID.display_name"));
			properties[1].setShortDescription(getExternalizedString("property.windowsPhone7PublisherID.short_description"));
			properties[1].setExpert(true);
		}
		catch(Exception e) {
			com.twinsoft.convertigo.engine.Engine.logBeans.error("Exception with bean info; beanClass=" + beanClass.toString(), e);
		}
	}

}
