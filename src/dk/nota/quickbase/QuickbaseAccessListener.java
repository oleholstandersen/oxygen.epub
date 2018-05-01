package dk.nota.quickbase;

public interface QuickbaseAccessListener {
	
	public void connected(QuickbaseAccess quickbaseAccess);
	
	public void disconnected(QuickbaseAccess quickbaseAccess);

}
