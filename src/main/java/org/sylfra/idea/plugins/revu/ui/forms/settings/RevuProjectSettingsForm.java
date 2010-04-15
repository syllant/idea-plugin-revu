package org.sylfra.idea.plugins.revu.ui.forms.settings;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettings;
import org.sylfra.idea.plugins.revu.settings.project.RevuProjectSettingsComponent;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractListUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.review.ReviewForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to interface settings inside Settings panel
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuProjectSettingsForm extends AbstractListUpdatableForm<Review, ReviewForm> implements ProjectComponent
{
  private static final Logger LOGGER = Logger.getInstance(RevuProjectSettingsForm.class.getName());

  private final IRevuSettingsListener<RevuAppSettings> appSettingsListener;

  public RevuProjectSettingsForm(@NotNull Project project)
  {
    super(project);

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
  }

  @Override
  protected void setupUI()
  {
    super.setupUI();

    // Later this label might display distinct message depending on app settings, but for now, it is used for
    // only one intention, so build it here once to optimize it !
    lbMessageWholePane.setText(RevuBundle.message("general.form.noLogin.text"));
    lbMessageWholePane.setIcon(Messages.getInformationIcon());
    lbMessageWholePane.setIconTextGap(20);
    lbMessageWholePane.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        // Open App settings in a new dialog. Could also open settings using current dialog...
        ShowSettingsUtil.getInstance().editConfigurable(project,
          ApplicationManager.getApplication().getComponent(RevuAppSettingsForm.class));
      }
    });

    RevuAppSettingsComponent appSettingsComponent =
      ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class);
    updateUIDependingOnAppSettings(appSettingsComponent.getState());
  }

  private void updateUIDependingOnAppSettings(RevuAppSettings settings)
  {
    showWholeMessage((settings.getLogin() == null) || (settings.getLogin().trim().length() == 0));
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
    ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class)
      .removeListener(appSettingsListener);
  }

  /**
   * {@inheritDoc}
   */
  @Nls
  public String getDisplayName()
  {
    return RevuPlugin.PLUGIN_NAME;
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
  public void disposeUIResources()
  {
    dispose();
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void apply(List<Review> items) throws ConfigurationException
  {
    List<String> projectReviewFiles = new ArrayList<String>();
    List<String> workspaceReviewFiles = new ArrayList<String>();
    for (int i = 0, itemsSize = items.size(); i < itemsSize; i++)
    {
      Review review = items.get(i);
      if (review.isEmbedded())
      {
        continue;
      }

      String reviewFilePath = RevuVfsUtils.buildRelativePath(project, review.getPath());

      if (review.isShared())
      {
        projectReviewFiles.add(reviewFilePath);
      }
      else
      {
        workspaceReviewFiles.add(reviewFilePath);
      }

      // Change original review to avoid handling an obsolete instance
      Review originalReview = originalItemsMap.get(review);
      if (originalReview == null)
      {
        originalReview = review;
      }
      else
      {
        originalReview.copyFrom(review);
      }

      try
      {
        project.getComponent(ReviewManager.class).save(originalReview);
      }
      catch (Exception e)
      {
        LOGGER.warn(e);
        final String details = ((e.getLocalizedMessage() == null) ? e.toString() : e.getLocalizedMessage());
        throw new ConfigurationException(
          RevuBundle.message("projectSettings.error.save.title.text", review.getName(), details),
          RevuBundle.message("general.plugin.title"));
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

  private RevuProjectSettings retrieveProjectSettings()
  {
    return RevuUtils.getProjectSettings(project);
  }

  private RevuWorkspaceSettings retrieveWorkspaceSettings()
  {
    return RevuUtils.getWorkspaceSettings(project);
  }

  @Override
  protected void customizeListCellRendererComponent(JLabel renderer, JList list, Review entity, int index,
    boolean selected, boolean cellHasFocus)
  {
    renderer.setIcon(RevuIconProvider.getIcon(entity.isShared()
      ? RevuIconProvider.IconRef.REVIEW_SHARED : RevuIconProvider.IconRef.REVIEW_LOCAL));
  }

  protected ReviewForm createMainForm()
  {
    List<Review> editedReviews = new AbstractList<Review>()
    {
      @Override
      public Review get(int index)
      {
        return (Review) list.getModel().getElementAt(index);
      }

      @Override
      public int size()
      {
        return list.getModel().getSize();
      }
    };

    return new ReviewForm(project, editedReviews);
  }

  protected ActionGroup createActionGroup()
  {
    return (ActionGroup) ActionManager.getInstance().getAction("revu.settings.project.reviews");
  }

  protected List<Review> getOriginalItems()
  {
    return new ArrayList<Review>(project.getComponent(ReviewManager.class).getReviews());
  }

  @Override
  protected DataKey createListSelectedEntityDataKey()
  {
    return RevuDataKeys.REVIEW;
  }

  @Override
  protected DataKey createListAllEntitiesDataKeys()
  {
    return RevuDataKeys.REVIEW_LIST;
  }
}