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

package com.twinsoft.convertigo.eclipse.editors.connector;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.part.FileInPlaceEditorInput;

import com.twinsoft.convertigo.beans.core.Connector;
import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.eclipse.ConvertigoPlugin;
import com.twinsoft.convertigo.eclipse.swt.SwtUtils;

public class ConnectorEditorInput extends FileInPlaceEditorInput {

	Connector connector;
	private String qname;
	
	private static IFile getTmpFile(Connector connector, String filename) {
		try {
			return ConvertigoPlugin.getDefault().getProjectPluginResource(connector.getProject().getName()).getFile("_private/editor/" + connector.getShortQName() + "/" + filename);
		} catch (Exception e) {
		}
		return null;
	}
	
	ConnectorEditorInput(Connector connector, String filename) {
		super(getTmpFile(connector, filename));
		this.connector = connector;
		qname = connector.getQName();
	}
	
	public ConnectorEditorInput(Connector connector) {
		this(connector, "init.txt");
		if (!fileExists()) {
			fileWrite("Click on the generation button to view the response generated by Convertigo.");
		}
	}
	
	@Override
	public boolean exists() {
		return super.exists();
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return super.getImageDescriptor();
	}

	@Override
	public String getName() {
		try {
			return connector.getParentName() + " [C: " + connector.getName()+"]." + getFile().getFileExtension();
		} catch (Exception e) {
			return getFile().getName();
		}
	}

	@Override
	public IPersistableElement getPersistable() {
		return super.getPersistable();
	}

	@Override
	public String getToolTipText() {
		return connector.getParentName() + "/" + connector.getName();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		return super.getAdapter(adapter);
	}
	
	public boolean is(Connector connector) {
		return connector.equals(this.connector) && connector.getQName().equals(qname);
	}
	
	public boolean is(Project project) {
		return qname.startsWith(project.getQName());
	}
	
	public Connector getConnector() {
		return connector;
	}
	
	boolean fileExists() {
		return getFile().exists();
	}
	
	void fileWrite(String str) {
		SwtUtils.fillFile(getFile(), str);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConnectorEditorInput) {
			return connector.equals(((ConnectorEditorInput) obj).connector);
		}
		return false;
	}
}
