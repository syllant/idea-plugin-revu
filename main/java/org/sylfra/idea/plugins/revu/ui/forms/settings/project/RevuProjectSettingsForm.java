package org.sylfra.idea.plugins.revu.ui.forms.settings.project;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
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
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.forms.settings.app.RevuAppSettingsForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.AbstractList;
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
  private JPanel contentPane;
  private JList liReviews;
  private JComponent reviewToolBar;
  private ReviewSettingsForm reviewForm;
  private JLabel lbMessage;
  private final IRevuSettingsListener<RevuAppSettings> appSettingsListener;

  public RevuProjectSettingsForm(@NotNull Project project)
  {
    this.project = project;

    RevuAppSettingsComponent appSettingsComponent =
      ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class);

    appSettingsListener = new IRevuSettingsListener<RevuAppSettings>()
    {
      public void settingsChanged(RevuAppSettings settings)
      {
        updateUIDependingOnAppSettings(settings);
      }
    };
    appSettingsComponent.addListener(appSettingsListener);

    configureUI();
    updateUIDependingOnAppSettings(appSettingsComponent.getState());
  }

  private void configureUI()
  {
    // Later this label might display distinct message depending on app settings
    lbMessage.setText(RevuBundle.message("general.form.noLogin.text"));
    lbMessage.setIcon(Messages.getInformationIcon());
    lbMessage.setIconTextGap(20);
    lbMessage.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        // Open App settings in a new dialog. Could also open settings using current dialog...
        ShowSettingsUtil.getInstance().editConfigurable(project,
          ApplicationManager.getApplication().getComponent(RevuAppSettingsForm.class));
      }
    });

    liReviews.setCellRenderer(new DefaultListCellRenderer()
    {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus)
      {
        Review review = (Review) value;
        value = review.getTitle();

        Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (!review.isActive())
        {
          result.setForeground(Color.GRAY);
        }

        //@TODO icon private/public

        return result;
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
      reviewForm.updateUI(current, current);
    }
  }

  private void updateUIDependingOnAppSettings(RevuAppSettings settings)
  {
    CardLayout cardLayout = (CardLayout) contentPane.getLayout();
    if ((settings.getLogin() == null) || (settings.getLogin().trim().length() == 0))
    {
      cardLayout.show(contentPane, "label");
    }
    else
    {
      cardLayout.show(contentPane, "form");
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
    ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class)
      .removeListener(appSettingsListener);
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
    if (lbMessage.isVisible())
    {
      return false;
    }
    
    List<Review> previousReviews = project.getComponent(ReviewManager.class).getReviews();

    // Review count
    int reviewCount = liReviews.getModel().getSize();
    if (reviewCount != previousReviews.size())
    {
      return true;
    }

    // Current edited review
    Review selectedReview = (Review) liReviews.getSelectedValue();
    if (reviewForm.isModified(selectedReview))
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
        RevuBundle.message("general.form.hasErrors.text"), RevuBundle.message("plugin.revu.title"));
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
      
      String reviewFilePath = RevuVfsUtils.buildRelativePath(project, currentReview.getPath());

      if (currentReview.isShared())
      {
        projectReviewFiles.add(reviewFilePath);
      }
      else
      {
        workspaceReviewFiles.add(reviewFilePath);
      }

      // Don't use reviewManager to control exceptions
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
    reviewForm.updateUI(null, null);

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
    liReviews = new JList();
    List<Review> editedReviews = new AbstractList<Review>()
    {
      @Override
      public Review get(int index)
      {
        return (Review) liReviews.getModel().getElementAt(index);
      }

      @Override
      public int size()
      {
        return liReviews.getModel().getSize();
      }
    };
    reviewForm = new ReviewSettingsForm(project, editedReviews);
    CreateReviewAction createReviewAction = (CreateReviewAction) ActionManager.getInstance()
      .getAction("revu.CreateReview");
    createReviewAction.setEditedReviews(editedReviews);

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
    private List<Review> editedReviews;

    public void setEditedReviews(List<Review> editedReviews)
    {
      this.editedReviews = editedReviews;
    }

    public void actionPerformed(AnActionEvent e)
    {
      JList liReviews = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
      Project project = e.getData(DataKeys.PROJECT);

      CreateReviewDialog dialog = new CreateReviewDialog(project, true);
      dialog.show(editedReviews, null);
      if (!dialog.isOK())
      {
        return;
      }

      DefaultListModel model = (DefaultListModel) liReviews.getModel();
      Review review = new Review();
      review.setActive(true);
      review.setPath(dialog.getPath());
      review.setTitle(dialog.getTitle());
      switch (dialog.getImportType())
      {
        case COPY:
          review.copyFrom(dialog.getImportedReview());
          break;
        case LINK:
          review.setExtendedReview(dialog.getImportedReview());
          break;
      }

      User currentUser = RevuUtils.getCurrentUser();
      User reviewCurrentUser = review.getDataReferential().getUser(currentUser.getLogin(), false);
      if (reviewCurrentUser == null)
      {
        currentUser.addRole(User.Role.ADMIN);
        review.getDataReferential().addUser(currentUser);
      }
      else
      {
        if (!reviewCurrentUser.hasRole(User.Role.ADMIN))
        {
          reviewCurrentUser.addRole(User.Role.ADMIN);
        }
      }

      History history = new History();
      Date now = new Date();
      history.setCreatedBy(currentUser);
      history.setCreatedOn(now);
      history.setLastUpdatedBy(currentUser);
      history.setLastUpdatedOn(now);
      review.setHistory(history);

      model.addElement(review);
      liReviews.setSelectedValue(review, true);
    }
  }

  public static class RemoveReviewAction extends AnAction
  {
    public void actionPerformed(AnActionEvent e)
    {
      JList liReviews = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
      DefaultListModel model = (DefaultListModel) liReviews.getModel();
      Review selectedReview = (Review) liReviews.getSelectedValue();

      // Check afferent link
      List<Review> afferentReviews = new ArrayList<Review>();
      for (int i=0; i<model.getSize(); i++)
      {
        Review review = (Review) model.get(i);
        if (selectedReview.equals(review.getExtendedReview()))
        {
          afferentReviews.add(review);
        }
      }

      String msgKey = afferentReviews.isEmpty()
        ? "settings.project.confirmRemoveReview.text"
        : "settings.project.confirmRemoveReviewWithAfferentLink.text";
      int result = Messages.showOkCancelDialog(liReviews,
        RevuBundle.message(msgKey, selectedReview.getTitle()),
        RevuBundle.message("settings.project.confirmRemoveReview.title"),
        Messages.getWarningIcon());

      if (result == DialogWrapper.OK_EXIT_CODE)
      {
        model.removeElement(selectedReview);
        for (Review review : afferentReviews)
        {
          review.setExtendedReview(null);
        }
        liReviews.setSelectedIndex(0);
      }
    }
  }
}