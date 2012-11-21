package org.sylfra.idea.plugins.revu.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class Issue extends AbstractRevuEntity<Issue> implements IRevuHistoryHolderEntity<Issue>
{
  public static enum LocationType
  {
    GLOBAL,
    FILE,
    LINE_RANGE
  }

  private VirtualFile file;
  private String vcsRev;
  private String localRev;
  private int lineStart;
  private int lineEnd;
  private int hash;
  private History history;
  private Review review;
  private User resolver;
  private String summary;
  private String desc;
  private IssuePriority priority;
  private List<IssueTag> tags;
  private IssueStatus status;
  private List<User> assignees;
  private List<IssueNote> notes;

  public Issue()
  {
    history = new History();
    tags = new ArrayList<IssueTag>();
    assignees = new ArrayList<User>();
    notes = new ArrayList<IssueNote>();
    lineStart = -1;
    lineEnd = -1;
  }

  @Nullable
  public VirtualFile getFile()
  {
    return file;
  }

  public void setFile(VirtualFile file)
  {
    this.file = file;
  }

  public String getVcsRev()
  {
    return vcsRev;
  }

  public void setVcsRev(String vcsRev)
  {
    this.vcsRev = vcsRev;
  }

  public String getLocalRev()
  {
    return localRev;
  }

  public void setLocalRev(String localRev)
  {
    this.localRev = localRev;
  }

  public int getLineStart()
  {
    return lineStart;
  }

  public void setLineStart(int lineStart)
  {
    this.lineStart = lineStart;
  }

  public int getLineEnd()
  {
    return lineEnd;
  }

  public void setLineEnd(int lineEnd)
  {
    this.lineEnd = lineEnd;
  }

  public int getHash()
  {
    return hash;
  }

  public void setHash(int hash)
  {
    this.hash = hash;
  }

  public History getHistory()
  {
    return history;
  }

  public void setHistory(History history)
  {
    this.history = history;
  }

  public Review getReview()
  {
    return review;
  }

  public void setReview(Review review)
  {
    this.review = review;
  }

  public User getResolver()
  {
    return resolver;
  }

  public void setResolver(User resolver)
  {
    this.resolver = resolver;
  }

  public String getSummary()
  {
    return summary;
  }

  public void setSummary(String summary)
  {
    this.summary = summary;
  }

  public String getDesc()
  {
    return desc;
  }

  public void setDesc(String desc)
  {
    this.desc = desc;
  }

  @Nullable
  public IssuePriority getPriority()
  {
    return priority;
  }

  public void setPriority(@Nullable IssuePriority priority)
  {
    this.priority = priority;
  }

  public List<IssueTag> getTags()
  {
    return tags;
  }

  public void setTags(List<IssueTag> tags)
  {
    this.tags = tags;
  }

  public IssueStatus getStatus()
  {
    return status;
  }

  public void setStatus(IssueStatus status)
  {
    this.status = status;
  }

  public List<User> getAssignees()
  {
    return assignees;
  }

  public void setAssignees(List<User> assignees)
  {
    this.assignees = assignees;
  }

  public List<IssueNote> getNotes()
  {
    return notes;
  }

  public void setNotes(List<IssueNote> notes)
  {
    this.notes = notes;
  }

  public LocationType getLocationType()
  {
    if (file == null)
    {
      return LocationType.GLOBAL;
    }

    if (lineStart == -1)
    {
      return LocationType.FILE;
    }

    return LocationType.LINE_RANGE;
  }

  public int compareTo(Issue o)
  {
    return history.getCreatedOn().compareTo(o.getHistory().getCreatedOn());
  }

  @Override
  public int hashCode()
  {
    int result = file != null ? file.hashCode() : 0;
    result = 31 * result + (vcsRev != null ? vcsRev.hashCode() : 0);
    result = 31 * result + (localRev != null ? localRev.hashCode() : 0);
    result = 31 * result + lineStart;
    result = 31 * result + lineEnd;
    result = 31 * result + hash;
    result = 31 * result + (history != null ? history.hashCode() : 0);
    result = 31 * result + (resolver != null ? resolver.hashCode() : 0);
    result = 31 * result + (summary != null ? summary.hashCode() : 0);
    result = 31 * result + (desc != null ? desc.hashCode() : 0);
    result = 31 * result + (priority != null ? priority.hashCode() : 0);
    result = 31 * result + (tags != null ? tags.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    result = 31 * result + (assignees != null ? assignees.hashCode() : 0);
    result = 31 * result + (notes != null ? notes.hashCode() : 0);

    // /!\ Cyclic call with review
    result = 31 * result + (review != null ? review.getName().hashCode() : 0);

    return result;
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

    Issue that = (Issue) o;

    if (lineEnd != that.lineEnd)
    {
      return false;
    }
    if (lineStart != that.lineStart)
    {
      return false;
    }
    if (hash != that.hash)
    {
      return false;
    }
    if (tags != null ? !tags.equals(that.tags) : that.tags != null)
    {
      return false;
    }
    if (desc != null ? !desc.equals(that.desc) : that.desc != null)
    {
      return false;
    }
    if (file != null ? !file.equals(that.file) : that.file != null)
    {
      return false;
    }
    if (history != null ? !history.equals(that.history) : that.history != null)
    {
      return false;
    }
    if (localRev != null ? !localRev.equals(that.localRev) : that.localRev != null)
    {
      return false;
    }
    if (priority != null ? !priority.equals(that.priority) : that.priority != null)
    {
      return false;
    }
    if (status != that.status)
    {
      return false;
    }
    if (resolver != null ? !resolver.equals(that.resolver) : that.resolver != null)
    {
      return false;
    }
    if (summary != null ? !summary.equals(that.summary) : that.summary != null)
    {
      return false;
    }
    if (vcsRev != null ? !vcsRev.equals(that.vcsRev) : that.vcsRev != null)
    {
      return false;
    }
    if (assignees != null ? !assignees.equals(that.assignees) : that.assignees != null)
    {
      return false;
    }
    if (notes != null ? !notes.equals(that.notes) : that.notes != null)
    {
      return false;
    }

    // /!\ Cyclic call with review
    if (review != null ? !review.getName().equals(that.review.getName()) : that.review != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("file", file).
      append("vcsRev", vcsRev).
      append("localRev", localRev).
      append("lineStart", lineStart).
      append("lineEnd", lineEnd).
      append("hash", hash).
      append("history", history).
      append("review", review).
      append("resolver", resolver).
      append("summary", summary).
      append("desc", desc).
      append("priority", priority).
      append("tags", tags).
      append("status", status).
      append("assignees", assignees).
      append("notes", notes).
      toString();
  }

  public String getPresentableSummary()
  {
    return getSummary();
  }

  public void copyFrom(Issue source)
  {
    setFile(source.getFile());
    setVcsRev(source.getVcsRev());
    setLocalRev(source.getLocalRev());
    setLineStart(source.getLineStart());
    setLineEnd(source.getLineEnd());
    setHash(source.getHash());
    setHistory(source.getHistory().clone());
    setReview(source.getReview());
    setResolver(source.getResolver());
    setSummary(source.getSummary());
    setDesc(source.getDesc());
    final IssuePriority sourcePriority = source.getPriority();
    if (sourcePriority != null) {
      setPriority(sourcePriority.clone());
    }
    getTags().addAll(source.getTags());
    setStatus(source.getStatus());
    getAssignees().addAll(source.getAssignees());
    for (IssueNote note : source.getNotes()) {
      getNotes().add(note.clone());
    }
  }
}
