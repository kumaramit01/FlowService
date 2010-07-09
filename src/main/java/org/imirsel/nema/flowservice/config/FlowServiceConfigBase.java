package org.imirsel.nema.flowservice.config;

import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class FlowServiceConfigBase implements FlowServiceConfig {
   private Set<ConfigChangeListener> listeners;
   private Lock listenersLock = new ReentrantLock();
   
   @Override
   public void addChangeListener(ConfigChangeListener listener) {
      listenersLock.lock();
      try {
         listeners.add(listener);
      } finally {
         listenersLock.unlock();
      }
   }

   @Override
   public void removeChangeListener(ConfigChangeListener listener) {
      listenersLock.lock();
      try {
         listeners.remove(listener);
      } finally {
         listenersLock.unlock();
      }
   }
   
   protected void notifyListeners() {
      listenersLock.lock();
      try {
         for (ConfigChangeListener listener : listeners) {
            listener.configChanged();
         }
      } finally {
         listenersLock.unlock();
      }
   }
   
}
