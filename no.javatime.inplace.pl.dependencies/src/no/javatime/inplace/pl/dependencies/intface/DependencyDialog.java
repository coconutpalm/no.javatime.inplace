package no.javatime.inplace.pl.dependencies.intface;

public interface DependencyDialog {

	public final static String DEPENDENCY_DIALOG_HEADER = "Dependency-Dialog";

	public int open();
	public boolean close();
}