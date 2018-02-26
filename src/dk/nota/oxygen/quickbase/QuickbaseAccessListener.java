package dk.nota.oxygen.quickbase;

public interface QuickbaseAccessListener {
	
	public void connected(QuickbaseAccess quickbaseAccess);
	
	public void disconnected(QuickbaseAccess quickbaseAccess);

}
