package org.sylfra.idea.plugins.revu.ui.actions.filter;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Filter;

import javax.swing.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
public class RemoveFilterAction extends AbstractFilterSettingsAction
{
  public void actionPerformed(AnActionEvent e)
  {
    JList list = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
    DefaultListModel model = (DefaultListModel) list.getModel();
    Filter selectedFilter = (Filter) list.getSelectedValue();

    int result = Messages.showOkCancelDialog(list,
      RevuBundle.message("filterSettings.confirmRemove.text", selectedFilter.getName()),
      RevuBundle.message("filterSettings.confirmRemove.title"),
      Messages.getWarningIcon());

    if (result == DialogWrapper.OK_EXIT_CODE)
    {
      model.removeElement(selectedFilter);
      if (model.getSize() > 0)
      {
        list.setSelectedIndex(0);
      }
    }
  }
}