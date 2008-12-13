package org.sylfra.idea.plugins.revu.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.IIssueListener;

import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class Review extends AbstractRevuEntity<Review> implements IRevuHistoryHolderEntity<Review>
{
  private Review extendedReview;
  private String path;
  private History history;
  private String name;
  private String goal;
  private boolean template;
  private boolean shared;
  private boolean active;
  private boolean embedded;
  private DataReferential dataReferential;
  private Map<VirtualFile, List<Issue>> itemsByFiles;
  private final transient List<IIssueListener> issueListeners;

  public Review(@Nullable String name)
  {
    this.name = name;
    history = new History();
    itemsByFiles = new HashMap<VirtualFile, List<Issue>>();
    issueListeners = new LinkedList<IIssueListener>();
    dataReferential = new DataReferential(this);
  }

  public Review()
  {
    this(null);
  }

  public Review getExtendedReview()
  {
    return extendedReview;
  }

  public void setExtendedReview(Review extendedReview)
  {
    this.extendedReview = extendedReview;
  }

  public String getPath()
  {
    return path;
  }

  public void setPath(String path)
  {
    this.path = path;
  }

  public History getHistory()
  {
    return history;
  }

  public void setHistory(History history)
  {
    this.history = history;
  }

  public boolean isTemplate()
  {
    return template;
  }

  public void setTemplate(boolean template)
  {
    this.template = template;
  }

  public boolean isShared()
  {
    return shared;
  }

  public void setShared(boolean shared)
  {
    this.shared = shared;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(boolean active)
  {
    this.active = active;
  }

  public boolean isEmbedded()
  {
    return embedded;
  }

  public void setEmbedded(boolean embedded)
  {
    this.embedded = embedded;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getGoal()
  {
    return goal;
  }

  public void setGoal(String goal)
  {
    this.goal = goal;
  }

  @NotNull
  public DataReferential getDataReferential()
  {
    return dataReferential;
  }

  public void setDataReferential(DataReferential dataReferential)
  {
    this.dataReferential = dataReferential;
  }

  @NotNull
  public Map<VirtualFile, List<Issue>> getItemsByFiles()
  {
    return Collections.unmodifiableMap(itemsByFiles);
  }

  public void setItems(List<Issue> items)
  {
    itemsByFiles.clear();
    for (Issue item : items)
    {
      List<Issue> fileItems = itemsByFiles.get(item.getFile());
      if (fileItems == null)
      {
        fileItems = new ArrayList<Issue>();
        itemsByFiles.put(item.getFile(), fileItems);
      }
      fileItems.add(item);
    }
  }

  @NotNull
  public List<Issue> getItems(@NotNull VirtualFile file)
  {
    List<Issue> fileItems = itemsByFiles.get(file);
    return (fileItems == null) ? new ArrayList<Issue>(0) : Collections.unmodifiableList(fileItems);
  }

  @NotNull
  public boolean hasItems(@NotNull VirtualFile file)
  {
    return itemsByFiles.containsKey(file);
  }

  @NotNull
  public List<Issue> getItems()
  {
    List<Issue> result = new ArrayList<Issue>();

    for (List<Issue> items : itemsByFiles.values())
    {
      for (Issue item : items)
      {
        result.add(item);
      }
    }

    return result;
  }

  public void addItem(Issue item)
  {
    List<Issue> fileItems = itemsByFiles.get(item.getFile());
    if (fileItems == null)
    {
      fileItems = new ArrayList<Issue>();
      itemsByFiles.put(item.getFile(), fileItems);
    }
    fileItems.add(item);

    for (IIssueListener listener : issueListeners)
    {
      listener.itemAdded(item);
    }
  }

  public void removeItem(Issue item)
  {
    List<Issue> fileItems = itemsByFiles.get(item.getFile());
    if (fileItems != null)
    {
      fileItems.remove(item);
    }

    for (IIssueListener listener : issueListeners)
    {
      listener.itemDeleted(item);
    }
  }

  public void fireItemUpdated(Issue item)
  {
    for (IIssueListener listener : issueListeners)
    {
      listener.itemUpdated(item);
    }
  }

  public void addIssueListener(IIssueListener listener)
  {
    issueListeners.add(listener);
  }

  public void removeIssueListener(IIssueListener listener)
  {
    issueListeners.remove(listener);
  }

  public void clearIssuesListeners()
  {
    issueListeners.clear();
  }

  public void copyFrom(@NotNull Review otherReview)
  {
    dataReferential.copyFrom(otherReview.getDataReferential());
  }

  @Override
  public Review clone()
  {
    Review clone = super.clone();

    DataReferential referentialClone = clone.getDataReferential().clone();
    clone.setDataReferential(referentialClone);
    referentialClone.setReview(clone);

    return clone;
  }

  public int compareTo(Review o)
  {
    return name.compareTo(o.getName());
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    Review review = (Review) o;

    if (active != review.active)
    {
      return false;
    }
    if (embedded != review.embedded)
    {
      return false;
    }
    if (shared != review.shared)
    {
      return false;
    }
    if (template != review.template)
    {
      return false;
    }
    if (dataReferential != null ? !dataReferential.equals(review.dataReferential) : review.dataReferential != null)
    {
      return false;
    }
    if (goal != null ? !goal.equals(review.goal) : review.goal != null)
    {
      return false;
    }
    if (extendedReview != null ? !extendedReview.equals(review.extendedReview) : review.extendedReview != null)
    {
      return false;
    }
    if (history != null ? !history.equals(review.history) : review.history != null)
    {
      return false;
    }
    if (itemsByFiles != null ? !itemsByFiles.equals(review.itemsByFiles) : review.itemsByFiles != null)
    {
      return false;
    }
    if (path != null ? !path.equals(review.path) : review.path != null)
    {
      return false;
    }
    if (name != null ? !name.equals(review.name) : review.name != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = extendedReview != null ? extendedReview.getName().hashCode() : 0;

    result = 31 * result + (path != null ? path.hashCode() : 0);
    result = 31 * result + (history != null ? history.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (goal != null ? goal.hashCode() : 0);
    result = 31 * result + (template ? 1 : 0);
    result = 31 * result + (shared ? 1 : 0);
    result = 31 * result + (active ? 1 : 0);
    result = 31 * result + (embedded ? 1 : 0);
    result = 31 * result + (dataReferential != null ? dataReferential.hashCode() : 0);
    result = 31 * result + (itemsByFiles != null ? itemsByFiles.hashCode() : 0);

    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("history", history).
      append("name", name).
      append("goal", goal).
      append("active", active).
      append("embedded", embedded).
      append("itemsByFiles", itemsByFiles).
      append("issueListeners", issueListeners).
      append("dataReferential", dataReferential).
      toString();
  }
}
