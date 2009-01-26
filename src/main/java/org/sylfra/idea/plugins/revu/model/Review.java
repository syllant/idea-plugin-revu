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
public class Review extends AbstractRevuEntity<Review> implements IRevuHistoryHolderEntity<Review>,
  IRevuUniqueNameHolderEntity<Review>
{
  private Review extendedReview;
  private String path;
  private History history;
  private String name;
  private String goal;
  private boolean shared;
  private ReviewStatus status;
  private boolean embedded;
  private DataReferential dataReferential;
  private Map<VirtualFile, List<Issue>> issuesByFiles;
  private final transient List<IIssueListener> issueListeners;

  public Review(@Nullable String name)
  {
    this.name = name;
    history = new History();
    issuesByFiles = new HashMap<VirtualFile, List<Issue>>();
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

  public boolean isShared()
  {
    return shared;
  }

  public void setShared(boolean shared)
  {
    this.shared = shared;
  }

  public ReviewStatus getStatus()
  {
    return status;
  }

  public void setStatus(ReviewStatus status)
  {
    this.status = status;
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
  public Map<VirtualFile, List<Issue>> getIssuesByFiles()
  {
    return Collections.unmodifiableMap(issuesByFiles);
  }

  public void setIssues(List<Issue> issues)
  {
    issuesByFiles.clear();
    for (Issue issue : issues)
    {
      List<Issue> fileIssues = issuesByFiles.get(issue.getFile());
      if (fileIssues == null)
      {
        fileIssues = new ArrayList<Issue>();
        issuesByFiles.put(issue.getFile(), fileIssues);
      }
      fileIssues.add(issue);
    }
  }

  @NotNull
  public List<Issue> getIssues(@NotNull VirtualFile file)
  {
    List<Issue> fileIssues = issuesByFiles.get(file);
    return (fileIssues == null) ? new ArrayList<Issue>(0) : Collections.unmodifiableList(fileIssues);
  }

  @NotNull
  public boolean hasIssues(@NotNull VirtualFile file)
  {
    return issuesByFiles.containsKey(file);
  }

  @NotNull
  public List<Issue> getIssues()
  {
    List<Issue> result = new ArrayList<Issue>();

    for (List<Issue> issues : issuesByFiles.values())
    {
      for (Issue issue : issues)
      {
        result.add(issue);
      }
    }

    return result;
  }

  public void addIssue(Issue issue)
  {
    List<Issue> fileIssues = issuesByFiles.get(issue.getFile());
    if (fileIssues == null)
    {
      fileIssues = new ArrayList<Issue>();
      issuesByFiles.put(issue.getFile(), fileIssues);
    }
    fileIssues.add(issue);

    // Defensive copy against concurrent modifications
    List<IIssueListener> copy = new ArrayList<IIssueListener>(issueListeners);
    for (IIssueListener listener : copy)
    {
      listener.issueAdded(issue);
    }
  }

  public void removeIssue(Issue issue)
  {
    List<Issue> fileIssues = issuesByFiles.get(issue.getFile());
    if (fileIssues != null)
    {
      fileIssues.remove(issue);
    }

    // Defensive copy against concurrent modifications
    List<IIssueListener> copy = new ArrayList<IIssueListener>(issueListeners);
    for (IIssueListener listener : copy)
    {
      listener.issueDeleted(issue);
    }
  }

  public void fireIssueUpdated(Issue issue)
  {
    // Defensive copy against concurrent modifications
    List<IIssueListener> copy = new ArrayList<IIssueListener>(issueListeners);
    for (IIssueListener listener : copy)
    {
      listener.issueUpdated(issue);
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

  public boolean hasIssueListener(IIssueListener listener)
  {
    return issueListeners.contains(listener);
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

    if (status != review.status)
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
    if (issuesByFiles != null ? !issuesByFiles.equals(review.issuesByFiles) : review.issuesByFiles != null)
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
    result = 31 * result + (shared ? 1 : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (embedded ? 1 : 0);
    result = 31 * result + (dataReferential != null ? dataReferential.hashCode() : 0);
    result = 31 * result + (issuesByFiles != null ? issuesByFiles.hashCode() : 0);

    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("history", history).
      append("name", name).
      append("goal", goal).
      append("status", status).
      append("embedded", embedded).
      append("issuesByFiles", issuesByFiles).
      append("issueListeners", issueListeners).
      append("dataReferential", dataReferential).
      toString();
  }
}
