package org.sylfra.idea.plugins.revu.ui.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuKeys;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuToolWindowManager implements ProjectComponent, IReviewListener
{
  private ToolWindow toolwindow;
  private final Project project;
  private final JLabel lbMessage;
  private final Map<Review, Content> contentsByReviews;
  private final MessageClickHandler messageClickHandler;
  private Content messageContent;
  private IRevuSettingsListener<RevuAppSettings> appSettingsListener;

  public RevuToolWindowManager(Project project)
  {
    this.project = project;
    contentsByReviews = new IdentityHashMap<Review, Content>();
    messageClickHandler = new MessageClickHandler(project);

    lbMessage = new JLabel("", SwingConstants.CENTER);
    lbMessage.setIcon(IconLoader.getIcon("/general/informationDialog.png"));
    lbMessage.setIconTextGap(20);
    lbMessage.addMouseListener(messageClickHandler);

    messageContent = ContentFactory.SERVICE.getInstance().createContent(lbMessage, RevuPlugin.PLUGIN_NAME, true);
  }

  private IssueBrowsingPane addReviewTab(@NotNull Review review)
  {
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

    IssueBrowsingPane issueBrowsingPane = new IssueBrowsingPane(project, review);
    Content content = contentFactory.createContent(issueBrowsingPane.getContentPane(), buildTableTitle(review), true);
    content.putUserData(RevuKeys.ISSUE_BROWSING_PANE_KEY, issueBrowsingPane);
    toolwindow.getContentManager().addContent(content);
    contentsByReviews.put(review, content);

    checkMessagePane();

    return issueBrowsingPane;
  }

  private void removeReviewTab(@NotNull Review review)
  {
    Content content = contentsByReviews.remove(review);
    if (content != null)
    {
      toolwindow.getContentManager().removeContent(content, true);
    }

    checkMessagePane();
  }

  private String buildTableTitle(Review review)
  {
    return RevuBundle.message("browsing.issues.review.title", review.getName(),
      RevuUtils.buildReviewStatusLabel(review.getStatus(), true));
  }

  @Nullable
  public IssueBrowsingPane getSelectedReviewBrowsingForm()
  {
    Content selectedContent = toolwindow.getContentManager().getSelectedContent();

    return (selectedContent != null) ? selectedContent.getUserData(RevuKeys.ISSUE_BROWSING_PANE_KEY) : null;
  }

  public void projectOpened()
  {
    toolwindow = ToolWindowManager.getInstance(project)
      .registerToolWindow(RevuPlugin.PLUGIN_NAME, true, ToolWindowAnchor.BOTTOM);
    toolwindow.setIcon(RevuIconProvider.getIcon(RevuIconProvider.IconRef.REVU));

    toolwindow.getContentManager().addContentManagerListener(new ContentManagerAdapter()
    {
      @Override
      public void selectionChanged(ContentManagerEvent event)
      {
        IssueBrowsingPane browsingPane = getSelectedReviewBrowsingForm();
        if (browsingPane != null)
        {
          browsingPane.updateUI(false);
        }
      }
    });

    project.getComponent(ReviewManager.class).addReviewListener(this);

    checkMessagePane();
  }

  public void projectClosed()
  {
    // If dispose is done in #initComponent(), userData is empty ?! 
    for (Content content : contentsByReviews.values())
    {
      IssueBrowsingPane pane = content.getUserData(RevuKeys.ISSUE_BROWSING_PANE_KEY);
      if (pane != null)
      {
        pane.dispose();
      }
    }

    project.getComponent(ReviewManager.class).removeReviewListener(this);
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + "." + getClass().getSimpleName();
  }

  public void initComponent()
  {
    appSettingsListener = new IRevuSettingsListener<RevuAppSettings>()
    {
      public void settingsChanged(RevuAppSettings oldSettings, RevuAppSettings newSettings)
      {
        checkMessagePane();
      }
    };
    RevuAppSettingsComponent appSettingsComponent =
      ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class);
    appSettingsComponent.addListener(appSettingsListener);
  }

  public void disposeComponent()
  {
    messageContent.dispose();

    RevuAppSettingsComponent appSettingsComponent =
      ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class);
    appSettingsComponent.removeListener(appSettingsListener);
  }

  public ToolWindow getToolwindow()
  {
    return toolwindow;
  }

  public void reviewChanged(Review review)
  {
    if (!RevuUtils.isActiveForCurrentUser(review))
    {
      removeReviewTab(review);
    }
    else
    {
      Content content = contentsByReviews.get(review);
      if (content == null)
      {
        addReviewTab(review);
      }
      else
      {
        content.setDisplayName(buildTableTitle(review));
        // @TODO how to refresh tab title ?
        IssueBrowsingPane pane = content.getUserData(RevuKeys.ISSUE_BROWSING_PANE_KEY);
        if (pane != null)
        {
          pane.updateReview();
        }
      }
    }
  }

  public void reviewAdded(Review review)
  {
    if (RevuUtils.isActiveForCurrentUser(review))
    {
      addReviewTab(review);
    }
  }

  public void reviewDeleted(Review review)
  {
    removeReviewTab(review);
  }

  private void checkMessagePane()
  {
    String message = null;

    // Login set
    RevuAppSettings appSettings = RevuUtils.getAppSettings();

    if ((appSettings.getLogin() == null) || (appSettings.getLogin().trim().length() == 0))
    {
      message = RevuBundle.message("general.form.noLogin.text");
      messageClickHandler.setType(MessageClickHandler.Type.NO_LOGIN);
    }
    else
    {
      // No review
      if (contentsByReviews.isEmpty())
      {
        message = RevuBundle.message("browsing.issues.noReview.text");
        messageClickHandler.setType(MessageClickHandler.Type.NO_REVIEW);
      }
    }

    if (message == null)
    {
      toolwindow.getContentManager().removeContent(messageContent, false);
    }
    else
    {
      lbMessage.setText(message);
      toolwindow.getContentManager().addContent(messageContent);
    }
  }

  private static class MessageClickHandler extends MouseAdapter
  {
    enum Type
    {
      NO_LOGIN,
      NO_REVIEW
    }

    private final Project project;
    private Type type;

    public MessageClickHandler(Project project)
    {
      this.project = project;
    }

    public void setType(Type type)
    {
      this.type = type;
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
      switch (type)
      {
        case NO_LOGIN:
          RevuUtils.editAppSettings(project);
          break;

        case NO_REVIEW:
          RevuUtils.editProjectSettings(project, null);
          break;
      }
    }
  }
}
