package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.TableUtil;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueNotesForm extends AbstractIssueForm
{
  private JPanel contentPane;
  private JButton bnAdd;
  private TableView<IssueNote> notesTable;
  private JLabel lbCount;
  private NotesTableModel notesTableModel;

  public IssueNotesForm(@NotNull Project project)
  {
    super(project);
    installListeners();
  }

  private void createUIComponents()
  {
    notesTableModel = new NotesTableModel();
    notesTable = new TableView<IssueNote>(notesTableModel)
    {
      @Override
      public boolean editCellAt(int row, int column, EventObject e)
      {
        return super.editCellAt(row, column, e);
      }
    };
    notesTable.setModel(notesTableModel);
    notesTable.setShowVerticalLines(false);
    notesTable.setGridColor(Color.LIGHT_GRAY);
    notesTable.setTableHeader(null);
  }

  private void installListeners()
  {
    bnAdd.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Review review = getEnclosingReview();
        if (review != null)
        {
          IssueNote note = new IssueNote();
          note.setHistory(RevuUtils.buildHistory(review));
          note.setContent(RevuBundle.message("issueForm.notes.defaultContent.text"));

          addNote(note);
        }
      }
    });

    // Make the cell editor for first col active to avoid force user having to click once for enter in editing mode,
    // and once for delete/modify action
    notesTable.addMouseMotionListener(new MouseAdapter()
    {
      @Override
      public void mouseMoved(MouseEvent e)
      {
        int column = notesTable.columnAtPoint(e.getPoint());
        if (column == 0)
        {
          if (notesTable.isEditing())
          {
            return;
          }
          int row = notesTable.rowAtPoint(e.getPoint());
          notesTable.editCellAt(row, column);
        }
      }
    });

    notesTableModel.addTableModelListener(new TableModelListener()
    {
      public void tableChanged(TableModelEvent e)
      {
        lbCount.setText(RevuBundle.message("issueForm.notes.count.text", notesTableModel.getRowCount()));
      }
    });
  }

  private void addNote(IssueNote note)
  {
    // Bad code but this seems to be how ListTableModel is usually used...
    final java.util.List<IssueNote> notes = new ArrayList<IssueNote>(notesTableModel.getItems());
    notes.add(note);
    notesTableModel.setItems(notes);

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        int row = notes.size() - 1;
        notesTable.getSelectionModel().setSelectionInterval(row, row);
        TableUtil.scrollSelectionToVisible(notesTable);

        if (notesTable.editCellAt(row, 1))
        {
          final JTextComponent textEditor = (JTextComponent)
            ((JScrollPane) notesTable.getEditorComponent()).getViewport().getView();
          textEditor.requestFocusInWindow();
          textEditor.selectAll();
        }
      }
    });
  }

  private void removeNote(IssueNote note)
  {
    // Bad code but this seems to be how ListTableModel is usually used...
    java.util.List<IssueNote> notes = new ArrayList<IssueNote>(notesTableModel.getItems());
    notes.remove(note);
    notesTableModel.setItems(notes);
  }

  private boolean isLastNote(IssueNote note)
  {
    java.util.List<IssueNote> notes = notesTableModel.getItems();
    return (notes.indexOf(note) == notes.size() - 1);
  }

  private int indexOfNote(IssueNote note)
  {
    return notesTableModel.getItems().indexOf(note);
  }

  public JComponent getPreferredFocusedComponent()
  {
    return bnAdd;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  public boolean isModified(@NotNull Issue data)
  {
    notesTable.stopEditing();
    return !data.getNotes().equals(notesTableModel.getItems());
  }

  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    RevuUtils.setWriteAccess(((user != null)
      && ((currentIssue == null) || (IssueStatus.CLOSED != currentIssue.getStatus()))), bnAdd);
  }

  protected void internalValidateInput()
  {
  }

  protected void internalUpdateUI(@Nullable Issue data, boolean requestFocus)
  {
    super.internalUpdateUI(data, requestFocus);

    java.util.List<IssueNote> notes;
    if (data == null)
    {
      notes = Collections.emptyList();
    }
    else
    {
      notes = cloneNotes(data.getNotes());
    }

    notesTableModel.setItems(notes);
  }

  private java.util.List<IssueNote> cloneNotes(List<IssueNote> issueNotes)
  {
    java.util.List<IssueNote> clone = new ArrayList<IssueNote>(issueNotes.size());
    for (IssueNote note : issueNotes)
    {
      clone.add(note.clone());
    }

    return clone;
  }

  protected void internalUpdateData(@NotNull Issue data)
  {
    notesTable.stopEditing();
    data.setNotes(cloneNotes(notesTableModel.getItems()));
  }

  private boolean isEditable(@NotNull IssueNote note)
  {
    Review enclosingReview = getEnclosingReview();
    if (enclosingReview == null)
    {
      return false;
    }

    User user = RevuUtils.getCurrentUser(enclosingReview);
    return ((currentIssue != null) && (user != null) && (IssueStatus.CLOSED != currentIssue.getStatus())
      && ((note.getHistory().getCreatedBy().equals(user)) || (user.hasRole(User.Role.ADMIN))));
  }

  private class NotesTableModel extends ListTableModel<IssueNote>
  {
    private NotesTableModel()
    {
      super(
        new ColumnInfo<IssueNote, Object>("1")
        {
          private NoteInfoPanel noteInfoPanel = new NoteInfoPanel();

          @Override
          public int getWidth(JTable table)
          {
            // @TODO dynamic ?
            return 150;
          }

          public Object valueOf(IssueNote note)
          {
            return note;
          }

          @Override
          public boolean isCellEditable(IssueNote note)
          {
            return isEditable(note);
          }

          @Override
          public TableCellEditor getEditor(IssueNote o)
          {
            return new NoteInfoEditor();
          }

          @Override
          public TableCellRenderer getRenderer(final IssueNote note)
          {
            return new TableCellRenderer()
            {
              public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus,
                int row, int column)
              {
                noteInfoPanel.updateNote(note);
                return noteInfoPanel;
              }
            };
          }
        },
        new ColumnInfo<IssueNote, String>("2")
        {
          public String valueOf(IssueNote note)
          {
            return note.getContent();
          }

          @Override
          public void setValue(IssueNote note, String value)
          {
            note.setContent(value);
          }

          @Override
          public boolean isCellEditable(IssueNote note)
          {
            return isEditable(note);
          }

          @Override
          public TableCellRenderer getRenderer(IssueNote note)
          {
            final JTextArea textArea = new JTextArea();
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setBackground(UIUtil.getPanelBackground());

            return new TableCellRenderer()
            {
              public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus,
                int row, int column)
              {
                textArea.setText((String) value);

                int minHeight = table.getCellRenderer(row, 0)
                  .getTableCellRendererComponent(table, table.getModel().getValueAt(row, 0), isSelected, hasFocus, row,
                    0).getMinimumSize().height;
                table.setRowHeight(row, Math.max(minHeight, (int) textArea.getPreferredSize().getHeight()));
                return textArea;
              }
            };
          }

          @Override
          public TableCellEditor getEditor(IssueNote o)
          {
            return new NoteContentEditor();
          }
        }
      );

    }
  }

  public class NoteContentEditor extends DefaultCellEditor
  {
    public NoteContentEditor()
    {
      super(new JTextField());
      final JTextArea textArea = new JTextArea();
      textArea.setWrapStyleWord(true);
      textArea.setLineWrap(true);
      JScrollPane scrollPane = new JScrollPane(textArea);
      scrollPane.setBorder(null);
      editorComponent = scrollPane;

      delegate = new DefaultCellEditor.EditorDelegate()
      {
        @Override
        public void setValue(Object value)
        {
          textArea.setText((String) value);
        }

        @Override
        public Object getCellEditorValue()
        {
          return textArea.getText();
        }

        @Override
        public boolean startCellEditing(EventObject anEvent)
        {
          textArea.setCaretPosition(textArea.getText().length());
          return super.startCellEditing(anEvent);
        }
      };
    }
  }

  public class NoteInfoEditor extends DefaultCellEditor
  {
    public NoteInfoEditor()
    {
      super(new JTextField());
      final NoteInfoPanel noteInfoPanel = new NoteInfoPanel();
      editorComponent = noteInfoPanel;

      delegate = new DefaultCellEditor.EditorDelegate()
      {
        @Override
        public void setValue(Object value)
        {
          noteInfoPanel.updateNote((IssueNote) value);
        }
      };
    }

    @Override
    public boolean isCellEditable(EventObject anEvent)
    {
      return true;
    }
  }

  private class NoteInfoPanel extends JPanel
  {
    private final DateFormat DATE_FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

    private JLabel lbUser;
    private JLabel lbDate;
    private JLabel lbDelete;
    private JLabel lbModify;
    private IssueNote currentNote;

    private NoteInfoPanel()
    {
      super(new GridBagLayout());
      lbUser = new JLabel();
      lbUser.setFont(lbUser.getFont().deriveFont(Font.BOLD));

      lbDate = new JLabel();

      lbDelete = new JLabel(RevuBundle.message("issueForm.notes.delete.text"));
      lbDelete.setHorizontalAlignment(SwingConstants.CENTER);
      lbDelete.setVerticalAlignment(SwingConstants.TOP);
      lbDelete.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lbDelete.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          int result = Messages.showOkCancelDialog(NoteInfoPanel.this,
            RevuBundle.message("issueForm.notes.delete.confirm.text"),
            RevuBundle.message("issueForm.notes.delete.confirm.title"),
            Messages.getQuestionIcon());
          if (result == DialogWrapper.OK_EXIT_CODE)
          {
            removeNote(currentNote);
          }
        }
      });

      lbModify = new JLabel(RevuBundle.message("issueForm.notes.modify.text"));
      lbModify.setHorizontalAlignment(SwingConstants.CENTER);
      lbModify.setVerticalAlignment(SwingConstants.TOP);
      lbModify.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      lbModify.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mouseClicked(MouseEvent e)
        {
          if (notesTable.editCellAt(indexOfNote(currentNote), 1))
          {
            final JTextComponent textEditor = (JTextComponent)
              ((JScrollPane) notesTable.getEditorComponent()).getViewport().getView();
            textEditor.requestFocusInWindow();
          }
        }
      });
      configureUI();
    }

    private void configureUI()
    {
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.gridwidth = 2;
      c.anchor = GridBagConstraints.NORTHWEST;
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1.0;
      c.weighty = 0.0;
      add(lbUser, c);

      c.gridy++;
      c.insets = new Insets(0, 0, 5, 0);
      add(lbDate, c);

      c.gridy++;
      c.gridwidth = 1;
      c.weighty = 1.0;
      c.weightx = 0.5;
      c.fill = GridBagConstraints.BOTH;
      c.insets = new Insets(5, 0, 5, 0);
      add(lbModify, c);

      c.gridx++;
      add(lbDelete, c);
    }

    private void updateNote(IssueNote note)
    {
      currentNote = note;

      lbUser.setText(note.getHistory().getCreatedBy().getDisplayName());
      lbDate.setText(DATE_FORMATTER.format(note.getHistory().getCreatedOn()));

      User user = RevuUtils.getCurrentUser(getEnclosingReview());
      boolean mayUpdate = (user != null)
        && (((note.getHistory().getCreatedBy().getLogin().equals(user.getLogin())) && (isLastNote(note)))
        || (user.hasRole(User.Role.ADMIN)));
      lbModify.setVisible(mayUpdate);
      lbDelete.setVisible(mayUpdate);
    }
  }
}