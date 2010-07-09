package org.imirsel.nema.flowservice.config;

/**
 * Interface for objects to listen for configuration changes.
 *  
 * @author shirk
 */
public interface ConfigChangeListener {
   
   /**
    * Handle an updated {@link FlowServiceConfig}.
    */
   public void configChanged();
}
