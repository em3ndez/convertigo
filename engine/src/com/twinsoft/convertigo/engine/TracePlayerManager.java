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

package com.twinsoft.convertigo.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import com.twinsoft.convertigo.engine.trace.ibm.TracePlayer;
import com.twinsoft.convertigo.engine.util.PropertiesUtils;

public class TracePlayerManager implements AbstractManager {
	
	private String _traceplayerFilePath = Engine.CONFIGURATION_PATH + "/traceplayer.properties";
	private Map<Integer, TraceConfig> _traces = new HashMap<Integer, TraceConfig>();
	private Map<TraceConfig, TracePlayer> _traceplayers = new HashMap<TraceConfig, TracePlayer>();
	
	
	public class TraceConfig{
		private int _port;
		private String _file;
		private boolean _enable;
		
		private TraceConfig(int port, String file, boolean enable){
			_port = port;
			_file = file;
			_enable = enable;
		}
		
		public int getPort(){
			return _port;
		}
		
		public String getFile(){
			return _file;
		}
		
		public boolean getEnable(){
			return _enable;
		}
		
		private void setEnable(boolean b){
			_enable = b;
		}
	}

	public void init() throws EngineException {
		
		Properties _props = new Properties();
		try {
			PropertiesUtils.load(_props, _traceplayerFilePath);
			for(Entry<Object,Object> entry : _props.entrySet()){
				String[] tab = entry.getValue().toString().split("=>");
				internalAddTrace(entry.getKey().toString(), tab[0], tab[1]);
			}
			syncTracePlayers();
		}
		catch (IOException e) {
			Engine.logTracePlayerManager.error("Error while trying to get trace player manager properties", e);
		}
	}
	
	private synchronized void startTraceConfig(final TraceConfig trace){
		if(trace.getEnable() && !_traceplayers.containsKey(trace)){
			synchronized (_traceplayers) {
				Thread th = new Thread(new Runnable(){
					public void run() {
						Engine.logTracePlayerManager.trace("The thread has started ("+Thread.currentThread().getName()+")");
						TracePlayer mtp = new TracePlayer();
						synchronized (_traceplayers) {
							_traceplayers.put(trace, mtp);
							_traceplayers.notify();
						}
						mtp.runTrace(Engine.PROJECTS_PATH+trace.getFile(), trace.getPort());
					}
				});
				th.setName("TracePlayer "+trace.getPort());
				th.setDaemon(true);
				th.start();
				try {
					_traceplayers.wait();
				} catch (InterruptedException e) {
					Engine.logTracePlayerManager.error("An error occured while _tracePlayers.wait() function", e);
				}
			}
		}
	}
	
	private void stopTraceConfig(TraceConfig trace){
		if(_traceplayers.containsKey(trace)){
			TracePlayer tracePlayer = _traceplayers.get(trace);
			tracePlayer.closeSocket();
			tracePlayer.stop();
			_traceplayers.remove(trace);
			Engine.logTracePlayerManager.trace("The thread has been stopped (TracePlayer "+trace.getPort()+")");
		}
	}
	
	private void syncTracePlayers(){
		for(TraceConfig trace : _traces.values()){
			stopTraceConfig(trace);
			startTraceConfig(trace);
		}
	}
	
	private void saveTraces(){
		
		Properties _props = new Properties();
		for(TraceConfig trace : _traces.values())
			_props.setProperty(Integer.toString(trace.getPort()), trace.getFile()+"=>"+trace.getEnable());
		try {
			PropertiesUtils.store(_props, _traceplayerFilePath, "Generated by TracePlayerManager");
		} catch (FileNotFoundException e) {
			Engine.logTracePlayerManager.debug("The target file does not exist ("+_traceplayerFilePath+")");
		} catch (IOException e) {
			Engine.logTracePlayerManager.debug("A problem occured while storing traces into the file");
		}
	}
	
	private boolean internalAddTrace(int port, String file, boolean enabled){
		if(!_traces.containsKey(port)){
			_traces.put(port, new TraceConfig(port, file, enabled));
			return true;
		}
		return false;
	}
	
	private boolean addTrace(int port, String file, boolean enabled){
		if(!_traces.containsKey(port)){
			_traces.put(port, new TraceConfig(port, file, enabled));
			saveAndSync();
			return true;
		}
		return false;
	}
	
	private boolean internalAddTrace(String port, String file, String enabled){
		try{
			return internalAddTrace(Integer.parseInt(port), file, toBoolean(enabled));
		}catch (NumberFormatException e) {
			Engine.logTracePlayerManager.debug("The format of the port is invalid ("+port+")");
		}
		return false;
	}
	
	public boolean addTrace(String port, String file, String enabled){
		try{
			return addTrace(Integer.parseInt(port), file, toBoolean(enabled));
		}catch (NumberFormatException e) {
			Engine.logTracePlayerManager.debug("The format of the port is invalid ("+port+")");
		}
		return false;
	}
	
	private void saveAndSync(){
		saveTraces();
		syncTracePlayers();
	}
	
	private Collection<TraceConfig> retrieveTraceConfigs(String[] ports){
		Set<TraceConfig> res = new HashSet<TraceConfig>();
		for(String port : ports){
			try{
				TraceConfig tc=null;
				if(null!=(tc=_traces.get(Integer.parseInt(port)))) res.add(tc);
			}catch (NumberFormatException e) {
				Engine.logTracePlayerManager.debug("The format of the port is invalid ("+port+")");
			}
		}
		return res;
	}
	
	private boolean toBoolean(String var){
		return "true".equalsIgnoreCase(var) || "on".equalsIgnoreCase(var);
	}
	
	private void changeEnabled(String[] ports, boolean enabled){
		for(TraceConfig tc : retrieveTraceConfigs(ports)) tc.setEnable(enabled);
		saveAndSync();
	}
	
	public void enableTraces(String[] ports){
		changeEnabled(ports, true);
	}
	
	public void disableTraces(String[] ports){
		changeEnabled(ports, false);
	}
	
	private void delTrace(TraceConfig traceConfig){
		traceConfig.setEnable(false);
		stopTraceConfig(traceConfig);
		_traces.remove(traceConfig.getPort());
	}
	
	public void delTraces(String[] ports){
		for(TraceConfig tc : retrieveTraceConfigs(ports)) delTrace(tc);
		saveAndSync();
	}
	
	public boolean modifyTrace(String oldPort, String newPort, String newConfigPath, String enabled){
		try{
			return modifyTrace(Integer.parseInt(oldPort), Integer.parseInt(newPort), newConfigPath, toBoolean(enabled));
		}catch (NumberFormatException e) {
			Engine.logTracePlayerManager.debug("The format of the port is invalid (old : "+oldPort+" new : "+newPort+")");
		}
		return false;
	}
	
	private boolean modifyTrace(int oldPort, int newPort, String newConfigPath, boolean enabled){
		TraceConfig trace = _traces.get(oldPort);
		if(trace!=null && (oldPort==newPort || !_traces.containsKey(newPort))){
			delTrace(trace);
			addTrace(newPort, newConfigPath, enabled);
			saveAndSync();
			return true;
		}else return false;
	}
	
	public Collection<TraceConfig> getTraces(){
		return Collections.unmodifiableCollection(_traces.values());
	}
	
	public boolean exists(int port){
		return _traces.containsKey(port);
	}

	public void destroy() throws EngineException {
		for(TraceConfig trace : _traces.values()){
			stopTraceConfig(trace);
		}
	}
	
	public Collection<String> getEtr(){
		Collection<File> res = new LinkedList<File>();
		File[] listdir = new File(Engine.PROJECTS_PATH).listFiles();
		for(File s : listdir){
			File tracedir = new File(s,"Traces");
			if(tracedir.exists()&&tracedir.isDirectory()){
				File[] listconnectors = tracedir.listFiles();
				for(File s2 : listconnectors){
					res.addAll(Arrays.asList(s2.listFiles(new FilenameFilter(){
						public boolean accept(File arg0, String arg1) {
							return arg1.endsWith(".etr");
						}
					})));
				}
			}
		}
		Collection<String> listetr = new ArrayList<String>(res.size());
		for(File f : res)
			listetr.add(f.getPath().substring(Engine.PROJECTS_PATH.length()));
		return listetr;
	}
}
