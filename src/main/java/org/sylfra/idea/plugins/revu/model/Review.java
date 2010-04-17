package org.sylfra.idea.plugins.revu.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.IIssueListener;

import java.util.*;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
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
  private FileScope fileScope;
  private Map<VirtualFile, List<Issue>> issuesByFiles;
  private final transient List<IIssueListener> issueListeners;

  public Review(@Nullable String name)
  {
    this.name = name;
    history = new History();
    issuesByFiles = new HashMap<VirtualFile, List<Issue>>();
    issueListeners = new LinkedList<IIssueListener>();
    dataReferential = new DataReferential(this);
    fileScope = new FileScope();
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
  public FileScope getFileScope()
  {
    return fileScope;
  }

  public void setFileScope(FileScope fileScope)
  {
    this.fileScope = fileScope;
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
    if (isEmbedded())
    {
      return o.isEmbedded() ? name.compareToIgnoreCase(o.getName()) : -1;
    }

    if (o.isEmbedded())
    {
      return 1;
    }

    return name.compareToIgnoreCase(o.getName());
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof Review))
    {
      return false;
    }
    if (this == o)
    {
      return true;
    }

    Review r = (Review) o;
    return new EqualsBuilder()
      .appendSuper(super.equals(o))
      .append(status, r.status)
      .append(embedded, r.embedded)
      .append(shared, r.shared)
      .append(dataReferential, r.dataReferential)
      .append(goal, r.goal)
      .append(extendedReview, r.extendedReview)
      .append(history, r.history)
      .append(issuesByFiles, r.issuesByFiles)
      .append(path, r.path)
      .append(name, r.name)
      .append(fileScope, r.fileScope)
      .isEquals();
  }

  @Override
  public int hashCode()
  {
    return new HashCodeBuilder()
      .append(extendedReview == null ? "" : extendedReview.getName())
      .append(path)
      .append(history)
      .append(name)
      .append(goal)
      .append(shared)
      .append(status)
      .append(embedded)
      .append(dataReferential)
      .append(issuesByFiles)
      .append(fileScope)
      .toHashCode();
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
