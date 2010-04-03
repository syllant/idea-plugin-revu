package org.sylfra.idea.plugins.revu.ui;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.util.Alarm;
import com.intellij.util.OpenSourceUtil;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.ui.browsingtable.IssueTable;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Note: Should have used {@link com.intellij.ui.AutoScrollToSourceHandler}, but this class does not support tables
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public final class CustomAutoScrollToSourceHandler
{
  private RevuWorkspaceSettings revuWorkspaceSettings;
  private Alarm autoScrollAlarm;

  public CustomAutoScrollToSourceHandler(RevuWorkspaceSettings revuWorkspaceSettings)
  {
    this.revuWorkspaceSettings = revuWorkspaceSettings;
  }

  public void install(final IssueTable table)
  {
    autoScrollAlarm = new Alarm();
    table.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          return;
        }
        final Object source = e.getSource();
        final int index = table.rowAtPoint(
          SwingUtilities.convertPoint(source instanceof Component ? (Component) source : null,
            e.getPoint(), table));
        if (index >= 0 && index < table.getModel().getRowCount())
        {
          onMouseClicked(table);
        }
      }
    });
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent e)
      {
        onSelectionChanged(table);
      }
    });
  }

  public void cancelAllRequests()
  {
    if (autoScrollAlarm != null)
    {
      autoScrollAlarm.cancelAllRequests();
    }
  }

  public void onMouseClicked(final Component component)
  {
    autoScrollAlarm.cancelAllRequests();
    if (isAutoScrollMode())
    {
      ApplicationManager.getApplication().invokeLater(new Runnable()
      {
        public void run()
        {
          scrollToSource(component);
        }
      });
    }
  }

  private boolean isAutoScrollMode()
  {
    return revuWorkspaceSettings.isAutoScrollToSource();
  }

  private void onSelectionChanged(final Component component)
  {
    if ((component != null) && (!component.isShowing()))
    {
      return;
    }

    if (!isAutoScrollMode())
    {
      return;
    }

    if ((needToCheckFocus()) && (component != null) && (!component.hasFocus()))
    {
      return;
    }

    autoScrollAlarm.cancelAllRequests();
    autoScrollAlarm.addRequest(
      new Runnable()
      {
        public void run()
        {
          if (component.isShowing())
          { //for tests
            scrollToSource(component);
          }
        }
      },
      500
    );
  }

  protected boolean needToCheckFocus()
  {
    return false;
  }

  protected void scrollToSource(Component tree)
  {
    DataContext dataContext = DataManager.getInstance().getDataContext(tree);
    final VirtualFile vFile = PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (vFile != null)
    {
      // Attempt to navigate to the virtual file with unknown file type will show a modal dialog
      // asking to register some file type for this file. This behaviour is undesirable when autoscrolling.
      if (FileTypeManager.getInstance().getFileTypeByFile(vFile) == FileTypes.UNKNOWN)
      {
        return;
      }
    }
    Navigatable[] navigatables = PlatformDataKeys.NAVIGATABLE_ARRAY.getData(dataContext);
    if (navigatables != null)
    {
      for (Navigatable navigatable : navigatables)
      {
        // we are not going to open modal dialog during autoscrolling
        if (!navigatable.canNavigateToSource())
        {
          return;
        }
      }
    }
    OpenSourceUtil.openSourcesFrom(dataContext, false);
  }
}
