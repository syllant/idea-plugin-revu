package org.sylfra.idea.plugins.revu.ui.actions.filter;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Filter;

import javax.swing.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
public class CreateFilterAction extends AbstractFilterSettingsAction
{
  public void actionPerformed(AnActionEvent e)
  {
    JList list = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
    Project project = e.getData(DataKeys.PROJECT);

    String name = Messages.showInputDialog(project, RevuBundle.message("filterSettings.create.text"),
       RevuBundle.message("filterSettings.create.title"),
      Messages.getInformationIcon(), "Unnamed", new NonEmptyInputValidator());

    if (name == null)
    {
      return;
    }

    Filter filter = new Filter();
    filter.setName(name);

    DefaultListModel model = (DefaultListModel) list.getModel();
    model.addElement(filter);
    list.setSelectedValue(filter, true);
  }
}
