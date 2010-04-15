package org.sylfra.idea.plugins.revu.ui.forms.review.referential;

import com.intellij.openapi.project.Project;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.IRevuEntity;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractReferentialForm<T extends IRevuEntity<T>>
  extends AbstractUpdatableForm<ReferentialListHolder<T>>
{
  private final Project project;
  private JPanel contentPane;
  protected TableView<T> table;
  private List<AbstractTableAction<T>> actions;

  protected AbstractReferentialForm(@NotNull Project project)
  {
    this.project = project;
    buildUI();
  }

  private void buildUI()
  {
    createTable();

    actions = buildActions(buildDetailDialogFactory());
    JPanel pnButtons = createButtonsPanel(actions);

    // Used for error icon
    JLabel label = new JLabel("");
    label.setLabelFor(table);
    
    contentPane = new JPanel(new BorderLayout());
    contentPane.add(new JScrollPane(table), BorderLayout.CENTER);
    contentPane.add(pnButtons, BorderLayout.EAST);
    contentPane.add(label, BorderLayout.WEST);
  }

  protected java.util.List<AbstractTableAction<T>> buildActions(IDetailDialogFactory<T> detailDialogFactory)
  {
    java.util.List<AbstractTableAction<T>> result = new ArrayList<AbstractTableAction<T>>();

    AbstractTableAction<T> action = new AddAction<T>(table, detailDialogFactory);
    addUpdatableFormListener(action);
    result.add(action);

    action = new EditAction<T>(table, detailDialogFactory);
    addUpdatableFormListener(action);
    result.add(action);

    action = new RemoveAction<T>(table);
    addUpdatableFormListener(action);
    result.add(action);

    if (isTableSelectionMovable())
    {
      result.add(null);

      action = new MoveUpAction<T>(table);
      addUpdatableFormListener(action);
      result.add(action);

      action = new MoveDownAction<T>(table);
      addUpdatableFormListener(action);
      result.add(action);
    }

    return result;
  }

  protected abstract boolean isTableSelectionMovable();
  protected abstract IDetailDialogFactory<T> buildDetailDialogFactory();

  private JPanel createButtonsPanel(java.util.List<AbstractTableAction<T>> actions)
  {
    JPanel pnButtons = new JPanel();
    pnButtons.setLayout(new BoxLayout(pnButtons, BoxLayout.Y_AXIS));
    pnButtons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    for (Action action : actions)
    {
      if (action == null)
      {
        JPanel separator = new JPanel();
        separator.setMaximumSize(new Dimension(Short.MAX_VALUE, 10));
        pnButtons.add(separator);
      }
      else
      {
        JButton button = new JButton(action);
        button.setMaximumSize(new Dimension(Short.MAX_VALUE, button.getPreferredSize().height));
        pnButtons.add(button);
      }
    }

    return pnButtons;
  }

  private void createTable()
  {
    ReferentialColumnInfo<T, ?>[] columnInfos = buildColumnInfos();
    for (ReferentialColumnInfo<T, ?> columnInfo : columnInfos)
    {
      addUpdatableFormListener(columnInfo);
    }

    table = new TableView<T>(new ListTableModel<T>(columnInfos))
    {
      @Override
      public TableCellRenderer getCellRenderer(int row, int column)
      {
        // Hum, why getCustomizedRenderer is not automatically called by TableView ?
        ColumnInfo columnInfo = getListTableModel().getColumnInfos()[convertColumnIndexToModel(column)];
        return columnInfo.getCustomizedRenderer(getListTableModel().getItems().get(row),
          super.getCellRenderer(row, column));
      }
    };
  }

  protected abstract ReferentialColumnInfo<T, ?>[] buildColumnInfos();

  public boolean isModified(@NotNull ReferentialListHolder<T> data)
  {
    List<T> items = new ArrayList<T>(table.getListTableModel().getItems());
    if (data.getLinkedItems() != null)
    {
      List<T> linkedItems = new ArrayList<T>(data.getLinkedItems());
      linkedItems.removeAll(data.getItems());
      items.removeAll(linkedItems);
    }
    
    return !items.equals(data.getItems());
  }

  protected void internalValidateInput(ReferentialListHolder<T> data)
  {
  }

  protected void internalUpdateUI(ReferentialListHolder<T> data, boolean requestFocus)
  {
    List<T> items;
    if (data == null)
    {
      items = new ArrayList<T>();
    }
    else
    {
      items = new ArrayList<T>(data.getAllItems().size());
      for (T item : data.getAllItems())
      {
        items.add(item.clone());
      }
    }
    
    table.getListTableModel().setItems(items);
// @TODO
//    for (AbstractTableAction<T> action : actions)
//    {
//      if (action != null)
//      {
//        action.checkEnabled();
//      }
//    }
  }

  protected void internalUpdateData(@NotNull ReferentialListHolder<T> data)
  {
    List<T> items = new ArrayList<T>(table.getListTableModel().getItems());
    if (data.getLinkedItems() != null)
    {
      List<T> linkedItems = new ArrayList<T>(data.getLinkedItems());
      linkedItems.removeAll(data.getItems());
      items.removeAll(linkedItems);
    }
    
    data.setItems(items);
  }

  @Override
  protected void internalUpdateWriteAccess(ReferentialListHolder<T> data, @Nullable User user)
  {
  }

  public JComponent getPreferredFocusedComponent()
  {
    return table;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  protected static interface IDetailDialogFactory<T extends IRevuEntity<T>>
  {
    @NotNull AbstractDetailDialog<T> createDialog();
  }

  protected static abstract class AbstractTableAction<T extends IRevuEntity> extends AbstractAction
    implements UpdatableFormListener<ReferentialListHolder<T>>
  {
    protected Review enclosingReview;
    protected ReferentialListHolder<T> referentialListHolder;
    protected final TableView<T> table;
    private User currentUser;

    protected AbstractTableAction(String name, TableView<T> table)
    {
      super(name);
      this.table = table;
      table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {
        public void valueChanged(ListSelectionEvent e)
        {
          checkEnabled();
        }
      });
      checkEnabled();
    }

    public void checkEnabled()
    {
      setEnabled((currentUser != null) && (currentUser.hasRole(User.Role.ADMIN)) && isEnabledForSelection());
    }

    protected boolean isFromLinkedReferential()
    {
      if ((referentialListHolder == null) || (referentialListHolder.getLinkedItems() == null))
      {
        return false;
      }

      for (T item : table.getSelection())
      {
        if (referentialListHolder.getLinkedItems().contains(item))
        {
          return true;
        }
      }

      return false;
    }

    public void uiUpdated(Review enclosingReview, @Nullable ReferentialListHolder<T> data)
    {
      this.enclosingReview = enclosingReview;
      currentUser = RevuUtils.getCurrentUser(enclosingReview);
      referentialListHolder = data;

      checkEnabled();
    }

    public void dataUpdated(@NotNull ReferentialListHolder<T> data)
    {
    }

    protected abstract boolean isEnabledForSelection();
  }

  protected static abstract class AbstractDialogTableAction<T extends IRevuEntity<T>> extends AbstractTableAction<T>
  {
    protected final IDetailDialogFactory<T> detailDialogFactory;

    protected AbstractDialogTableAction(String name, TableView<T> table, IDetailDialogFactory<T> detailDialogFactory)
    {
      super(name, table);
      this.detailDialogFactory = detailDialogFactory;
    }

    protected void showDialog(T data)
    {
      AbstractDetailDialog<T> dialog = detailDialogFactory.createDialog();
      dialog.show(enclosingReview, data);
      if (dialog.isOK())
      {
        // Not very optimized, but this is what IDEA seems to use to update ListTableModel items
        // com.intellij.profile.codeInspection.ui.ProjectProfileConfigurable.MyTableModel.addRow()
        java.util.List<T> items = new ArrayList<T>(table.getListTableModel().getItems());
        T editedData = updateItems(dialog, items);
        table.getListTableModel().setItems(items);

        table.clearSelection();
        table.addSelection(editedData);
      }
    }

    protected abstract T updateItems(AbstractDetailDialog<T> dialog, java.util.List<T> items);
  }

  private static final class AddAction<T extends IRevuEntity<T>> extends AbstractDialogTableAction<T>
  {
    private AddAction(TableView<T> table, IDetailDialogFactory<T> detailDialogFactory)
    {
      super(RevuBundle.message("general.add.action"), table, detailDialogFactory);
    }

    public void actionPerformed(ActionEvent e)
    {
      showDialog(null);
    }

    protected T updateItems(AbstractDetailDialog<T> dialog, List<T> items)
    {
      T data = dialog.getData();
      items.add(data);

      return data;
    }

    protected boolean isEnabledForSelection()
    {
      return true;
    }
  }

  private static final class EditAction<T extends IRevuEntity<T>> extends AbstractDialogTableAction<T>
  {
    private EditAction(TableView<T> table, IDetailDialogFactory<T> detailDialogFactory)
    {
      super(RevuBundle.message("general.edit.action"), table, detailDialogFactory);
      table.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          if ((isEnabled()) && (e.getClickCount() == 2))
          {
            actionPerformed(null);
          }
        }
      });
    }

    public void actionPerformed(ActionEvent e)
    {
      showDialog(table.getSelectedObject());
    }

    protected T updateItems(AbstractDetailDialog<T> dialog, List<T> items)
    {
      T currentData = table.getSelectedObject();
      T newData = dialog.getData();
      items.set(items.indexOf(currentData), newData);

      return newData;
    }

    protected boolean isEnabledForSelection()
    {
      return ((!isFromLinkedReferential()) && (table.getSelectedRow() > -1));
    }
  }

  private static final class RemoveAction<T extends IRevuEntity> extends AbstractTableAction<T>
  {
    private RemoveAction(TableView<T> table)
    {
      super(RevuBundle.message("general.remove.action"), table);
    }

    public void actionPerformed(ActionEvent e)
    {
      TableUtil.removeSelectedItems(table);
    }

    protected boolean isEnabledForSelection()
    {
      return ((!isFromLinkedReferential()) && (table.getSelectedRow() > -1));
    }
  }

  private static abstract class AbstractMoveTableAction<T extends IRevuEntity> extends AbstractTableAction<T>
  {
    protected AbstractMoveTableAction(String name, TableView<T> table)
    {
      super(name, table);
    }

    protected void move(int offset)
    {
      // Not very optimized, but this is what IDEA seems to use to update ListTableModel items
      java.util.List<T> items = new ArrayList<T>(table.getListTableModel().getItems());
      T currentObject = table.getSelectedObject();
      T otherObject = items.get(table.getSelectedRow() + offset);
      items.set(table.getSelectedRow(), otherObject);
      items.set(table.getSelectedRow() + offset, currentObject);
      table.getListTableModel().setItems(items);

      table.clearSelection();
      table.addSelection(currentObject);
    }
  }

  private static final class MoveUpAction<T extends IRevuEntity> extends AbstractMoveTableAction<T>
  {
    private MoveUpAction(TableView<T> table)
    {
      super(RevuBundle.message("general.moveUp.action"), table);
    }

    public void actionPerformed(ActionEvent e)
    {
      move(-1);
    }

    protected boolean isEnabledForSelection()
    {
      return ((!isFromLinkedReferential()) && (table.getSelectedRow() > 0));
    }
  }

  private static final class MoveDownAction<T extends IRevuEntity> extends AbstractMoveTableAction<T>
  {
    private MoveDownAction(TableView<T> table)
    {
      super(RevuBundle.message("general.moveDown.action"), table);
    }

    public void actionPerformed(ActionEvent e)
    {
      move(1);
    }

    protected boolean isEnabledForSelection()
    {
      return ((!isFromLinkedReferential())
        && (table.getSelectedRow() > -1) && (table.getSelectedRow() < table.getRowCount() - 1));
    }
  }

  protected abstract static class ReferentialColumnInfo<Item extends IRevuEntity, Aspect>
    extends ColumnInfo<Item, Aspect> implements UpdatableFormListener<ReferentialListHolder<Item>>
  {
    private ReferentialListHolder<Item> referentialListHolder;

    public ReferentialColumnInfo(String name)
    {
      super(name);
    }

    @Override
    public TableCellRenderer getCustomizedRenderer(final Item item, final TableCellRenderer renderer)
    {
      return new TableCellRenderer()
      {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
          int row, int column)
        {
          Component result = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
          if ((referentialListHolder != null)
            && ((referentialListHolder.getLinkedItems() != null)
            && (referentialListHolder.getLinkedItems().contains(item))
            && (!referentialListHolder.getItems().contains(item))))
          {
            result.setForeground(UIUtil.getInactiveTextColor());
          }
          else
          {
            result.setForeground(UIUtil.getActiveTextColor());
          }

          return result;
        }
      };
    }

    public void uiUpdated(Review enclosingReview, @Nullable ReferentialListHolder<Item> data)
    {
      this.referentialListHolder = data;
    }

    public void dataUpdated(@NotNull ReferentialListHolder<Item> data)
    {
    }
  }
}
