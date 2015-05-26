package no.javatime.inplace.bundlejobs;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import no.javatime.inplace.Activator;
import no.javatime.inplace.bundlejobs.intface.ActivateProject;
import no.javatime.inplace.dl.preferences.intface.CommandOptions;
import no.javatime.inplace.dl.preferences.intface.MessageOptions;
import no.javatime.inplace.extender.intface.ExtenderException;
import no.javatime.inplace.msg.Msg;
import no.javatime.inplace.region.closure.ProjectSorter;
import no.javatime.inplace.region.events.BundleTransitionEvent;
import no.javatime.inplace.region.events.BundleTransitionEventListener;
import no.javatime.inplace.region.intface.BundleCommand;
import no.javatime.inplace.region.intface.BundleProjectCandidates;
import no.javatime.inplace.region.intface.BundleProjectMeta;
import no.javatime.inplace.region.intface.BundleRegion;
import no.javatime.inplace.region.intface.BundleTransition;
import no.javatime.inplace.region.intface.BundleTransition.Transition;
import no.javatime.inplace.region.intface.DuplicateBundleException;
import no.javatime.inplace.region.intface.InPlaceException;
import no.javatime.inplace.region.intface.ProjectLocationException;
import no.javatime.inplace.region.status.BundleStatus;
import no.javatime.inplace.region.status.IBundleStatus;
import no.javatime.inplace.region.status.IBundleStatus.StatusCode;
import no.javatime.util.messages.ErrorMessage;
import no.javatime.util.messages.ExceptionMessage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/**
 * Container class for bundle status objects added during a bundle job. A status object contains a
 * status code and one or more elements of type exception, message, project and bundle.
 * 
 * @see no.javatime.inplace.region.status.IBundleStatus
 */
public class JobStatus extends WorkspaceJob implements BundleTransitionEventListener {

	/**
	 * Convenience references to bundle management
	 */
	protected BundleCommand bundleCommand;
	protected BundleTransition bundleTransition;
	protected BundleRegion bundleRegion;

	protected BundleProjectCandidates bundleProjectCandidates;
	protected BundleProjectMeta bundleProjectMeta;
	protected MessageOptions messageOptions;
	protected CommandOptions commandOptions;

	long startTime;

	// List of error status objects
	private List<IBundleStatus> errStatusList = new ArrayList<>();

	// List of historic status objects
	private List<IBundleStatus> logStatusList = new ArrayList<>();

	/**
	 * Construct a job with the name of the job to run
	 * 
	 * @param name the name of the job to run
	 */
	public JobStatus(String name) {
		super(name);
	}

	/**
	 * Runs the bundle(s) status operation.
	 * <p>
	 * Note that the internal {@code JobManager} class logs status unconditionally to the
	 * {@code LogView} if a job returns a status object with {@code IStatus.ERROR} or
	 * {@code IStatus.WARNING}
	 * 
	 * @return a {@code BundleStatus} object with {@code BundleStatusCode.OK} if job terminated
	 * normally and no status objects have been added to this job status list, or
	 * {@code BundleStatusCode.JOBINFO} if any status objects have been added to the job status list.
	 * The status list may be obtained from this job by accessing {@linkplain #getErrorStatusList()}.
	 * @throws ExtenderException If failing to obtain any of the bundle command, transition region,
	 * candidates services or the project meta and message options service
	 */
	@Override
	public IBundleStatus runInWorkspace(IProgressMonitor monitor) throws CoreException,
			ExtenderException {

		initServices();
		startTime = System.currentTimeMillis();
		return new BundleStatus(StatusCode.OK, Activator.PLUGIN_ID, null);
	}

	/**
	 * The initialization of services are delayed until bundle jobs are scheduled. Exceptions are when
	 * member methods in job interface services are accessed prior to scheduling of a bundle job
	 * <p>
	 * The initialization is conditional to reduce the number of get service calls for the different
	 * services
	 * 
	 * @throws ExtenderException failing to get any of the services used by bundle jobs
	 */
	protected void initServices() throws ExtenderException {

		if (null == bundleCommand) {
			bundleCommand = Activator.getBundleCommandService();
		}
		if (null == bundleTransition) {
			bundleTransition = Activator.getBundleTransitionService();
		}
		if (null == bundleRegion) {
			bundleRegion = Activator.getBundleRegionService();
		}
		if (null == bundleProjectCandidates) {
			bundleProjectCandidates = Activator.getBundleProjectCandidatesService();
		}
		if (null == bundleProjectMeta) {
			bundleProjectMeta = Activator.getbundlePrrojectMetaService();
		}
		if (null == messageOptions) {
			messageOptions = Activator.getMessageOptionsService();
		}
		if (null == commandOptions) {
			commandOptions = Activator.getCommandOptionsService();
		}
	}

	public long getStartedTime() {
		return startTime;
	}

	/**
	 * Get the status of a bundle job so far. If the job has generated any errors or warnings a bundle
	 * status object with {@code StatucCode.JOBINFO} is returned otherwise a status object with
	 * {@code StatusCode.OK} is returned. The message in the returned status object contains the name
	 * of the job.
	 * <p>
	 * Generated errors and warnings so far may be obtained from {@link #getErrorStatusList()}
	 * 
	 * @return A bundle status object describing the status of the a bundle job so far
	 */
	protected IBundleStatus getJobSatus() {

		StatusCode statusCode = hasErrorStatus() ? StatusCode.JOBINFO : StatusCode.OK;
		return new BundleStatus(statusCode, Activator.PLUGIN_ID, getName());
	}

	/**
	 * Add log status messages to this job according to transition type
	 */
	@Override
	public void bundleTransitionChanged(BundleTransitionEvent event) {

		if (!messageOptions.isBundleOperations()) {
			return;
		}
		Bundle bundle = event.getBundle();
		Transition transition = event.getTransition();
		IProject project = event.getProject();
		String bundleClassPath = null;

		try {
			switch (transition) {
			case RESOLVE:
				addLogStatus(Msg.RESOLVE_BUNDLE_OP_TRACE, new Object[] { bundle }, bundle);
				break;
			case UNRESOLVE:
				addLogStatus(Msg.UNRESOLVE_BUNDLE_OP_TRACE, new Object[] { bundle }, bundle);
				break;
			case UPDATE:
				addLogStatus(Msg.UPDATE_BUNDLE_OP_TRACE, new Object[] { bundle }, bundle);
				break;
			case REFRESH:
				addLogStatus(Msg.REFRESH_BUNDLE_OP_TRACE, new Object[] { bundle }, bundle);
				break;
			case START:
				if (bundleProjectMeta.getCachedActivationPolicy(bundle)) {
					addLogStatus(Msg.ON_DEMAND_BUNDLE_START_OP_TRACE, new Object[] { bundle }, bundle);
				} else {
					addLogStatus(
							Msg.START_BUNDLE_OP_TRACE,
							new Object[] { bundle, new DecimalFormat().format(bundleCommand.getExecutionTime()) },
							bundle);
				}
				break;
			case STOP:
				addLogStatus(Msg.STOP_BUNDLE_OP_TRACE,
						new Object[] { bundle, new DecimalFormat().format(bundleCommand.getExecutionTime()) },
						bundle);
				break;
			case UNINSTALL:
				String locUninstMsg = NLS.bind(Msg.BUNDLE_LOCATION_TRACE, bundle.getLocation());
				IBundleStatus uninstStatus = new BundleStatus(StatusCode.INFO, bundle, project,
						locUninstMsg, null);
				String uninstMsg = NLS.bind(Msg.UNINSTALL_BUNDLE_OP_TRACE,
						new Object[] { bundle.getSymbolicName(), bundle.getBundleId() });
				IBundleStatus multiUninstStatus = new BundleStatus(StatusCode.INFO, bundle, project,
						uninstMsg, null);
				multiUninstStatus.add(uninstStatus);
				addLogStatus(multiUninstStatus);
				break;
			case INSTALL:
				// If null, the bundle project probably failed to install
				if (null != bundle) {
					String locInstMsg = NLS.bind(Msg.BUNDLE_LOCATION_TRACE, bundle.getLocation());
					IBundleStatus instStatus = new BundleStatus(StatusCode.INFO, bundle, project, locInstMsg,
							null);
					String instMsg = NLS.bind(Msg.INSTALL_BUNDLE_OP_TRACE,
							new Object[] { bundle.getSymbolicName(), bundle.getBundleId() });
					IBundleStatus multiInstStatus = new BundleStatus(StatusCode.INFO, bundle, project,
							instMsg, null);
					multiInstStatus.add(instStatus);
					addLogStatus(multiInstStatus);
				}
				break;
			case LAZY_ACTIVATE:
				addLogStatus(Msg.LAZY_ACTIVATE_BUNDLE_OP_TRACE,
						new Object[] { bundle, bundleCommand.getStateName(bundle) }, bundle);
				break;
			case UPDATE_CLASSPATH:
				bundleClassPath = bundleProjectMeta.getBundleClassPath(project);
				addLogStatus(Msg.UPDATE_BUNDLE_CLASSPATH_TRACE, new Object[] { bundleClassPath,
						bundleProjectMeta.getDefaultOutputFolder(project), project.getName() }, project);
				break;
			case REMOVE_CLASSPATH:
				bundleClassPath = bundleProjectMeta.getBundleClassPath(project);
				if (null == bundleClassPath) {
					addLogStatus(
							Msg.REMOVE_BUNDLE_CLASSPATH_TRACE,
							new Object[] { bundleProjectMeta.getDefaultOutputFolder(project), project.getName() },
							project);
				} else {
					addLogStatus(Msg.REMOVE_BUNDLE_CLASSPATH_ENTRY_TRACE, new Object[] { bundleClassPath,
							bundleProjectMeta.getDefaultOutputFolder(project), project.getName() }, project);
				}
				break;
			case UPDATE_ACTIVATION_POLICY:
				Boolean policy = bundleProjectMeta.getActivationPolicy(project);
				addLogStatus(
						Msg.TOGGLE_ACTIVATION_POLICY_TRACE,
						// Changing from (old policy) to (new policy)
						new Object[] { (policy) ? "eager" : "lazy", (policy) ? "lazy" : "eager",
								project.getName() }, project);
				break;
			case UPDATE_DEV_CLASSPATH:
				String osgiDev = bundleProjectMeta.inDevelopmentMode();
				String symbolicName = bundleProjectMeta.getSymbolicName(project);
				addLogStatus(Msg.CLASS_PATH_COMMON_INFO, new Object[] { osgiDev, symbolicName }, project);
				break;
			case EXTERNAL:
				addLogStatus(Msg.FRAMEWORK_BUNDLE_OP_TRACE, new Object[] { bundle.getSymbolicName(),
						bundleCommand.getStateName(bundle) }, bundle);
				break;
			case REMOVE_PROJECT:
				// Do not test for nature. Project files are not accessible at this point
				String remActivated = bundleRegion.isBundleActivated(project) ? "activated" : "deactivated";
				addLogStatus(Msg.REMOVE_PROJECT_OP_TRACE, new Object[] { remActivated, project.getName() },
						project);
				break;
			case NEW_PROJECT: {
				ActivateProject activate = new ActivateProjectJob();
				String addActivated = activate.isProjectActivated(project) ? "activated" : "deactivated";
				addLogStatus(Msg.ADD_PROJECT_OP_TRACE, new Object[] { addActivated, project.getName() },
						project);
				break;
			}
			default:
				break;
			}
		} catch (InPlaceException e) {
			addLogStatus(new BundleStatus(StatusCode.EXCEPTION, Activator.PLUGIN_ID, project,
					Msg.LOG_TRACE_EXP, e));
		} catch (NullPointerException e) {
			addLogStatus(new BundleStatus(StatusCode.EXCEPTION, Activator.PLUGIN_ID, project,
					Msg.LOG_TRACE_EXP, e));
		}
		// TODO Check and add exceptions to catch
	}

	/**
	 * Adds an error to the status list
	 * 
	 * @param e the exception belonging to the error
	 * @param message an error message
	 * @param project the project the error belongs to
	 * @return the newly created error status object with an exception, a message and a project
	 */
	protected IBundleStatus addError(Throwable e, String message, IProject project) {
		IBundleStatus status = new BundleStatus(StatusCode.ERROR, Activator.PLUGIN_ID, project,
				message, e);
		this.errStatusList.add(status);
		try {
			bundleTransition.setTransitionError(project);
		} catch (ProjectLocationException locEx) {
			errorSettingTransition(project, locEx);
		}
		return status;
	}

	/**
	 * Get all log status objects added by this job
	 * 
	 * @return a list of status log status objects or an empty list
	 * 
	 * @see #addLogStatus(String, Bundle, IProject)
	 * @see #addLogStatus(String, Object[], Object)
	 */
	public Collection<IBundleStatus> getLogStatusList() {
		return logStatusList;
	}

	/**
	 * Creates a bundle log status object and stores it in a log status list
	 * <p>
	 * Either the specified bundle or project may be null, but not both
	 * 
	 * @param message the message part of the created log status object
	 * @param bundle the bundle part of the created log status object
	 * @param project the project part of the created log status object
	 * @return the bundle log status object added to the log status list
	 * @see #addLogStatus(String, Object[], Object)
	 * @see #getLogStatusList()
	 */
	protected IBundleStatus addLogStatus(String message, Bundle bundle, IProject project) {
		IBundleStatus status = new BundleStatus(StatusCode.INFO, bundle, project, message, null);
		this.logStatusList.add(status);
		return status;
	}

	public IBundleStatus addLogStatus(IBundleStatus status) {
		this.logStatusList.add(status);
		return status;
	}

	/**
	 * Creates a bundle log status object and adds it to the bundle log status list
	 * <p>
	 * If the specified bundle project is of type {@code IProject}, its corresponding bundle will be
	 * added to the log status object if it exists and if of type {@code Bundle} its project will be
	 * added.
	 * 
	 * @param key a {@code NLS} identifier
	 * @param substitutions parameters to the {@code NLS} string
	 * @param bundleProject a {@code Bundle} or an {@code IProject}. Must not be null
	 * @see #addLogStatus(String, Bundle, IProject)
	 * @see #getLogStatusList()
	 */
	protected IBundleStatus addLogStatus(String key, Object[] substitutions, Object bundleProject) {
		Bundle bundle = null;
		IProject project = null;
		if (null != bundleProject) {
			if (bundleProject instanceof Bundle) {
				bundle = (Bundle) bundleProject;
				project = bundleRegion.getProject(bundle);
			} else if (bundleProject instanceof IProject) {
				project = (IProject) bundleProject;
				bundle = bundleRegion.getBundle(project);
			}
		}
		return addLogStatus(NLS.bind(key, substitutions), bundle, project);
	}

	/**
	 * Adds an error to the status list
	 * 
	 * @param e the exception belonging to the error
	 * @param message an error message
	 * @param bundle the bundle object the error belongs to
	 * @return the newly created error status object with an exception, a message and a bundle object
	 */
	protected IBundleStatus addError(Throwable e, String message, Bundle bundle) {
		IBundleStatus status = new BundleStatus(StatusCode.ERROR, Activator.PLUGIN_ID, bundle, message,
				e);
		this.errStatusList.add(status);
		try {
			bundleTransition.setTransitionError(bundleRegion.getProject(bundle));
		} catch (ProjectLocationException locEx) {
			errorSettingTransition(bundleRegion.getProject(bundle), locEx);
		}
		return status;
	}

	/**
	 * Adds an error to the status list
	 * 
	 * @param e the exception belonging to the error
	 * @param project the project the error belongs to
	 * @return the newly created error status object with an exception and its project
	 */
	protected IBundleStatus addError(Throwable e, IProject project) {
		IBundleStatus status = new BundleStatus(StatusCode.ERROR, Activator.PLUGIN_ID, project, null, e);
		this.errStatusList.add(status);
		try {
			bundleTransition.setTransitionError(project);
		} catch (ProjectLocationException locEx) {
			errorSettingTransition(project, locEx);
		}
		return status;
	}

	private IBundleStatus errorSettingTransition(IProject project, Exception e) {
		String msg = null;
		if (null == project) {
			msg = ExceptionMessage.getInstance().formatString("project_null_location");
		} else {
			msg = ErrorMessage.getInstance().formatString("project_location", project.getName());
		}
		IBundleStatus status = new BundleStatus(StatusCode.ERROR, Activator.PLUGIN_ID, project, msg, e);
		this.errStatusList.add(status);
		return status;
	}

	/**
	 * Adds an error to the status list
	 * 
	 * @param e the exception belonging to the error
	 * @param message an error message
	 * @return the newly created error status object with an exception and a message
	 */
	protected IBundleStatus addError(Throwable e, String message) {
		IBundleStatus status = new BundleStatus(StatusCode.ERROR, Activator.PLUGIN_ID, message, e);
		this.errStatusList.add(status);
		return status;
	}

	/**
	 * Adds an information status object to the status list
	 * 
	 * @param message an information message
	 * @return the newly created information status object with a message
	 */
	protected IBundleStatus addInfoMessage(String message) {
		IBundleStatus status = new BundleStatus(StatusCode.INFO, Activator.PLUGIN_ID, message, null);
		this.errStatusList.add(status);
		return status;
	}

	protected IBundleStatus addInfoMessage(String message, IProject project) {
		IBundleStatus status = new BundleStatus(StatusCode.INFO, Activator.PLUGIN_ID, project, message,
				null);
		this.errStatusList.add(status);
		return status;
	}

	/**
	 * Adds an cancel status object to the status list
	 * 
	 * @param e the cancel exception belonging to the cancel message
	 * @param message an canceling message
	 * @return the newly created canceling status object with a message
	 */
	protected IBundleStatus addCancelMessage(OperationCanceledException e, String message) {
		IBundleStatus status = new BundleStatus(StatusCode.CANCEL, Activator.PLUGIN_ID, message, e);
		this.errStatusList.add(status);
		return status;
	}

	/**
	 * Adds a build error to the status list
	 * 
	 * @param msg an error message
	 * @param project the project the build error belongs to
	 * @return the newly created error status object with a build error message and the project the
	 * build error belongs to.
	 */
	protected IBundleStatus addBuildError(String msg, IProject project) {
		IBundleStatus status = new BundleStatus(StatusCode.BUILDERROR, Activator.PLUGIN_ID, project,
				msg, null);
		this.errStatusList.add(status);
		return status;
	}

	/**
	 * Adds a warning to the status list
	 * 
	 * @param e the exception belonging to the warning
	 * @param message a warning message
	 * @param project the project the warning belongs to
	 * @return the newly created warning status object with an exception, a message and a project
	 */
	protected IBundleStatus addWarning(Throwable e, String message, IProject project) {
		IBundleStatus status = new BundleStatus(StatusCode.WARNING, Activator.PLUGIN_ID, project,
				message, e);
		this.errStatusList.add(status);
		return status;
	}

	/**
	 * Adds a status object to the error status list
	 * 
	 * @param status the status object to add to the list
	 * @return the added status object
	 */
	public IBundleStatus addStatus(IBundleStatus status) {
		this.errStatusList.add(status);
		return status;
	}

	/**
	 * Adds a list of status objects to the status list
	 * 
	 * @param statusList the list of status objects to add to the list
	 */
	protected void addStatus(Collection<IBundleStatus> statusList) {
		this.errStatusList.addAll(statusList);
	}

	public IBundleStatus createMultiStatus(IBundleStatus multiStatus) {
		for (IBundleStatus status : getErrorStatusList()) {
			multiStatus.add(status);
		}
		clearErrorStatusList();
		return addStatus(multiStatus);
	}

	/**
	 * Creates a multi status object with the specified parent as the root status and all status
	 * objects added to the status list before and including the specified child object. The child
	 * status object must exist in the status list. The created multi status object replace all child
	 * status objects in the status list
	 * 
	 * @param parent this is the parent status object
	 * @param child this is the first child
	 * @return the newly created multi status object
	 */
	protected IBundleStatus createMultiStatus(IBundleStatus parent, IBundleStatus child) {
		int startIndex = 0;
		if (null == child || errStatusList.size() == 0) {
			String msg = ErrorMessage.getInstance().formatString("failed_to_format_multi_status");
			addError(null, msg);
		} else {
			startIndex = errStatusList.indexOf(child);
			if (-1 == startIndex) {
				startIndex = 0;
			}
		}
		for (int i = errStatusList.size() - 1; i >= startIndex; i--) {
			parent.add(errStatusList.get(i));
		}
		IStatus[] is = parent.getChildren();
		for (int i = 0; i < is.length; i++) {
			errStatusList.remove(is[i]);
		}
		return addStatus(parent);
	}

	/**
	 * Creates a new status object with {@code StatusCode#OK}. The created status object is not added
	 * to the status list
	 * 
	 * @return the new status object
	 */
	protected IBundleStatus createStatus() {
		return new BundleStatus(StatusCode.OK, Activator.PLUGIN_ID, "");
	}

	/**
	 * Get all status information added by this job
	 * 
	 * @return a list of status objects where each status object describes the nature of the status or
	 * an empty list
	 */
	public Collection<IBundleStatus> getErrorStatusList() {
		return errStatusList;
	}

	/**
	 * Number of status elements registered
	 * 
	 * @return number of status elements
	 */
	protected int errorStatusList() {
		return errStatusList.size();
	}

	/**
	 * Get the last added bundle status object added by this job
	 * 
	 * @return a the last added bundle status with added by this job or a status object with
	 * {@code StatusCode} = OK if the list is empty
	 */
	protected IBundleStatus getLastErrorStatus() {

		return hasErrorStatus() ? errStatusList.get(errStatusList.size() - 1) : createStatus();
	}

	/**
	 * Check if any bundle status objects have been added to the error list
	 * 
	 * @return true if bundle error status objects exists in the error status list, otherwise false
	 */
	public boolean hasErrorStatus() {
		return (errStatusList.size() > 0 ? true : false);
	}

	/**
	 * Removes all status objects from the status list
	 */
	private void clearErrorStatusList() {
		this.errStatusList.clear();
	}

	@SuppressWarnings("unused")
	private IBundleStatus formateBundleStatus(Collection<IBundleStatus> statusList, String rootMessage) {
		ProjectSorter bs = new ProjectSorter();
		Collection<IProject> duplicateClosureSet = null;
		IBundleStatus rootStatus = new BundleStatus(StatusCode.EXCEPTION, Activator.PLUGIN_ID,
				rootMessage);
		for (IBundleStatus bundleStatus : statusList) {
			IProject project = bundleStatus.getProject();
			Throwable e = bundleStatus.getException();
			if (null != e && e instanceof DuplicateBundleException) {
				if (null != project) {
					duplicateClosureSet = bs.sortRequiringProjects(Collections.singleton(project));
					duplicateClosureSet.remove(project);
					if (duplicateClosureSet.size() > 0) {
						String msg = ErrorMessage.getInstance().formatString("duplicate_affected_bundles",
								project.getName(), bundleProjectCandidates.formatProjectList(duplicateClosureSet));
						rootStatus.add(new BundleStatus(StatusCode.INFO, Activator.PLUGIN_ID, msg));
					}
				}
			}
			rootStatus.add(bundleStatus);
		}
		return rootStatus;
	}
}
