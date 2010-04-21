package org.sylfra.idea.plugins.revu.ui.forms;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.IRevuUniqueNameHolderEntity;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Used to interface settings inside Settings panel
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractListUpdatableForm<E extends IRevuUniqueNameHolderEntity<E>,
  F extends AbstractUpdatableForm<E>> implements Configurable, Disposable
{
  protected final Project project;
  protected JComponent contentPane;
  protected JComponent toolBar;
  protected RevuEntityJList<E> list;
  protected JLabel lbMessageWholePane;
  protected JLabel lbMessageMainPane;
  protected F mainForm;
  private final IdentityHashMap<E, E> originalItemsMap;
  private JPanel mainPane;

  public AbstractListUpdatableForm(@NotNull Project project)
  {
    this.project = project;
    originalItemsMap = new IdentityHashMap<E,E>();

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

    // List
    JPanel pnList = new JPanel(new BorderLayout());
    pnList.setMinimumSize(new Dimension(50, 0));
    pnList.setMinimumSize(new Dimension(150, 0));

    list = new RevuEntityJList<E>(createListSelectedEntityDataKey(), createListAllEntitiesDataKeys());

    // Toolbar
    ActionToolbar actionToolbar = ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, createActionGroup(), true);
    actionToolbar.setTargetComponent(list);
    toolBar = actionToolbar.getComponent();

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
          super.setSelectionInterval(index1, index1);
          updateFormUI();
        }
      }
    });
  }

  public void addItem(E item)
  {
    DefaultListModel model = (DefaultListModel) list.getModel();

    // Put item according to sort
    int index = 0;
    //noinspection unchecked
    while ((index < model.getSize()) && (((E) model.getElementAt(index)).compareTo(item) < 0))
    {
      index++;
    }

    model.add(index, item);

    list.setSelectedValue(item, true);
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

    // Item count
    int itemCount = list.getModel().getSize();
    if (itemCount != originalItemsMap.size())
    {
      return true;
    }

    // Current edited item
    E selectedValue = (E) list.getSelectedValue();
    if (mainForm.isModified(selectedValue))
    {
      return true;
    }

    // Other lists
    for (int i = 0; i < itemCount; i++)
    {
      E item = (E) list.getModel().getElementAt(i);
      if (!item.equals(originalItemsMap.get(item)))
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
    Map<E, E> items = new HashMap<E, E>(itemCount);
    for (int i=0; i < itemCount; i++)
    {
      //noinspection unchecked
      E item = (E) list.getModel().getElementAt(i);
      items.put(item, originalItemsMap.get(item));
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

    originalItemsMap.clear();

    List<E> originalItems = getOriginalItems();
    for (E item : originalItems)
    {
      // List stores shallow clones so changes may be properly canceled
      E clone = item.clone();
      originalItemsMap.put(clone, item);
      listModel.addElement(clone);
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

  protected abstract void apply(Map<E, E> items) throws ConfigurationException;

  protected abstract F createMainForm();

  protected abstract ActionGroup createActionGroup();

  protected abstract DataKey createListSelectedEntityDataKey();

  protected abstract DataKey createListAllEntitiesDataKeys();

  @NotNull
  protected abstract List<E> getOriginalItems();

  protected static class RevuEntityJList<E extends IRevuUniqueNameHolderEntity<E>> extends JList implements DataProvider
  {
    private final DataKey listSelectedEntityDataKey;
    private final DataKey listAllEntitiesDataKeys;

    public RevuEntityJList(DataKey listSelectedEntityDataKey, DataKey listAllEntitiesDataKeys)
    {
      this.listSelectedEntityDataKey = listSelectedEntityDataKey;
      this.listAllEntitiesDataKeys = listAllEntitiesDataKeys;
    }

    public Object getData(@NonNls String dataId)
    {
      if (listSelectedEntityDataKey.getName().equals(dataId))
      {
        return getSelectedValue();
      }

      if (listAllEntitiesDataKeys.getName().equals(dataId))
      {
        List<E> items = new ArrayList<E>(getModel().getSize());
        for (int i=0; i<getModel().getSize(); i++)
        {
          //noinspection unchecked
          items.add((E) getModel().getElementAt(i));
        }

        return items;
      }

      return null;
    }
  }
}