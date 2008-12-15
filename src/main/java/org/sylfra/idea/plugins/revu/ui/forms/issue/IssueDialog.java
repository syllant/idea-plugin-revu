package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.ui.forms.IUpdatableForm;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueDialog extends DialogWrapper
{
  private IssuePane updateTabbedPane;
  private IssueMainForm createMainForm;
  private CardLayout cardLayout;
  private JPanel centerPanel;
  private IUpdatableForm<Issue> currentForm;
  private final boolean create;

  public IssueDialog(@NotNull Project project, boolean create)
  {
    super(project, true);
    this.create = create;

    updateTabbedPane = new IssuePane(project, null);
    createMainForm = new IssueMainForm(project, true);

    cardLayout = new CardLayout();
    centerPanel = new JPanel(cardLayout);

    setTitle(RevuBundle.message(create ? "dialog.issue.create.title": "dialog.issue.update.title"));
    init();
  }

  @Override
  protected String getDimensionServiceKey()
  {
    return RevuPlugin.PLUGIN_NAME + ".IssueDialog" + (create ? "_create" : "_update");
  }

  protected JComponent createCenterPanel()
  {
    centerPanel.add(updateTabbedPane.getClass().getName(), updateTabbedPane.getContentPane());
    centerPanel.add(createMainForm.getClass().getName(), createMainForm.getContentPane());

    return centerPanel;
  }

  public void show(@NotNull Issue issue)
  {
    currentForm = (create) ? createMainForm : updateTabbedPane;
    currentForm.updateUI(issue.getReview(), issue, true);

    cardLayout.show(centerPanel, currentForm.getClass().getName());

    super.show();
  }

  @Override
  protected void doOKAction()
  {
    if (currentForm.validateInput())
    {
      super.doOKAction();
    }
  }

  @Override
  public JComponent getPreferredFocusedComponent()
  {
    return currentForm.getPreferredFocusedComponent();
  }

  public void updateData(@NotNull Issue issue)
  {
    currentForm.updateData(issue);
  }
}
