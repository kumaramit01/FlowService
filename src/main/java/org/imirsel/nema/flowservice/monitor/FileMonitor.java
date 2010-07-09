package org.imirsel.nema.flowservice.monitor;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Monitors a file on the file system for changes.
 * 
 * @author shirk
 * @since 0.7.0
 */
public class FileMonitor {
   
   private static Logger logger = Logger.getLogger(FileMonitor.class
         .getName());
   
	private ScheduledExecutorService executorService = Executors
   .newSingleThreadScheduledExecutor();

	private FileChangeListener listener;
	private File file;
	private long interval;

	/**
	 * Creates a new {@link FileMonitor} instance by specifying the path to
	 * the file to monitor.
	 * 
	 * @param listener
	 * @param filePath
	 * @param interval
	 */
	public FileMonitor(FileChangeListener listener, String filePath, long interval) {
		this(listener,new File(filePath),interval);
	}

	/**
    * Creates a new {@link FileMonitor} instance by specifying the {@code File}
    * to monitor.
    * 
    * @param listener
    * @param filePath
    * @param interval
    */
   public FileMonitor(FileChangeListener handler, File file, long interval) {
      this.listener = handler;
      this.file = file;
      this.interval = interval;
   }
   
	/**
	 * Start the file monitor.
	 */
	public void start() throws IOException {
		FileChangeDetector fileChangeDetector = new FileChangeDetector();
		executorService.scheduleWithFixedDelay(fileChangeDetector, 0,
				interval, TimeUnit.SECONDS);
		logger.info("Starting to monitor file " + file.getName() + 
		      " for changes.");
	}

	/** 
	 * Main, for testing purposes.
	 * 
	 * @param args Main process arguments.
	 */
	public static void main(String args[]) {
		FileChangeListener listener = new FileChangeListener() {
         Logger logger = Logger.getAnonymousLogger();
		   @Override
         public void fileChanged(File file) {
           logger.info("File changed.");
         }
		};
		FileMonitor monitor = new FileMonitor(
				listener, "src/test/resources/flowservice.properties", 2);
		try {
         monitor.start();
      } catch (IOException e) {
         e.printStackTrace();
      }
	}

	/**
	 * Runs to check to see if the file has been updated.
	 */
	private class FileChangeDetector implements Runnable {
      private long lastModified;
      
	   public void run() {
	      long lastModifiedCheck = file.lastModified();
	      if(lastModified==0) {
	         lastModified = lastModifiedCheck;
	         return;
	      }
	      if(lastModified != lastModifiedCheck) {
	         try {
               listener.fileChanged(file);
            } catch (Exception e) {
               logger.warning("Uncaught exception in FileMonitor: " + 
                     e.getMessage());
            }
            lastModified = lastModifiedCheck;
	      }
	   }
	}
}


