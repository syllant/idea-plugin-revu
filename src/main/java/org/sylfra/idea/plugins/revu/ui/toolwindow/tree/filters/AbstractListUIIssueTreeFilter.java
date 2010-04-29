package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.filters;

import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractListUiIssueTreeFilter<T> extends AbstractIssueTreeFilter<T>
{
  public static final Object ALL_CHOICE = "ALL";
  public static final Object NONE_CHOICE = "NONE";

  private JScrollPane contentPane;
  private JList jList;

  public JComponent buildUI()
  {
    if (contentPane == null)
    {
      jList = new JList();

      jList.addListSelectionListener(new ListSelectionListener()
      {
        public void valueChanged(ListSelectionEvent e)
        {
          fireValueChanged(e, jList.getSelectedValue());
        }
      });

      jList.setCellRenderer(new ColoredListCellRenderer()
      {
        @Override
        protected void customizeCellRenderer(JList list, Object value, int index, boolean selected, boolean hasFocus)
        {
          String text;
          SimpleTextAttributes textAttributes;
          if (value.equals(ALL_CHOICE))
          {
            text = RevuBundle.message("browsing.filter.values.all.text");
            textAttributes = SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
          }
          else if (value.equals(NONE_CHOICE))
          {
            text = RevuBundle.message("browsing.filter.values.none.text");
            textAttributes = SimpleTextAttributes.GRAY_ATTRIBUTES;
          }
          else
          {
            textAttributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
            text = getListItemText((T) value);
          }

          append(text, textAttributes);
        }
      });

      contentPane = new JScrollPane(jList);
    }

    final List<Object> items = retrieveItemsForReview();
    jList.setModel(new AbstractListModel()
    {
      public int getSize()
      {
        return items.size();
      }

      public Object getElementAt(int index)
      {
        return items.get(index);
      }
    });

    return contentPane;
  }

  protected List<Object> retrieveItemsForReview()
  {
    Review review = getReview();

    boolean includeNoneValue = false;
    SortedSet<T> set = new TreeSet<T>();
    for (Issue issue : review.getIssues())
    {
      List<T> users = retrieveItemsForIssue(issue);
      if (users.isEmpty())
      {
        includeNoneValue = true;
      }
      for (T user : users)
      {
        set.add(user);
      }
    }

    ArrayList<Object> result = new ArrayList<Object>(set);
    result.add(0, ALL_CHOICE);
    if (includeNoneValue)
    {
      result.add(1, NONE_CHOICE);
    }

    return result;
  }

  @Override
  protected boolean match(Issue issue, T filterValue)
  {
    if (ALL_CHOICE.equals(filterValue))
    {
      return true;
    }

    List<T> items = retrieveItemsForIssue(issue);
    return NONE_CHOICE.equals(filterValue) ? items.isEmpty() : items.contains(filterValue);
  }

  protected abstract List<T> retrieveItemsForIssue(@NotNull Issue issue);

  protected abstract String getListItemText(@NotNull T item);
}