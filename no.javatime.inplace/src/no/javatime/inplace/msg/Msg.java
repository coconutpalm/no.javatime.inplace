/*******************************************************************************
 * Copyright (c) 2007, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacek Pospychala <jacek.pospychala@pl.ibm.com> - bugs 202583, 207344
 *     Benjamin Cabe <benjamin.cabe@anyware-tech.com> - bug 218648 
 *******************************************************************************/
package no.javatime.inplace.msg;

import org.eclipse.osgi.util.NLS;

public class Msg extends NLS {

	// Manifest trace operations 
	public static String UPDATE_BUNDLE_CLASSPATH_TRACE;
	public static String REMOVE_BUNDLE_CLASSPATH_ENTRY_TRACE;
	public static String REMOVE_BUNDLE_CLASSPATH_TRACE;
	public static String TOGGLE_ACTIVATION_POLICY_TRACE;
	
	// Bundle trace operations
	public static String START_BUNDLE_OP_TRACE;
	public static String STOP_BUNDLE_OP_TRACE;
	public static String REFRESH_BUNDLE_OP_TRACE;
	public static String RESOLVE_BUNDLE_OP_TRACE;
	public static String UNRESOLVE_BUNDLE_OP_TRACE;
	public static String UNINSTALL_BUNDLE_OP_TRACE;
	public static String INSTALL_BUNDLE_OP_TRACE;
	public static String UPDATE_BUNDLE_OP_TRACE;
	public static String LAZY_ACTIVATE_BUNDLE_OP_TRACE;
	public static String ON_DEMAND_BUNDLE_START_OP_TRACE;
	public static String EXTERNAL_BUNDLE_OP_TRACE;
	public static String DISABLE_NATURE_TRACE;
	public static String ENABLE_NATURE_TRACE;
	public static String UNDEFINED_CONTEXT_ERROR_TRACE;
	public static String JOB_NAME_TRACE;
	public static String FRAMEWORK_BUNDLE_OP_TRACE;
	public static String ADD_PROJECT_OP_TRACE;
	public static String REMOVE_PROJECT_OP_TRACE;
	
	// Build trace operations
	public static String BUILD_HEADER_TRACE;
	public static String NO_RESOURCE_DELTA_BUILD_TRACE;
	public static String NO_RESOURCE_DELTA_BUILD_AVAILABLE_TRACE;
	public static String INCREMENTAL_BUILD_TRACE;
	public static String FULL_BUILD_TRACE;
	public static String BUILD_ERROR_UPDATE_TRACE;
	public static String BUILD_ERROR_TRACE;
	public static String USING_CURRENT_REVISION_TRACE;
	
	// Bundle job names
	public static String ACTIVATE_PROJECTS_JOB;
	public static String ACTIVATE_WORKSPACE_JOB;
	public static String INIT_DEACTIVATED_WORKSPACE_JOB;
  public static String UPDATE_BUNDLE_CLASS_PATH_JOB; 
  public static String REMOVE_BUNDLE_PROJECT_JOB;
  public static String ADD_BUNDLE_PROJECT_JOB;
  
	// Errors
	public static String BEGIN_SHUTDOWN_ERROR;
	public static String END_SHUTDOWN_ERROR;
	public static String INSTALL_ERROR;
	public static String SYMBOLIC_NAME_ERROR;
	public static String FATAL_ACTIVATE_ERROR;
	public static String MANIFEST_BUILD_ERROR;
	public static String BUILD_STATE_ERROR;
	public static String END_JOB_ROOT_ERROR;
	public static String GET_SERVICE_ERROR;
	public static String INIT_BUNDLE_STATE_ERROR;

	// Warnings
	public static String EXTERNAL_UNINSTALL_WARN;
	public static String REQUIRING_BUNDLES_WARN;
	public static String DELAYED_BUILD_WARN;
	public static String RENAME_PROJECT_WARN;
	public static String INIT_WORKSPACE_STORE_WARN;
	public static String INSTALL_CONTEXT_FOR_ACTION_SET_WARN;
	public static String NOT_RESOLVED_ROOT_WARN;
	
	// Info messages
	public static String DEACTIVATE_BUILD_ERROR_INFO;
	public static String UPDATE_BUILD_ERROR_INFO;
	public static String ACTIVATED_BUNDLES_INFO;
	public static String IMPLICIT_ACTIVATION_INFO;
	public static String DELAYED_RESOLVE_INFO;
	public static String DELAYED_UPDATE_INFO;
	public static String SYSTEM_EXIT_INFO;
	public static String CLASS_PATH_DEV_PARAM_INFO;
	public static String REFRESH_HINT_INFO;
	public static String CANCEL_JOB_INFO;
	public static String BUILDER_OFF_LIST_INFO;
	public static String BUILDER_OFF_INFO;
	public static String NO_PROJECTS_TO_ACTIVATE_INFO;
	public static String ADD_BUNDLES_TO_ACTIVATE_INFO;
	public static String UNINSTALL_BEFORE_ACTIVATE_INFO;
	public static String MISSING_CLASSPATH_BUNDLE_LOADED_INFO;
	public static String MISSING_CLASSPATH_BUNDLE_INFO;
	public static String MISSING_DEV_CLASSPATH_BUNDLE_INFO;
	public static String THREAD_BLOCKED_INFO;
	public static String THREAD_WAITING_INFO;
	public static String FAILED_TO_GET_THREAD_INFO;
	public static String NO_TASK_TO_STOP_INFO;
	public static String DEACTIVATING_AFTER_TIMEOUT_STOP_TASK_INFO;
	public static String DEACTIVATING_MANUAL_STOP_TASK_INFO;
	public static String AFTER_TIMEOUT_STOP_TASK_INFO;
	public static String MANUAL_STOP_TASK_INFO;
	public static String CLASS_PATH_COMMON_INFO;
	public static String WAITING_ON_JOB_INFO;
	public static String CUSTOMIZED_STATUS_HANDLER_INFO;
	public static String CUSTOMIZED_STATUS_HANDLER_CMD_LINE_INFO;
	public static String STANDARD_STATUS_HANDLER_INFO;
	public static String USE_CUSTOMIZED_STATUS_HANDLER_INFO;
	public static String ATOBUILD_OFF_RESET_INFO;
	public static String AUTOUPDATE_OFF_INFO;
	public static String CONDITIONAL_START_BUNDLE_INFO;
	public static String UNINSTALLED_REQUIRING_BUNDLES_INFO;
	public static String NOT_RESOLVED_BUNDLE_WARN;
	public static String PROVIDING_TO_NOT_RESOLVED_BUNDLES_WARN;

	// Dialog and View messages
	public static String DEACTIVATE_QUESTION_DLG;
	public static String DEACTIVATE_QUESTION_REQ_DLG;

	// Location references
	public static String JAVATIME_ID_NATURE_ID;
	
	private static final String BUNDLE_NAME = "no.javatime.inplace.msg.messages"; //$NON-NLS-1$

	static {
		NLS.initializeMessages(BUNDLE_NAME, Msg.class);
	}

}
