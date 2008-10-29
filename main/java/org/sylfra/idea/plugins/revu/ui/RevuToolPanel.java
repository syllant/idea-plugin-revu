package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import org.sylfra.idea.plugins.revu.RevuPlugin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuToolPanel extends JPanel
{
  public RevuToolPanel()
  {
    super(new BorderLayout());

    setBorder(new EmptyBorder(2, 2, 2, 2));

    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance()
      .getAction("revu");
    ActionToolbar toolbar = ActionManager.getInstance()
      .createActionToolbar(RevuPlugin.PLUGIN_NAME, actionGroup, false);

    add(toolbar.getComponent(), BorderLayout.WEST);
  }
}
