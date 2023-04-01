package fileIO.FileListener;

public abstract class FileAdapter implements FileListener {
    public static boolean listenerPerformAction;
    @Override
    public void onCreated(FileEvent event) {
        // default
    }

    @Override
    public void onModified(FileEvent event) {
        // default
    }
        
    @Override
    public void onDeleted(FileEvent event) {
        // default
    }
        
}