/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bug 202583
 *******************************************************************************/
package no.javatime.inplace.pl.trace;

import java.io.File;
import java.util.Dictionary;

import no.javatime.inplace.extender.provider.Extender;
import no.javatime.inplace.pl.trace.intface.MessageView;
import no.javatime.inplace.pl.trace.intface.Trace;
import no.javatime.inplace.pl.trace.logservice.TraceLogWriter;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.log.ExtendedLogReaderService;
import org.eclipse.equinox.log.ExtendedLogService;
import org.eclipse.equinox.log.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

public class Activator extends AbstractUIPlugin {

	// Log path and file
	public static final String F_META_AREA = ".metadata"; //$NON-NLS-1$
	public static final String F_TRACE = "command.log"; //$NON-NLS-1$
	private IPath traceLogPath;
	private File fInputFile;
	
	// Get the workbench window from UI thread
	private IWorkbenchWindow workBenchWindow;

	public static final String PLUGIN_ID = "no.javatime.inplace.pl.trace"; //$NON-NLS-1$

	private static Activator plugin;
	private static BundleContext context;
	
	private ServiceTracker<ExtendedLogService, ExtendedLogService> extendedLogServiceTracker;
	private ServiceTracker<ExtendedLogReaderService, ExtendedLogReaderService> extendedLogReaderServiceTracker;
	private ServiceTracker<EnvironmentInfo, EnvironmentInfo> environmentInfoServiceTracker;
	
	/**
	 * The constructor
	 */
	public Activator() { 
		traceLogPath = Platform.getLocation().append("\\").append(F_META_AREA).append(F_TRACE);
		fInputFile = traceLogPath.toFile();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		Activator.context = context;
		plugin = this;
		Bundle bundle = context.getBundle();
		Dictionary<String, String> dictionary = bundle.getHeaders();

		String contImpl = dictionary.get(Trace.TRACE_CONTAINER_HEADER);
		Extender.<Trace>register(bundle, Trace.class, contImpl);
		
		String msgImpl = dictionary.get(MessageView.MESSAGE_VIEW_HEADER);
	  Extender.<MessageView>register(bundle, MessageView.class, msgImpl);
	  
		TraceLogWriter writer = new TraceLogWriter(getLogService(), context.getBundle());
		ExtendedLogReaderService readerService = getLogReaderService(); 
		readerService.addLogListener(writer, writer);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		extendedLogServiceTracker.close();
		extendedLogReaderServiceTracker.close();
		plugin = null;
		Activator.context = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static BundleContext getContext() {
		return context;
	}
	
	public File getLogFile() {
		return fInputFile;
	}
	
	public void setLogFile(File fInputFile) {
		this.fInputFile = fInputFile;
	}
	
	public EnvironmentInfo getEnvironmentService() {

		if (null == environmentInfoServiceTracker) {
			if (null == context) {
				return null;
			}
			environmentInfoServiceTracker = new 
					ServiceTracker<EnvironmentInfo, EnvironmentInfo>(context, EnvironmentInfo.class, null);
			environmentInfoServiceTracker.open();
		}
		return environmentInfoServiceTracker.getService();
	}

	/**
	 * The log reader service used by this bundle
	 * @return the log service or null if this {@code context} is null
	 */
	public ExtendedLogReaderService getLogReaderService() {

		if (null == extendedLogReaderServiceTracker) {
			if (null == context) {
				return null;
			}
			extendedLogReaderServiceTracker = new 
					ServiceTracker<ExtendedLogReaderService, ExtendedLogReaderService>(context, ExtendedLogReaderService.class, null);
			extendedLogReaderServiceTracker.open();
		}
		return extendedLogReaderServiceTracker.getService();
	}

	/**
	 * The log service used by this bundle
	 * @return the log service or null if this {@code context} is null
	 */
	public ExtendedLogService getLogService() {

		if (null == extendedLogServiceTracker) {
			if (null == context) {
				return null;
			}
			extendedLogServiceTracker = new 
					ServiceTracker<ExtendedLogService, ExtendedLogService>(context, ExtendedLogService.class, null);
			extendedLogServiceTracker.open();
		}
		return extendedLogServiceTracker.getService();
	}

	public Logger getLogger(Bundle bundle) {
		ExtendedLogService logService = getLogService();
		if (null == bundle) {
			return logService.getLogger(null);
		} else {
			return logService.getLogger(bundle, TraceLogWriter.TRACE_LOGGER_NAME); 			
		}
	}

	protected void initializeImageRegistry(ImageRegistry registry) {
		registry.put(SharedImages.DESC_PREV_EVENT, createImageDescriptor(SharedImages.DESC_PREV_EVENT));
		registry.put(SharedImages.DESC_NEXT_EVENT, createImageDescriptor(SharedImages.DESC_NEXT_EVENT));

		registry.put(SharedImages.DESC_ERROR_ST_OBJ, createImageDescriptor(SharedImages.DESC_ERROR_ST_OBJ));
		registry.put(SharedImages.DESC_ERROR_STACK_OBJ, createImageDescriptor(SharedImages.DESC_ERROR_STACK_OBJ));
		registry.put(SharedImages.DESC_INFO_ST_OBJ, createImageDescriptor(SharedImages.DESC_INFO_ST_OBJ));
		registry.put(SharedImages.DESC_OK_ST_OBJ, createImageDescriptor(SharedImages.DESC_OK_ST_OBJ));
		registry.put(SharedImages.DESC_WARNING_ST_OBJ, createImageDescriptor(SharedImages.DESC_WARNING_ST_OBJ));
		registry.put(SharedImages.DESC_HIERARCHICAL_LAYOUT_OBJ, createImageDescriptor(SharedImages.DESC_HIERARCHICAL_LAYOUT_OBJ));

		registry.put(SharedImages.DESC_CLEAR, createImageDescriptor(SharedImages.DESC_CLEAR));
		registry.put(SharedImages.DESC_CLEAR_DISABLED, createImageDescriptor(SharedImages.DESC_CLEAR_DISABLED));
		registry.put(SharedImages.DESC_REMOVE_LOG, createImageDescriptor(SharedImages.DESC_REMOVE_LOG));
		registry.put(SharedImages.DESC_REMOVE_LOG_DISABLED, createImageDescriptor(SharedImages.DESC_REMOVE_LOG_DISABLED));
		registry.put(SharedImages.DESC_EXPORT, createImageDescriptor(SharedImages.DESC_EXPORT));
		registry.put(SharedImages.DESC_EXPORT_DISABLED, createImageDescriptor(SharedImages.DESC_EXPORT_DISABLED));
		registry.put(SharedImages.DESC_FILTER, createImageDescriptor(SharedImages.DESC_FILTER));
		registry.put(SharedImages.DESC_FILTER_DISABLED, createImageDescriptor(SharedImages.DESC_FILTER_DISABLED));
		registry.put(SharedImages.DESC_IMPORT, createImageDescriptor(SharedImages.DESC_IMPORT));
		registry.put(SharedImages.DESC_IMPORT_DISABLED, createImageDescriptor(SharedImages.DESC_IMPORT_DISABLED));
		registry.put(SharedImages.DESC_OPEN_LOG, createImageDescriptor(SharedImages.DESC_OPEN_LOG));
		registry.put(SharedImages.DESC_OPEN_LOG_DISABLED, createImageDescriptor(SharedImages.DESC_OPEN_LOG_DISABLED));
		registry.put(SharedImages.DESC_PROPERTIES, createImageDescriptor(SharedImages.DESC_PROPERTIES));
		registry.put(SharedImages.DESC_PROPERTIES_DISABLED, createImageDescriptor(SharedImages.DESC_PROPERTIES_DISABLED));
		registry.put(SharedImages.DESC_READ_LOG, createImageDescriptor(SharedImages.DESC_READ_LOG));
		registry.put(SharedImages.DESC_READ_LOG_DISABLED, createImageDescriptor(SharedImages.DESC_READ_LOG_DISABLED));
	}

	private ImageDescriptor createImageDescriptor(String id) {
		return imageDescriptorFromPlugin(PLUGIN_ID, id);
	}
	/**
	 * Returns the active or the default display if the active display is null
	 * @return the active or default display or null if the display is disposed
	 */
	public static Display getDisplay() {
     Display display = Display.getCurrent();
     //may be null if outside the UI thread
     if (display == null) {
        display = Display.getDefault();
     }
     if (display.isDisposed()) { 
    	 return null;
     }
     return display;		
  }
	/**
	 * Returns the active workbench shell
	 * @return the active workbench shell or null if not found
	 */
	public Shell getActiveWorkbenchShell() {
		IWorkbenchWindow workBenchWindow = getActiveWorkbenchWindow();
		if (workBenchWindow == null) {
			return null;
		}
		return workBenchWindow.getShell();
	}


	/**
	 * Returns the active workbench window
	 * @return the active workbench window or null if not found
	 */
	private IWorkbenchWindow getActiveWorkbenchWindow() {
		if (plugin == null) {
			return null;
		}
		workBenchWindow = null;
		getDisplay().syncExec(new Runnable() {
			public void run() {
				final IWorkbench workBench = PlatformUI.getWorkbench();
				if (workBench == null) {
					return;
				}
				workBenchWindow = workBench.getActiveWorkbenchWindow();
			}
		});
		return workBenchWindow;
	}

	/**
	 * Returns the active page
	 * @return the active page or null if not found
	 */
	public IWorkbenchPage getActivePage() {
		IWorkbenchWindow activeWorkbenchWindow = getActiveWorkbenchWindow();
		if (activeWorkbenchWindow == null) {
			return null;
		}
		return activeWorkbenchWindow.getActivePage();
	}

	/**
   * Returns an image descriptor for the image file at the given plug-in relative path.
   * 
   * @param path
   *            the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path) {
      return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
  }

}
