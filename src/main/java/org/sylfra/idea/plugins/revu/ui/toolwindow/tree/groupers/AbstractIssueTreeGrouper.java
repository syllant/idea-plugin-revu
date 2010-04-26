package org.sylfra.idea.plugins.revu.ui.toolwindow.tree.groupers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Issue;

import java.util.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractIssueTreeGrouper<T extends Comparable<T>>
  implements IIssueTreeGrouper<AbstractIssueTreeGrouper.DefaultNamedGroup>
{
  public SortedMap<AbstractIssueTreeGrouper.DefaultNamedGroup, SortedSet<Issue>> group(List<Issue> issues)
  {
    SortedMap<AbstractIssueTreeGrouper.DefaultNamedGroup, SortedSet<Issue>> result
      = new TreeMap<AbstractIssueTreeGrouper.DefaultNamedGroup, SortedSet<Issue>>();
    for (Issue issue : issues)
    {
      List<AbstractIssueTreeGrouper.DefaultNamedGroup> groups = findGroups(issue);

      for (AbstractIssueTreeGrouper.DefaultNamedGroup group : groups)
      {
        SortedSet<Issue> groupIssues = result.get(group);
        if (groupIssues == null)
        {
          groupIssues = new TreeSet<Issue>(createIssueComparator());
          result.put(group, groupIssues);
        }

        groupIssues.add(issue);
      }
    }

    return result;
  }

  protected Comparator<Issue> createIssueComparator()
  {
    return new Comparator<Issue>()
    {
      public int compare(Issue o1, Issue o2)
      {
        return o1.getSummary().compareToIgnoreCase(o2.getSummary());
      }
    };
  }

  protected List<AbstractIssueTreeGrouper.DefaultNamedGroup> findGroups(@NotNull Issue issue)
  {
    List<T> groupObjects = getGroupObjects(issue);
    if (groupObjects.isEmpty())
    {
      return Arrays.asList(new AbstractIssueTreeGrouper.DefaultNamedGroup(null));
    }
    
    List<AbstractIssueTreeGrouper.DefaultNamedGroup>  result
      = new ArrayList<AbstractIssueTreeGrouper.DefaultNamedGroup>(groupObjects.size());
    for (T groupObject : groupObjects)
    {
      result.add(new AbstractIssueTreeGrouper.DefaultNamedGroup(groupObject));
    }

    return result;
  }

  @NotNull
  protected abstract List<T> getGroupObjects(Issue issue);

  protected abstract String getGroupName(@NotNull T entity);

  final class DefaultNamedGroup implements INamedGroup, Comparable<AbstractIssueTreeGrouper.DefaultNamedGroup>
  {
    private T groupObject;

    private DefaultNamedGroup(@Nullable T groupObject)
    {
      this.groupObject = groupObject;
    }

    public String getName()
    {
      return (groupObject == null) ? null : getGroupName(groupObject);
    }

    public int compareTo(AbstractIssueTreeGrouper.DefaultNamedGroup o)
    {
      return (groupObject == null)
        ? ((o.groupObject == null) ? 0 : -1)
        : ((o.groupObject == null) ? 1 : groupObject.compareTo((T) o.groupObject));
    }
  }
}
