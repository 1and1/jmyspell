package org.dts.spell.event;

import org.dts.spell.utils.EventMulticaster;

public class ProgressListenerSupport implements ProgressListener {

    private EventMulticaster<ProgressListener> internalListeners = new EventMulticaster<ProgressListener>(ProgressListener.class);

    public ProgressListenerSupport() {
    }

    public void addListener(ProgressListener listener) {
        internalListeners.addListener(listener);
    }

    public void removeListener(ProgressListener listener) {
        internalListeners.removeListener(listener);
    }

  public void beginProgress(ProgressEvent evt) {
    internalListeners.getMulticaster().beginProgress(evt);
  }

  public void nextStep(ProgressEvent evt) {
    internalListeners.getMulticaster().nextStep(evt);
  }

  public void endProgress(ProgressEvent evt) {
    internalListeners.getMulticaster().endProgress(evt);
  }
}
