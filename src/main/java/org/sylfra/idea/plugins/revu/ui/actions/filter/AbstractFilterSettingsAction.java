package org.sylfra.idea.plugins.revu.ui.actions.filter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.Filter;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
abstract class AbstractFilterSettingsAction extends AnAction
{
  protected AbstractFilterSettingsAction()
  {
  }

  protected AbstractFilterSettingsAction(String text)
  {
    super(text);
  }

  protected AbstractFilterSettingsAction(String text, String description, Icon icon)
  {
    super(text, description, icon);
  }

  @Override
  public void update(AnActionEvent e)
  {
    Component component = e.getData(DataKeys.CONTEXT_COMPONENT);
    if (!(component instanceof JList))
    {
      return;
    }

    JList list = (JList) component;
    Object selectedValue = list.getSelectedValue();
    if (!(selectedValue instanceof Filter))
    {
      return;
    }

    e.getPresentation().setEnabled(isEnabledForFilter((Filter) selectedValue));
  }

  protected boolean isEnabledForFilter(@NotNull Filter filter)
  {
    return true;
  }
}