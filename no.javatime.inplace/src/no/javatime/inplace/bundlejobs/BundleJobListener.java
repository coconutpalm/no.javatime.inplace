/*******************************************************************************
 * Copyright (c) 2011, 2012 JavaTime project and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	JavaTime project, Eirik Gronsund - initial implementation
 *******************************************************************************/
package no.javatime.inplace.bundlejobs;

import java.util.ArrayList;
import java.util.Collection;

import no.javatime.inplace.InPlace;
import no.javatime.inplace.bundle.log.status.BundleStatus;
import no.javatime.inplace.bundle.log.status.IBundleStatus;
import no.javatime.inplace.bundle.log.status.IBundleStatus.StatusCode;
import no.javatime.inplace.bundlemanager.BundleManager;
import no.javatime.inplace.bundlemanager.BundleTransition;
import no.javatime.inplace.bundlemanager.BundleTransition.Transition;
import no.javatime.inplace.bundleproject.ProjectProperties;
import no.javatime.util.messages.Category;
import no.javatime.util.messages.Message;
import no.javatime.util.messages.WarnMessage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Listen to state changes and report job status in bundle jobs. Bundle status objects added by bundle
 * jobs are reported by this bundle job  listener when jobs are done.
 * @see no.javatime.inplace.bundle.log.status.IBundleStatus
 * @see no.javatime.inplace.bundlejobs.JobStatus   
 */
public class BundleJobListener extends JobChangeAdapter {
	
	/**
	 * Default constructor for jobs
	 */
	public BundleJobListener() {
  	super();
	}

	/**
	 * Calculate and set flush buffer for messages generated by this bundle job.
	 */
	@Override
	public void aboutToRun(IJobChangeEvent event) {
		Job job = event.getJob();
		if (job instanceof BundleJob) {
			// This does not take dependent projects into account
//			int pendingProjects = ((BundleJob) job).pendingProjects();
//			Message.getInstance().setFlushInterval(pendingProjects*5);
		}
	}

	/**
	 * If the option to report on bundle operations is true, a message that
	 * the job has started is forwarded.
	 */
	@Override
	public void running(IJobChangeEvent event) {
		Job job = event.getJob();
		if (job instanceof BundleJob) {
//			if (InPlace.get().msgOpt().isBundleOperations()) {
//				InPlace.get().trace("start_job", job.getName());
//			}
		}
	}
	
	/**
	 * Signal that a bundle job is rescheduled, after waiting for another job.
	 */
	@Override
	public void awake(IJobChangeEvent event) {
		Job job = event.getJob();
		if (job instanceof BundleJob) {
//			if (InPlace.get().msgOpt().isBundleOperations()) {
//				InPlace.get().trace("awake_job", job.getName());
//			}
		}
	}

	/**
	 * Signal that a bundle job is scheduled, but is waiting for another job.
	 */
	@Override
	public void sleeping(IJobChangeEvent event) {
		Job job = event.getJob();
		if (job instanceof BundleJob) {
//			if (InPlace.get().msgOpt().isBundleOperations()) {
//				InPlace.get().trace("sleep_job", job.getName());
//			}
		}
	}
	
	/**
	 * Report messages, exceptions, errors and warnings added by bundle jobs.
	 */
	@Override
	public void done(IJobChangeEvent event) {
		Job job = event.getJob();
		if (job instanceof BundleJob) {
			Message.getInstance().flush();
			BundleJob bundleJob = (BundleJob) job;
			if (InPlace.get().msgOpt().isBundleOperations()) {
				IBundleStatus mStatus = new BundleStatus(StatusCode.INFO, InPlace.PLUGIN_ID, bundleJob.getName());
				for (IBundleStatus status : bundleJob.getTraceList()) {
					mStatus.add(status);
				}
				// TODO run in separate thread
				InPlace.get().trace(mStatus);
			}			
			Collection<IBundleStatus> statusList = logCancelStatus(bundleJob);	
			if (statusList.size() > 0) {
				String rootMsg = WarnMessage.getInstance().formatString("end_job_root_message", bundleJob.getName());
				IBundleStatus multiStatus = bundleJob.createMultiStatus(new BundleStatus(StatusCode.ERROR,
						InPlace.PLUGIN_ID, rootMsg));
				StatusManager.getManager().handle(multiStatus, StatusManager.LOG);
			}
			if (InPlace.get().msgOpt().isBundleOperations()) {
				if (Category.DEBUG)
					getBundlesJobRunState(bundleJob);
//				InPlace.get().trace("end_job", job.getName());					
			}
			schedulePendingOperations(bundleJob);
		}
	}
	
	private void schedulePendingOperations(BundleJob job) {

		BundleTransition bundleTransition = BundleManager.getTransition();
		RefreshJob refreshJob = null;
		DeactivateJob deactivateJob = null;
		Collection<IProject> activatedProjects = ProjectProperties.getActivatedProjects();
		Collection<IProject> projectsToRefresh = 
				bundleTransition.getPendingProjects(activatedProjects, Transition.REFRESH);
		if (projectsToRefresh.size() > 0) {
			refreshJob = new RefreshJob(RefreshJob.refreshJobName);			
			refreshJob.addPendingProjects(projectsToRefresh);
			bundleTransition.removePending(projectsToRefresh, Transition.REFRESH);
			BundleManager.addBundleJob(refreshJob, 0);
		}
		Collection<IProject> projectsToDeactivate = 
				bundleTransition.getPendingProjects(activatedProjects, Transition.DEACTIVATE);
		if (projectsToDeactivate.size() > 0) {
			deactivateJob = new DeactivateJob(DeactivateJob.deactivateJobName);
			deactivateJob.addPendingProjects(projectsToDeactivate);
			bundleTransition.removePending(projectsToDeactivate, Transition.DEACTIVATE);
			BundleManager.addBundleJob(deactivateJob, 0);
		}
	}
	
	/**
	 * If the bundle job has been cancelled, log it
	 * @param bundleJob the job that may contain a cancel status
	 * 
	 * @return a copy of the status list with the cancel status removed from the status list or a copy of
	 * the status list if it does not contain any cancel status 
	 */
	private Collection<IBundleStatus> logCancelStatus(BundleJob bundleJob) {

		Collection<IBundleStatus> statusList = bundleJob.getStatusList();
		Collection<IBundleStatus> modifiedStatusList = new ArrayList<IBundleStatus>(statusList);
		for (IBundleStatus status : statusList) {
			if (status.hasStatus(StatusCode.CANCEL)) {
				StatusManager.getManager().handle(status, StatusManager.LOG);
				modifiedStatusList.remove(status);
				return modifiedStatusList;
			}
		}
		return statusList;
	}
	
	/**
	 * Trace that a bundle job is running if the job is in state {@code Job.RUNNING}.
	 * @param bundleJob the bundle job to trace
	 */
	private void getBundlesJobRunState(BundleJob bundleJob) {
		IJobManager jobMan = Job.getJobManager();
		Job[] jobs = jobMan.find(BundleJob.FAMILY_BUNDLE_LIFECYCLE); 
		for (int i = 0; i < jobs.length; i++) {
			Job job = jobs[i];
			if (job.getState() == Job.RUNNING) {
				InPlace.get().trace("running_jobs", job.getName(), bundleJob.getName());
			}
		}
	}
}
