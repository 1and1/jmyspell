/*
 * Created on 19/02/2005
 *
 */
package org.dts.spell.swing.utils;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.KeyEventPostProcessor;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * @author DreamTangerine
 */
public class CancelableDialog extends JDialog implements KeyEventPostProcessor {

  private boolean escWasPressed = false;

  @Override
  public void addNotify() {
    super.addNotify();

    KeyboardFocusManager mng = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    mng.addKeyEventPostProcessor(this);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();

    KeyboardFocusManager mng = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    mng.removeKeyEventPostProcessor(this);
  }

  protected boolean hasPressedExitKey(KeyEvent keyEvent) {
    return keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE
            && keyEvent.getID() == KeyEvent.KEY_PRESSED
            && SwingUtilities.getWindowAncestor(keyEvent.getComponent()) == this;
  }

  public boolean postProcessKeyEvent(KeyEvent keyEvent) {

    if (hasPressedExitKey(keyEvent)) {
      escWasPressed = true;
      close();
      return true;
    } else {
      return false;
    }
  }

  private static Frame getCurrentFrame() {
    Frame[] frames = Frame.getFrames();

    if (null == frames || frames.length == 0) {
      return null;
    } else {
      return frames[frames.length - 1];
    }
  }

  /**
   * @throws java.awt.HeadlessException
   */
  public CancelableDialog() throws HeadlessException {
    this(getCurrentFrame());
  }

  /**
   * @param owner
   * @throws java.awt.HeadlessException
   */
  public CancelableDialog(Frame owner) throws HeadlessException {
    this(owner, "");
  }

  /**
   * @param owner
   * @param title
   * @throws java.awt.HeadlessException
   */
  public CancelableDialog(Frame owner, String title) throws HeadlessException {
    super(owner, title, true);
  }

  /**
   * @param owner
   * @throws java.awt.HeadlessException
   */
  public CancelableDialog(Dialog owner) throws HeadlessException {
    this(owner, "");
  }

  /**
   * @param owner
   * @param title
   * @throws java.awt.HeadlessException
   */
  public CancelableDialog(Dialog owner, String title) throws HeadlessException {
    super(owner, title, true);
  }

  /**
   * @param owner
   * @throws java.awt.HeadlessException
   */
  public CancelableDialog(Window owner) throws HeadlessException {
    this(owner, "");
  }

  /**
   * @param owner
   * @param title
   * @throws java.awt.HeadlessException
   */
  public CancelableDialog(Window owner, String title) throws HeadlessException {
    super(owner, title, ModalityType.DOCUMENT_MODAL);
  }

  /**
   * @return the escWasPressed
   */
  public boolean isEscWasPressed() {
    return escWasPressed;
  }

  public void close() {
    setVisible(false);
    dispose();
  }

  public Action getCloseAction() {
    return new AbstractAction("Cerrar") {

      public void actionPerformed(ActionEvent e) {
        close();
      }
    };
  }
}
