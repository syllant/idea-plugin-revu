package org.sylfra.idea.plugins.revu.ui.forms.settings.project;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.config.IReviewExternalizer;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Used to interface settings inside Settings panel
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuProjectSettingsForm implements ProjectComponent, Configurable
{
  private final Project project;
//  private final List<Review> originalReviews;
  private JPanel contentPane;
  private JList liReviews;
  private JComponent reviewToolBar;
  private ReviewSettingsForm reviewForm;

  public RevuProjectSettingsForm(@NotNull Project project)
  {
    this.project = project;
//    originalReviews = new ArrayList<Review>();
    configureUI();
  }

  private void configureUI()
  {
    liReviews.setCellRenderer(new DefaultListCellRenderer()
    {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus)
      {
        Review review = (Review) value;
        value = review.getTitle();
        if (!review.isActive())
        {
          setForeground(Color.GRAY);
        }

        //@TODO icon private/public

        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
    liReviews.setSelectionModel(new DefaultListSelectionModel()
    {
      @Override
      public void setSelectionInterval(int index0, int index1)
      {
        if (updateReviewData())
        {
          super.setSelectionInterval(index0, index1);
          updateReviewUI();
          ActionManager.getInstance().getAction("revu.RemoveReview").getTemplatePresentation()
            .setEnabled(!liReviews.getSelectionModel().isSelectionEmpty());
        }
      }
    });
  }

  private boolean updateReviewData()
  {
    Review current = (Review) liReviews.getSelectedValue();
    return (current == null) || reviewForm.updateData(current);
  }

  private void updateReviewUI()
  {
    Review current = (Review) liReviews.getSelectedValue();
    if (current != null)
    {
      reviewForm.updateUI(current);
    }
  }

  /**
   * {@inheritDoc}
   */
  @NonNls
  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".ProjectSettingsConfigurable";
  }

  /**
   * {@inheritDoc}
   */
  public void initComponent()
  {
  }

  /**
   * {@inheritDoc}
   */
  public void disposeComponent()
  {
    reviewForm.dispose();
  }

  /**
   * {@inheritDoc}
   */
  @Nls
  public String getDisplayName()
  {
    return "reVu";
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  public Icon getIcon()
  {
    return RevuIconProvider.getIcon(RevuIconProvider.IconRef.REVU_LARGE);
  }

  /**
   * {@inheritDoc}
   */
  @Nullable
  @NonNls
  public String getHelpTopic()
  {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public JComponent createComponent()
  {
    return contentPane;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isModified()
  {
    List<Review> previousReviews = project.getComponent(ReviewManager.class).getReviews();

    // Review count
    int reviewCount = liReviews.getModel().getSize();
    if (reviewCount != previousReviews.size())
    {
      return true;
    }

    // Current edited review
    if (reviewForm.isModified((Review) liReviews.getSelectedValue()))
    {
      return true;
    }

    // Other lists
    for (int i = 0; i < reviewCount; i++)
    {
      Review review = (Review) liReviews.getModel().getElementAt(i);
      if (!review.equals(previousReviews.get(i)))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void apply() throws ConfigurationException
  {
    if (!updateReviewData())
    {
      throw new ConfigurationException(
        RevuBundle.message("settings.project.error.form.title.text"), RevuBundle.message("plugin.revu.title"));
    }

    IReviewExternalizer reviewExternalizer =
      project.getComponent(IReviewExternalizer.class);

    int reviewCount = liReviews.getModel().getSize();
    List<String> projectReviewFiles = new ArrayList<String>();
    List<String> workspaceReviewFiles = new ArrayList<String>();
    for (int i = 0; i < reviewCount; i++)
    {
      Review currentReview = (Review) liReviews.getModel().getElementAt(i);
      if (currentReview.isEmbedded())
      {
        continue;
      }
      
      String reviewFilePath = RevuUtils.buildRelativePath(project, currentReview.getPath());

      if (currentReview.isShared())
      {
        projectReviewFiles.add(reviewFilePath);
      }
      else
      {
        workspaceReviewFiles.add(reviewFilePath);
      }

      try
      {
        reviewExternalizer.save(currentReview);
      }
      catch (RevuException e)
      {
        throw new ConfigurationException(
          RevuBundle.message("settings.project.error.save.title.text", currentReview.getTitle(), e.getMessage()),
          RevuBundle.message("plugin.revu.title"));
      }
    }

    RevuProjectSettingsComponent projectSettingsComponent =
      project.getComponent(RevuProjectSettingsComponent.class);
    RevuProjectSettings projectSettings = retrieveProjectSettings();
    projectSettings.setReviewFiles(projectReviewFiles);
    projectSettingsComponent.loadState(projectSettings);

    RevuWorkspaceSettingsComponent workspaceSettingsComponent =
      project.getComponent(RevuWorkspaceSettingsComponent.class);
    RevuWorkspaceSettings workspaceSettings = retrieveWorkspaceSettings();
    workspaceSettings.setReviewFiles(workspaceReviewFiles);
    workspaceSettingsComponent.loadState(workspaceSettings);
  }

  /**
   * {@inheritDoc}
   */
  public void reset()
  {
    reviewForm.updateUI(null);
//    originalReviews.clear();

    DefaultListModel liReviewsModel = new DefaultListModel();

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    for (Review review : reviewManager.getReviews(null, null))
    {
      // List stores shallow clones so changes may be properly canceled
      liReviewsModel.addElement(review.clone());
    }

    liReviews.setModel(liReviewsModel);
    if (liReviews.getModel().getSize() > 0)
    {
      liReviews.setSelectedIndex(0);
    }
  }

  private RevuProjectSettings retrieveProjectSettings()
  {
    return project.getComponent(RevuProjectSettingsComponent.class).getState();
  }

  private RevuWorkspaceSettings retrieveWorkspaceSettings()
  {
    return project.getComponent(RevuWorkspaceSettingsComponent.class).getState();
  }

  /**
   * {@inheritDoc}
   */
  public void disposeUIResources()
  {
    reviewForm.dispose();
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  private void createUIComponents()
  {
    liReviews = new ReviewListComponent();
    reviewForm = new ReviewSettingsForm(project);

    // Toolbar
    ActionGroup reviewsActionGroup = (ActionGroup) ActionManager.getInstance()
      .getAction("revu.settings.project.reviews");

    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, reviewsActionGroup, true);
    actionToolbar.setTargetComponent(liReviews);
    reviewToolBar = actionToolbar.getComponent();
  }

  public static class CreateReviewAction extends AnAction
  {
    public void actionPerformed(AnActionEvent e)
    {
      JList liReviews = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
      String title = Messages.showInputDialog(liReviews,
        RevuBundle.message("settings.project.addReview.text"),
        RevuBundle.message("settings.project.addReview.title"),
        Messages.getInformationIcon(), "Unnamed", new NonEmptyInputValidator());

      if (title != null)
      {
        DefaultListModel model = (DefaultListModel) liReviews.getModel();
        Review review = new Review();
        review.setTitle(title);

        User user = new User();
        user.setLogin(RevuUtils.getCurrentUserLogin());

        History history = new History();
        Date now = new Date();
        history.setCreatedBy(user);
        history.setCreatedOn(now);
        history.setLastUpdatedBy(user);
        history.setLastUpdatedOn(now);
        review.setHistory(history);

        model.addElement(review);
        liReviews.setSelectedValue(review, true);
      }
    }
  }

  public static class RemoveReviewAction extends AnAction
  {
    public void actionPerformed(AnActionEvent e)
    {
      JList liReviews = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
      int result = Messages.showOkCancelDialog(liReviews,
        RevuBundle.message("settings.project.confirmRemoveReview.text"),
        RevuBundle.message("settings.project.confirmRemoveReview.title"),
        Messages.getWarningIcon());

      if (result == DialogWrapper.OK_EXIT_CODE)
      {
        DefaultListModel model = (DefaultListModel) liReviews.getModel();
        model.removeElement(liReviews.getSelectedValue());
      }
    }
  }

  private class ReviewListComponent extends JList implements DataProvider
  {
    public Object getData(@NonNls String dataId)
    {
      return null;
    }
  }
}