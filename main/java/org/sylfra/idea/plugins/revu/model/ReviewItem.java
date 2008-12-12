package org.sylfra.idea.plugins.revu.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItem extends AbstractRevuEntity<ReviewItem> implements IRevuHistoryHolderEntity<ReviewItem>
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
  private ItemPriority priority;
  private List<ItemTag> tags;
  private ItemResolutionType resolutionType;
  private ItemResolutionStatus resolutionStatus;

//  private String resolutionComment;
//  private List<User> recipients;
//  private String codeAlternative;
//  private List<ReviewItem> relations;

  public ReviewItem()
  {
    history = new History();
    tags = new ArrayList<ItemTag>();
    lineStart = -1;
    lineEnd = -1;
  }

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
  public ItemPriority getPriority()
  {
    return priority;
  }

  public void setPriority(@Nullable ItemPriority priority)
  {
    this.priority = priority;
  }

  public List<ItemTag> getTags()
  {
    return tags;
  }

  public void setTags(List<ItemTag> tags)
  {
    this.tags = tags;
  }

  public ItemResolutionType getResolutionType()
  {
    return resolutionType;
  }

  public void setResolutionType(ItemResolutionType resolutionType)
  {
    this.resolutionType = resolutionType;
  }

  public ItemResolutionStatus getResolutionStatus()
  {
    return resolutionStatus;
  }

  public void setResolutionStatus(ItemResolutionStatus resolutionStatus)
  {
    this.resolutionStatus = resolutionStatus;
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

  public int compareTo(ReviewItem o)
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
    result = 31 * result + (resolutionType != null ? resolutionType.hashCode() : 0);
    result = 31 * result + (resolutionStatus != null ? resolutionStatus.hashCode() : 0);

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

    ReviewItem that = (ReviewItem) o;

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
    if (resolutionStatus != that.resolutionStatus)
    {
      return false;
    }
    if (resolutionType != null ? !resolutionType.equals(that.resolutionType) : that.resolutionType != null)
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
      append("resolutionType", resolutionType).
      append("resolutionStatus", resolutionStatus).
      toString();
  }
}
