package org.sylfra.idea.plugins.revu.ui.forms;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.IRevuUniqueNameHolderEntity;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to interface settings inside Settings panel
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractListUpdatableForm<E extends IRevuUniqueNameHolderEntity<E>,
  F extends AbstractUpdatableForm<E>> implements Configurable, Disposable
{
  protected final Project project;
  protected JComponent contentPane;
  protected JComponent toolBar;
  protected JList list;
  protected JLabel lbMessageWholePane;
  protected JLabel lbMessageMainPane;
  protected F mainForm;
  private JPanel mainPane;

  public AbstractListUpdatableForm(@NotNull Project project)
  {
    this.project = project;

    setupUI();
    configureUI();
  }

  public void dispose()
  {
    mainForm.dispose();
  }

  protected void setupUI()
  {
    mainForm = createMainForm();

    // Toolbar
    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, createActionGroup(), true);
    actionToolbar.setTargetComponent(list);
    toolBar = actionToolbar.getComponent();

    JPanel pnList = new JPanel(new BorderLayout());
    pnList.setMinimumSize(new Dimension(50, 0));
    pnList.setMinimumSize(new Dimension(150, 0));

    list = new JList();
    pnList.add(new JScrollPane(list), BorderLayout.CENTER);
    pnList.add(toolBar, BorderLayout.NORTH);

    contentPane = new JPanel();
    contentPane.setLayout(new CardLayout(0, 0));

    JSplitPane splitPane = new JSplitPane();
    contentPane.add(splitPane, "pane");

    lbMessageMainPane = new JLabel();
    lbMessageMainPane.setHorizontalAlignment(0);
    lbMessageMainPane.setHorizontalTextPosition(SwingConstants.CENTER);

    mainPane = new JPanel(new CardLayout(0, 0));
    mainPane.add(mainForm.getContentPane(), "pane");
    mainPane.add(lbMessageMainPane, "message");

    splitPane.setLeftComponent(pnList);
    splitPane.setRightComponent(mainPane);
//    splitPane.setDividerLocation(150);
//    splitPane.setResizeWeight(0);

    lbMessageWholePane = new JLabel();
    lbMessageWholePane.setHorizontalAlignment(0);
    lbMessageWholePane.setHorizontalTextPosition(SwingConstants.CENTER);
    contentPane.add(lbMessageWholePane, "message");
  }

  protected void configureUI()
  {
    list.setCellRenderer(new DefaultListCellRenderer()
    {
      @SuppressWarnings({"unchecked"})
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus)
      {
        E entity = (E) value;
        value = entity.getName();

        Component result = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        AbstractListUpdatableForm.this.customizeListCellRendererComponent((JLabel) result, list, entity, index,
          isSelected, cellHasFocus);

        return result;
      }
    });
    list.setSelectionModel(new DefaultListSelectionModel()
    {
      @Override
      public void setSelectionInterval(int index0, int index1)
      {
        if (updateFormData())
        {
          super.setSelectionInterval(index0, index1);
          updateFormUI();
        }
      }
    });
  }

  protected void showWholeMessage(boolean visible)
  {
    showMessage(contentPane, visible);
  }

  protected void showMainMessage(boolean visible)
  {
    showMessage(mainPane, visible);
  }

  private void showMessage(JComponent pane, boolean visible)
  {
    // pane may be null when closing form
    if (pane == null)
    {
      return;
    }

    CardLayout cardLayout = (CardLayout) pane.getLayout();
    if (visible)
    {
      cardLayout.show(pane, "message");
    }
    else
    {
      cardLayout.show(pane, "pane");
    }
  }

  @SuppressWarnings({"unchecked"})
  private boolean updateFormData()
  {
    E current = (E) list.getSelectedValue();
    return (current == null) || mainForm.updateData(current);
  }

  @SuppressWarnings({"unchecked"})
  private void updateFormUI()
  {
    E current = (E) list.getSelectedValue();
    if (current != null)
    {
      showMainMessage(false);
      mainForm.updateUI(null, current, true);
    }
    else
    {
      String msgKey = getMessageKeyWhenNoSelection();
      if (msgKey != null)
      {
        lbMessageMainPane.setText(RevuBundle.message(msgKey));
      }

      showMainMessage(true);
    }
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({"unchecked"})
  public boolean isModified()
  {
    if (lbMessageWholePane.isVisible())
    {
      return false;
    }

    List<E> originalItems = getOriginalItems();

    // Review count
    int itemCount = list.getModel().getSize();
    if (itemCount != originalItems.size())
    {
      return true;
    }

    if (itemCount == 0)
    {
      return false;
    }

    // Current edited review
    E selectedValue = (E) list.getSelectedValue();
    if (mainForm.isModified(selectedValue))
    {
      return true;
    }

    // Other lists
    for (int i = 0; i < itemCount; i++)
    {
      E review = (E) list.getModel().getElementAt(i);
      if (!review.equals(originalItems.get(i)))
      {
        return true;
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public final void apply() throws ConfigurationException
  {
    if (!updateFormData())
    {
      throw new ConfigurationException(
        RevuBundle.message("general.form.hasErrors.text"), RevuBundle.message("general.plugin.title"));
    }

    int itemCount = list.getModel().getSize();
    List<E> items = new ArrayList<E>(itemCount);
    for (int i=0; i < itemCount; i++)
    {
      //noinspection unchecked
      items.add((E) list.getModel().getElementAt(i));
    }

    apply(items);
  }

  /**
   * {@inheritDoc}
   */
  public void reset()
  {
    updateFormUI();

    DefaultListModel listModel = new DefaultListModel();

    List<E> originalItems = getOriginalItems();
    if (originalItems != null)
    {
      for (E item : originalItems)
      {
        // List stores shallow clones so changes may be properly canceled
        listModel.addElement(item.clone());
      }
    }
    
    list.setModel(listModel);
    if (list.getModel().getSize() > 0)
    {
      list.setSelectedIndex(0);
    }
  }

  public void selectItem(@NotNull E item)
  {
    list.setSelectedValue(item, true);
  }

  public JComponent getContentPane()
  {
    return contentPane;
  }

  @SuppressWarnings({"UnusedDeclaration"})
  protected void customizeListCellRendererComponent(JLabel renderer, JList list, E entity, int index, boolean selected,
    boolean cellHasFocus)
  {
  }

  @Nullable
  @Nls
  protected String getMessageKeyWhenNoSelection()
  {
    return null;
  }

  protected abstract void apply(List<E> items) throws ConfigurationException;

  protected abstract F createMainForm();

  protected abstract ActionGroup createActionGroup();

  @NotNull
  protected abstract List<E> getOriginalItems();
}