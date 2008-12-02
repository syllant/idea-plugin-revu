package org.sylfra.idea.plugins.revu.ui.statusbar;

import com.intellij.concurrency.JobScheduler;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.Colors;
import com.intellij.ui.LightColors;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IReviewExternalizationListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.IRevuSettingsListener;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.forms.settings.app.RevuAppSettingsForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StatusBarComponent extends JLabel implements ProjectComponent, ApplicationComponent
{
  private Project project;
  private List<StatusBarMessage> messages;
  private ScheduledFuture<?> blinkerTask;
  private boolean mustBlink;
  private StatusBarComponent.PopupNotifier popupNotifier;

  public StatusBarComponent(final Project project)
  {
    this.project = project;
    messages = new ArrayList<StatusBarMessage>();
    setPreferredSize(new Dimension(20, 20));
    updateState();
    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        showPopup();
      }
    });
    popupNotifier = new PopupNotifier();

    project.getComponent(ReviewManager.class).addReviewExternalizationListener(new IReviewExternalizationListener()
    {
      public void loadFailed(final String path, Exception exception)
      {
        final String details = ((exception.getLocalizedMessage() == null)
          ? exception.toString() : exception.getLocalizedMessage());
        final VirtualFile vFile = RevuUtils.findVFileFromRelativeFile(project, path);
        ActionListener action = new ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            FileEditorManager.getInstance(project).openFile(vFile, true);
          }
        };
        addMessage(new StatusBarMessage(StatusBarMessage.Type.ERROR,
          RevuBundle.message("friendlyError.externalizing.load.error.title.text"),
          RevuBundle.message("friendlyError.externalizing.load.error.details.text", path, details),
          ((vFile != null) && (vFile.exists()))
            ? RevuBundle.message("friendlyError.externalizing.load.error.action.text") : null,
          action));
      }

      public void saveFailed(Review review, Exception exception)
      {
        final String details = ((exception.getLocalizedMessage() == null)
          ? exception.toString() : exception.getLocalizedMessage());
        addMessage(new StatusBarMessage(StatusBarMessage.Type.ERROR,
          RevuBundle.message("friendlyError.externalizing.save.error.title.text"),
          RevuBundle.message("friendlyError.externalizing.load.error.details.text", review.getPath(), details)));
      }

      public void loadSucceeded(Review review)
      {
      }

      public void saveSucceeded(Review review)
      {
      }
    });
  }

  public void addMessage(StatusBarMessage message)
  {
    mustBlink = true;
    messages.add(message);
    updateState();
    popupNotifier.show(message);
  }

  public void removeMessage(StatusBarMessage message)
  {
    messages.remove(message);
    mustBlink = (mustBlink && !messages.isEmpty());
    updateState();
  }

  public void removeAllMessages()
  {
    messages.clear();
    mustBlink = false;
    updateState();
  }

  private void updateState()
  {
    // Tooltip
    setToolTipText(RevuBundle.message("statusPopup.tip", messages.size()));

    // Icon
    StatusBarMessage.Type priorType = StatusBarMessage.Type.INFO;
    for (StatusBarMessage message : messages)
    {
      if (message.getType().getPriority() > priorType.getPriority())
      {
        priorType = message.getType();
      }
    }
    setIcon(RevuIconProvider.getIcon(priorType.getIconRef()));

    if (mustBlink)
    {
      if ((blinkerTask == null) || (blinkerTask.isCancelled()))
      {
        blinkerTask = JobScheduler.getScheduler().scheduleAtFixedRate(new Blinker(), 1L, 1L, TimeUnit.SECONDS);
      }
    }
    else
    {
      if (blinkerTask != null)
      {
        blinkerTask.cancel(true);
      }
    }
  }

  public void showPopup()
  {
    if (messages.isEmpty())
    {
      return;
    }

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        StatusBarPopup popup = new StatusBarPopup(project, StatusBarComponent.this);
        popup.show(messages);

        mustBlink = false;
        updateState();
      }
    });
  }

  public void projectOpened()
  {
    StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
    if (statusBar == null)
    {
      return;
    }

    statusBar.addCustomIndicationComponent(this);
    WindowManager.getInstance().getFrame(project).repaint();
  }

  public void projectClosed()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".StatusBarComponent";
  }

  public void initComponent()
  {
    ApplicationManager.getApplication().getComponent(RevuAppSettingsComponent.class)
      .addListener(new IRevuSettingsListener<RevuAppSettings>()
    {
      public void settingsChanged(RevuAppSettings settings)
      {
        if ((settings.getLogin() == null) || (settings.getLogin().equals("")))
        {
          addMessage(new StatusBarMessage(StatusBarMessage.Type.INFO,
            RevuBundle.message("friendlyError.nologin.info.title.text"),
            null,
            RevuBundle.message("friendlyError.nologin.info.action.text"),
            new ActionListener()
            {
              public void actionPerformed(ActionEvent e)
              {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, RevuAppSettingsForm.class);
              }
            }));
        }
      }
    });
  }

  public void disposeComponent()
  {
  }

  private class Blinker implements Runnable
  {
    private Icon swapIcon;

    public Blinker()
    {
      swapIcon = RevuIconProvider.getIcon(StatusBarMessage.Type.INFO.getIconRef());
    }

    public void run()
    {
      final Icon tmpIcon = swapIcon;
      swapIcon = StatusBarComponent.this.getIcon();
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run()
        {
          StatusBarComponent.this.setIcon(tmpIcon);
        }
      });
    }
  }

  private final class PopupNotifier
  {
    private boolean available;
    private JLabel label;

    public PopupNotifier()
    {
      label = new JLabel();
      label.setBorder(BorderFactory.createLineBorder(LightColors.BLUE));
      label.setForeground(Colors.DARK_BLUE);
      label.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          showPopup();
        }
      });

      available = true;
    }

    private void show(StatusBarMessage message)
    {
      if (!available)
      {
        return;
      }

      new Timer(5000, new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          available = true;
        }
      }).start();

      MessageType messageType = message.getType().getMessageType();

      label.setIcon(messageType.getDefaultIcon());
      label.setText(RevuBundle.message("statusPopup.alert.text", message.getTitle()));

      WindowManager.getInstance().getStatusBar(project).fireNotificationPopup(label,
        messageType.getPopupBackground());
    }
  }
}
