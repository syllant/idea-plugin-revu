package org.sylfra.idea.plugins.revu.ui.statusbar;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.LightColors;
import org.sylfra.idea.plugins.revu.RevuBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StatusBarPopup extends DialogWrapper
{
  private JEditorPane epDetails;
  private JLabel lbTitle;
  private JLabel lbLink;
  private JPanel contentPane;
  private JLabel lbPagination;
  private JComponent tbPagination;
  private List<StatusBarMessage> messages;
  private int currentIndex;
  private final StatusBarComponent statusBarComponent;

  public StatusBarPopup(Project project, StatusBarComponent statusBarComponent)
  {
    super(project, false);
    this.statusBarComponent = statusBarComponent;

    epDetails.setBorder(null);
    epDetails.setBackground(LightColors.YELLOW);
    lbLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    lbLink.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        final StatusBarMessage message = messages.get(currentIndex);
        if (message.getAction() != null)
        {
          close(DialogWrapper.OK_EXIT_CODE);
          SwingUtilities.invokeLater(new Runnable()
          {
            public void run()
            {
              message.getAction().actionPerformed(null);
            }
          });
        }
      }
    });

    init();
    setTitle(RevuBundle.message("statusPopup.title"));
    setOKButtonText(RevuBundle.message("statusPopup.clearnclose.text"));
    setCancelButtonText(RevuBundle.message("statusPopup.close.text"));
    getContentPane().setMinimumSize(new Dimension(400, 300));
    pack();
  }

  @Override
  protected void doOKAction()
  {
    statusBarComponent.removeAllMessages();
    update(-1);
    super.doOKAction();
  }

  protected JComponent createCenterPanel()
  {
    return contentPane;
  }

  public JPanel getContentPane()
  {
    return contentPane;
  }

  public void show(List<StatusBarMessage> messages)
  {
    this.messages = messages;
    update(0);
    super.show();
  }

  private void update(int index)
  {
    currentIndex = index;

    if ((currentIndex < 0) || (currentIndex > messages.size() - 1))
    {
      return;
    }

    StatusBarMessage message = messages.get(currentIndex);

    lbPagination.setText(RevuBundle.message("statusPopup.pagination.text", currentIndex + 1, messages.size()));
    lbTitle.setIcon(message.getType().getMessageType().getDefaultIcon());
    lbTitle.setText(message.getTitle());
    lbLink.setText(message.getActionText());

    String details = message.getDetails();
    if (details != null)
    {
      details = details.replaceAll("\n", "<br/>");
    }
    epDetails.setText(details);
  }

  private void createUIComponents()
  {
    AnAction previousAction = new AnAction(null, RevuBundle.message("statusPopup.previous.tip"),
      IconLoader.getIcon("/actions/back.png"))
    {
      @Override
      public void actionPerformed(AnActionEvent e)
      {
        StatusBarPopup.this.update(currentIndex - 1);
      }

      @Override
      public void update(AnActionEvent e)
      {
        e.getPresentation().setEnabled(currentIndex > 0);
      }
    };
    AnAction nextAction = new AnAction(null, RevuBundle.message("statusPopup.next.tip"),
      IconLoader.getIcon("/actions/forward.png"))
    {

      @Override
      public void actionPerformed(AnActionEvent e)
      {
        StatusBarPopup.this.update(currentIndex + 1);
      }

      @Override
      public void update(AnActionEvent e)
      {
        e.getPresentation().setEnabled(((messages != null) && (currentIndex < messages.size() - 1)));
      }
    };

    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(previousAction);
    actionGroup.add(nextAction);
    tbPagination = ActionManager.getInstance().createActionToolbar("", actionGroup, true)
      .getComponent();
  }
}
