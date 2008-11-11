package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import java.awt.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemDialog extends DialogWrapper
{
  private ReviewItemTabbedPane updateTabbedPane;
  private ReviewItemMainForm createMainForm;
  private CardLayout cardLayout;
  private JPanel centerPanel;
  private IReviewItemForm currentForm;

  public ReviewItemDialog(Project project)
  {
    super(project, true);

    updateTabbedPane = new ReviewItemTabbedPane(project);
    createMainForm = new ReviewItemMainForm(project);

    cardLayout = new CardLayout();
    centerPanel = new JPanel(cardLayout);

    setTitle(RevuBundle.message("dialog.createReviewItem.title"));

    init();
  }

  protected JComponent createCenterPanel()
  {
    centerPanel.add(updateTabbedPane.getClass().getName(), updateTabbedPane.getContentPane());
    centerPanel.add(createMainForm.getClass().getName(), createMainForm.getContentPane());

    return centerPanel;
  }

  public void show(@NotNull ReviewItem reviewItem, boolean create)
  {
    currentForm = (create) ? createMainForm : updateTabbedPane;
    currentForm.updateUI(reviewItem);

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

  public void updateData(@NotNull ReviewItem reviewItem)
  {
    currentForm.updateData(reviewItem);
  }
}
