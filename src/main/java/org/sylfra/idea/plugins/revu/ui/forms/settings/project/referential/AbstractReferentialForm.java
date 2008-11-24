package org.sylfra.idea.plugins.revu.ui.forms.settings.project.referential;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractReferentialForm<T> extends AbstractUpdatableForm<java.util.List<T>>
{
  private final Project project;
  private JPanel contentPane;
  protected TableView<T> table;

  protected AbstractReferentialForm(@NotNull Project project)
  {
    this.project = project;
    buildUI();
  }

  private void buildUI()
  {
    createTable();

    JPanel pnButtons = createButtonsPanel(getActionsForButtonPanel(buildDetailDialogFactory()));

    contentPane = new JPanel(new BorderLayout());
    contentPane.add(new JScrollPane(table), BorderLayout.CENTER);
    contentPane.add(pnButtons, BorderLayout.EAST);
  }

  protected java.util.List<AbstractTableAction<T>> getActionsForButtonPanel(IDetailDialogFactory<T> detailDialogFactory)
  {
    java.util.List<AbstractTableAction<T>> result = new ArrayList<AbstractTableAction<T>>();

    result.add(new AddAction<T>(table, detailDialogFactory));
    result.add(new EditAction<T>(table, detailDialogFactory));
    result.add(new RemoveAction<T>(table));

    if (isTableSelectionMovable())
    {
      result.add(null);
      result.add(new MoveUpAction<T>(table));
      result.add(new MoveDownAction<T>(table));
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
        separator.setPreferredSize(new Dimension(10, 10));
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
    table = new TableView<T>(new ListTableModel<T>(buildColumnInfos()));
  }

  protected abstract ColumnInfo[] buildColumnInfos();

  public boolean isModified(@NotNull java.util.List<T> data)
  {
    return !table.getListTableModel().getItems().equals(data);
  }

  protected void internalValidateInput()
  {
  }

  protected void internalUpdateUI(@NotNull java.util.List<T> data)
  {
    table.getListTableModel().setItems(data);
  }

  protected void internalUpdateData(@NotNull java.util.List<T> data)
  {
    data.clear();
    data.addAll(table.getListTableModel().getItems());
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

  protected static interface IDetailDialogFactory<T>
  {
    @NotNull AbstractDetailDialog<T> createDialog();
  }

  protected static abstract class AbstractTableAction<T> extends AbstractAction
  {
    protected final TableView<T> table;

    protected AbstractTableAction(String name, TableView<T> table)
    {
      super(name);
      this.table = table;
      table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
      {
        public void valueChanged(ListSelectionEvent e)
        {
          updateEnableState();
        }
      });
      updateEnableState();
    }

    protected abstract void updateEnableState();
  }

  protected static abstract class AbstractDialogTableAction<T> extends AbstractTableAction<T>
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
      dialog.show(data);
      if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE)
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

  private static final class AddAction<T> extends AbstractDialogTableAction<T>
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

    protected void updateEnableState()
    {
      setEnabled(true);
    }
  }

  private static final class EditAction<T> extends AbstractDialogTableAction<T>
  {
    private EditAction(TableView<T> table, IDetailDialogFactory<T> detailDialogFactory)
    {
      super(RevuBundle.message("general.edit.action"), table, detailDialogFactory);
      table.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          if (e.getClickCount() == 2)
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

    protected void updateEnableState()
    {
      setEnabled(table.getSelectedRow() > -1);
    }
  }

  private static final class RemoveAction<T> extends AbstractTableAction<T>
  {
    private RemoveAction(TableView<T> table)
    {
      super(RevuBundle.message("general.remove.action"), table);
    }

    public void actionPerformed(ActionEvent e)
    {
      TableUtil.removeSelectedItems(table);
    }

    protected void updateEnableState()
    {
      setEnabled(table.getSelectedRow() > -1);
    }
  }

  private static abstract class AbstractMoveTableAction<T> extends AbstractTableAction<T>
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

  private static final class MoveUpAction<T> extends AbstractMoveTableAction<T>
  {
    private MoveUpAction(TableView<T> table)
    {
      super(RevuBundle.message("general.moveUp.action"), table);
    }

    public void actionPerformed(ActionEvent e)
    {
      move(-1);
    }

    protected void updateEnableState()
    {
      setEnabled(table.getSelectedRow() > 0);
    }
  }

  private static final class MoveDownAction<T> extends AbstractMoveTableAction<T>
  {
    private MoveDownAction(TableView<T> table)
    {
      super(RevuBundle.message("general.moveDown.action"), table);
    }

    public void actionPerformed(ActionEvent e)
    {
      move(1);
    }

    protected void updateEnableState()
    {
      setEnabled(table.getSelectedRow() < table.getRowCount() - 1);
    }
  }
}
