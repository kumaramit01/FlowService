package org.imirsel.nema.flowservice.monitor;

import java.io.File;

/**
 * Interface for reacting to the modification of a file.
 * 
 * @author shirk
 * @since 0.7.0
 */
public interface FileChangeListener {

   public void fileChanged(File file);
}
