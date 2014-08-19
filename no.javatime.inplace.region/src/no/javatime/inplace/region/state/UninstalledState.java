package no.javatime.inplace.region.state;

import no.javatime.inplace.region.manager.BundleTransition.Transition;
import no.javatime.inplace.region.manager.InPlaceException;

/**
 * Uninstalled is the initial state for a specified bundle. The only atomic transition is install, which may be initiated by
 * activate and install operations. Refreshing uninstalled bundles has meaning as long there are dependencies on activated
 * bundles. 
 */
public class UninstalledState extends BundleState {

	public UninstalledState() {
		super();
	}

	public void install(BundleNode bundleNode) throws InPlaceException {
		bundleNode.begin(Transition.INSTALL, StateFactory.INSTANCE.installedState);
//		bundleNode.setCurrentState(StateFactory.INSTANCE.installedState);
	}
	
	public void refresh(BundleNode bundleNode) throws InPlaceException {
		bundleNode.begin(Transition.REFRESH, StateFactory.INSTANCE.uninstalledState);
//		bundleNode.setCurrentState(StateFactory.INSTANCE.uninstalledState);
	}
}
