package org.sylfra.idea.plugins.revu.actions.issuetree;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.CustomComponentAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.ui.toolwindow.RevuToolWindowManager;
import org.sylfra.idea.plugins.revu.ui.toolwindow.tree.IssueTree;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractComboIssueTreeAction<T> extends AnAction implements CustomComponentAction
{
  public void actionPerformed(AnActionEvent e)
  {
  }

  public JComponent createCustomComponent(Presentation presentation)
  {
    // Should store contentPane as attribute, but action is then not rendered!?
    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(4, 10, 0, 4));

    contentPane.add(new JLabel(getLabel()), BorderLayout.WEST);

    final JComboBox comboBox = new JComboBox();
    comboBox.addActionListener(new ActionListener()
    {
      public void actionPerformed(final ActionEvent e)
      {
        selectionChanged(getCurrentIssueTree(), (T) comboBox.getSelectedItem());
      }
    });
    comboBox.setModel(createModel());
    comboBox.setRenderer(createRenderer());
    contentPane.add(comboBox, BorderLayout.CENTER);

    return contentPane;
  }

  private ListCellRenderer createRenderer()
  {
    return new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus)
      {
        final Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        setText(getItemName((T) value));
        return result;
      }
    };
  }

  @NotNull
  protected abstract String getItemName(@Nullable T t);

  protected ComboBoxModel createModel()
  {
    return new DefaultComboBoxModel(buildComboItems());
  }

  @Nullable
  protected IssueTree getCurrentIssueTree()
  {
    final Project project = RevuUtils.getProject();

    return (project == null) ? null :
      project.getComponent(RevuToolWindowManager.class).getSelectedReviewBrowsingForm().getIssueTree();
  }

  protected abstract String getLabel();

  protected abstract Object[] buildComboItems();

  protected abstract void selectionChanged(@Nullable IssueTree issueTree, @Nullable T item);
}
