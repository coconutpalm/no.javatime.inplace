package no.javatime.inplace.builder;

import java.util.Collection;
import java.util.LinkedHashSet;

import no.javatime.inplace.InPlace;
import no.javatime.inplace.bundlejobs.NatureJob;
import no.javatime.inplace.dl.preferences.intface.DependencyOptions.Closure;
import no.javatime.inplace.region.closure.BundleClosures;
import no.javatime.inplace.region.closure.CircularReferenceException;
import no.javatime.inplace.region.manager.BundleManager;
import no.javatime.inplace.region.manager.BundleTransition.Transition;
import no.javatime.inplace.region.manager.InPlaceException;
import no.javatime.inplace.region.status.BundleStatus;
import no.javatime.inplace.region.status.IBundleStatus;
import no.javatime.inplace.region.status.IBundleStatus.StatusCode;
import no.javatime.util.messages.ErrorMessage;
import no.javatime.util.messages.ExceptionMessage;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.osgi.framework.Bundle;

/**
 * Uninstall all activated bundle projects that have been removed (closed or deleted) from the
 * workspace. Removed bundle projects should be added as pending projects to this job before scheduling the job.
 * <p>
 * When removing bundle projects with requiring bundles the requiring closure set becomes incomplete.
 * This inconsistency is solved by deactivating the requiring bundles in the closure before uninstalling.
 */
public class RemoveBundleProjectJob extends NatureJob {

	public RemoveBundleProjectJob(String name) {
		super(name);
	}

	/**
	 * Constructs a removal job with a given job name and pending bundle projects to remove
	 * 
	 * @param name job name
	 * @param projects pending projects to remove
	 */
	public RemoveBundleProjectJob(String name, Collection<IProject> projects) {
		super(name, projects);
	}

	/**
	 * Constructs an removal job with a given job name and a pending bundle project to remove
	 * 
	 * @param name job name
	 * @param project pending project to remove
	 */
	public RemoveBundleProjectJob(String name, IProject project) {
		super(name, project);
	}

	@Override
	public IBundleStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {

		try {
			BundleManager.addBundleTransitionListener(this);
			Collection<Bundle> pendingBundles = bundleRegion.getBundles(getPendingProjects());
			if (pendingBundles.size() == 0) {
					return super.runInWorkspace(monitor);
			}
			BundleClosures closure = new BundleClosures();
			Collection<Bundle> activatedBundles = bundleRegion.getActivatedBundles();
			// Ensure requiring closure by overriding the current closure preferences
			final Collection<Bundle> reqClosure = closure.bundleDeactivation(Closure.REQUIRING, pendingBundles,
					activatedBundles);
			stop(reqClosure, null, new SubProgressMonitor(monitor, 1));
			// Deactivate the requiring projects (those which have not been removed) in the closure
			Collection<IProject> reqProjects = new LinkedHashSet<IProject>(
					bundleRegion.getBundleProjects(reqClosure));
			reqProjects.removeAll(getPendingProjects());
			deactivateNature(reqProjects, new SubProgressMonitor(monitor, 1));
			// The deactivated projects are excluded from the uninstall (or requiring) closure, but are
			// by definition members in the closure set.
			// Uninstall cause the resolver to unresolve and initiate an unresolve event for all projects
			// being member of a defined requiring closure(in this case uninstalled projects and their
			// requiring projects). Inform others by adding a pending unresolve transition on each bundle excluded
			// from the requiring closure
			for (IProject reqProject : reqProjects) {
				bundleTransition.addPending(reqProject, Transition.UNRESOLVE);
			}
			// Uninstall, refresh and unregister the removed projects
			uninstall(pendingBundles, new SubProgressMonitor(monitor, 1), true);
			// Also refresh the deactivated bundles
			refresh(bundleRegion.getBundles(reqProjects), new SubProgressMonitor(monitor, 1));
			return super.runInWorkspace(monitor);
		} catch (InterruptedException e) {
			String msg = ExceptionMessage.getInstance().formatString("interrupt_job", getName());
			addError(e, msg);
		} catch (CircularReferenceException e) {
			String msg = ExceptionMessage.getInstance().formatString("circular_reference", getName());
			BundleStatus multiStatus = new BundleStatus(StatusCode.EXCEPTION, InPlace.PLUGIN_ID, msg);
			multiStatus.add(e.getStatusList());
			addStatus(multiStatus);
		} catch (InPlaceException e) {
			String msg = ExceptionMessage.getInstance().formatString("terminate_job_with_errors",
					getName());
			addError(e, msg);
		} catch (CoreException e) {
			String msg = ErrorMessage.getInstance().formatString("error_end_job", getName());
			return new BundleStatus(StatusCode.ERROR, InPlace.PLUGIN_ID, msg, e);
		} finally {
			BundleManager.removeBundleTransitionListener(this);
		}
		try {
			BundleManager.addBundleTransitionListener(this);
			return super.runInWorkspace(monitor);
		} catch (CoreException e) {
			String msg = ErrorMessage.getInstance().formatString("error_end_job", getName());
			return new BundleStatus(StatusCode.ERROR, InPlace.PLUGIN_ID, msg, e);
		} finally {
			BundleManager.removeBundleTransitionListener(this);
		}
	}
}