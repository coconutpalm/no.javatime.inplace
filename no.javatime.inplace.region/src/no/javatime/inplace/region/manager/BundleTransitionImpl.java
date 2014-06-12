package no.javatime.inplace.region.manager;

import java.util.Collection;
import java.util.EnumSet;

import no.javatime.inplace.region.state.BundleNode;

import org.eclipse.core.resources.IProject;
import org.osgi.framework.Bundle;

public class BundleTransitionImpl implements BundleTransition {

	public final static BundleTransitionImpl INSTANCE = new BundleTransitionImpl();

	private BundleWorkspaceImpl ws = BundleWorkspaceImpl.INSTANCE;

	public BundleTransitionImpl() {
	}

	@Override
	public Transition getTransition(IProject project) throws ProjectLocationException {
		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return Transition.NOTRANSITION;
		}
		return bn.getTransition();
	}

	@Override
	public String getTransitionName(IProject project) throws ProjectLocationException {
		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return BundleNode.getTransitionName(Transition.NOTRANSITION);
		}
		return BundleNode.getTransitionName(bn.getTransition());
	}

	@Override
	public String getTransitionName(Transition transition) {
		return BundleNode.getTransitionName(transition);
	}

	@Override
	public boolean setTransitionError(IProject project) throws ProjectLocationException {
		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return false;
		}		
		return bn.setTransitionError(TransitionError.ERROR);
	}

	public boolean setTransitionError(IProject project, TransitionError error) throws ProjectLocationException {

		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return false;
		}		
		return bn.setTransitionError(error);
	}
	
	@Override
	public boolean setTransitionError(Bundle bundle) {
		if (null == bundle) {
			return false;
		}
		BundleNode bn = ws.getBundleNode(bundle);
		return bn.setTransitionError(TransitionError.ERROR);
	}

	public boolean setTransitionError(Bundle bundle, TransitionError error) {
		if (null == bundle) {
			return false;
		}
		BundleNode bn = ws.getBundleNode(bundle);
		return bn.setTransitionError(error);
	}
	
	@Override
	public boolean hasTransitionError(IProject project) throws ProjectLocationException {
		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return false;
		}
		return bn.hasTransitionError();
	}

	@Override
	public boolean hasTransitionError(Bundle bundle) {
		BundleNode bn = ws.getBundleNode(bundle);
		if (null == bn) {
			return false;
		}
		return bn.hasTransitionError();
	}

	@Override
	public boolean hasTransitionError(TransitionError transitionError) {
		for (IProject project : ws.getBundleProjects()) {
			BundleNode bn = ws.getBundleNode(project);
			if  (bn.hasTransitionError()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public TransitionError getError(IProject project) throws ProjectLocationException {
		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return TransitionError.NOERROR;
		}		
		return bn.getTransitionError();
	}

	@Override
	public TransitionError getError(Bundle bundle) throws ProjectLocationException {

		if (null == bundle) {
			return TransitionError.NOERROR;
		}
		BundleNode bn = ws.getBundleNode(bundle);
		return bn.getTransitionError();
	}
	
	@Override
	public boolean clearTransitionError(IProject project) throws ProjectLocationException {

		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return false;
		}		
		return bn.clearTransitionError();
	}

	@Override
	public boolean removeTransitionError(IProject project, TransitionError transitionError) throws ProjectLocationException {
		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return false;
		}		
		return bn.removeTransitionError(transitionError);
	}
	
	@Override
	public void removeTransitionError(TransitionError transitionError) throws ProjectLocationException {
		for (IProject project : ws.getBundleProjects()) {
			BundleNode bn = ws.getBundleNode(project);
			if (null == bn) {
				continue;
			}		
			bn.removeTransitionError(transitionError);			
		}
	}

	@Override
	public boolean removeTransitionError(Bundle bundle, TransitionError transitionError) {

		if (null == bundle) {
			return false;
		}
		BundleNode bn = ws.getBundleNode(bundle);
		return bn.removeTransitionError(transitionError);
	}

//	Collection<Bundle> removeTransitionErrorClosures(Collection<Bundle> initialBundleSet,
//			Collection<Bundle> bDepClosures, Collection<IProject> pDepClosures) {
//
//		Collection<Bundle> bErrorDepClosures = null;
//		Collection<Bundle> errorBundles = new ArrayList<Bundle>();
//		for (Bundle errorBundle : initialBundleSet) {
//			IProject errorProject = BundleManager.getRegion().getProject(errorBundle);
//			TransitionError transitionError = getError(errorBundle);
//			if (null != errorProject && ( transitionError == TransitionError.DUPLICATE 
//					|| transitionError == TransitionError.CYCLE)) {
//				errorBundles.add(errorBundle);
//			}
//		}
//		if (errorBundles.size() > 0) {
//			// Get all projects with errors and their requiring projects from dependency closures
//			ProjectSorter ps = new ProjectSorter();
//			Collection<IProject> pErrorDepClosures = ps.sortRequiringProjects(BundleManager.getRegion().getProjects(errorBundles));
//			if (pErrorDepClosures.size() > 0) {
//				if (null != pDepClosures) {
//					pDepClosures.removeAll(pErrorDepClosures);
//				}
//				bErrorDepClosures = BundleManager.getRegion().getBundles(pErrorDepClosures);
//				if (bErrorDepClosures.size() > 0) {
//					if (null != bDepClosures) {
//						bDepClosures.removeAll(bErrorDepClosures);
//					}
//					initialBundleSet.removeAll(bErrorDepClosures);
//				}
//			}
//		}
//		return bErrorDepClosures;
//	}

	@Override
	public void initTransition(IProject project) throws ProjectLocationException {
		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return;
		}		
		bn.setTransition(Transition.UNINSTALL);
	}

	@Override
	public Transition setTransition(IProject project, Transition transition) throws ProjectLocationException {
		BundleNode bn = ws.getBundleNode(project);
		if (null == bn) {
			return null;
		}
		bn.clearTransitionError();
		return bn.setTransition(transition);
	}

	@Override
	public Transition setTransition(Bundle bundle, Transition transition) {
		BundleNode bn = ws.getBundleNode(bundle);
		if (null == bn) {
			return null;
		}		
		bn.clearTransitionError();
		return bn.setTransition(transition);
	}
	
	@Override
	public Transition getTransition(Bundle bundle) {
		BundleNode bn = ws.getBundleNode(bundle);
		if (null == bn) {
			return null;
		}		
		return bn.getTransition();
	}

	@Override
	public boolean containsPending(IProject project, Transition operation, boolean remove) {
		return ws.containsPendingCommand(project, operation, remove);
	}

	@Override
	public boolean containsPending(Bundle bundle, Transition operation, boolean remove) {
		return ws.containsPendingCommand(bundle, operation, remove);
	}
	
	@Override
	public boolean containsPending(Transition transition) {
		return ws.containsPendingCommand(transition);
	}
	
	@Override
	public EnumSet<BundleTransition.Transition> getPendingTransitions(IProject project) {
		return ws.getPendingCommands(project);
	}
	
	@Override
	public Collection<IProject> getPendingProjects(Collection<IProject> projects, BundleTransition.Transition command) {
		return ws.getPendingProjects(projects, command);
	}

	@Override
	public void addPending(Bundle bundle, Transition operation) {
	ws.addPendingCommand(bundle, operation);
	}

	@Override
	public void addPending(IProject project, Transition operation) {
		ws.addPendingCommand(project, operation);
	}
	
	@Override
	public boolean removePending(Bundle bundle, Transition operation) {
		return ws.removePendingCommand(bundle, operation);
	}

	@Override
	public boolean removePending(IProject project, Transition operation) {
		return ws.removePendingCommand(project, operation);
	}

	public boolean removePending(Collection<IProject> projects, Transition operation) {
		boolean removed = true;
		for (IProject project : projects) {
			if (!ws.removePendingCommand(project, operation)) {
				removed = false;
			}
		}
		return removed;
	}
}
