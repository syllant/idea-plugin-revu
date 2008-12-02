package org.sylfra.idea.plugins.revu.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItem extends AbstractRevuEntity<ReviewItem> implements IHistoryHolder
{
  public static enum LocationType
  {
    GLOBAL,
    FILE,
    LINE_RANGE
  }

  private VirtualFile file;
  private int lineStart;
  private int lineEnd;
  private History history;
  private Review review;
  private User resolver;
  private List<User> recipients;
  private String summary;
  private String desc;
  private String resolutionComment;
  private ItemPriority priority;
  private ItemCategory category;
  private ItemResolutionType resolutionType;
  private ItemResolutionStatus resolutionStatus;
  private String codeAlternative;
  private List<ReviewItem> relations;

  public ReviewItem()
  {
    history = new History();
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

  public List<User> getRecipients()
  {
    return recipients;
  }

  public void setRecipients(List<User> recipients)
  {
    this.recipients = recipients;
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

  public String getResolutionComment()
  {
    return resolutionComment;
  }

  public void setResolutionComment(String resolutionComment)
  {
    this.resolutionComment = resolutionComment;
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

  public ItemCategory getCategory()
  {
    return category;
  }

  public void setCategory(ItemCategory category)
  {
    this.category = category;
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

  public String getCodeAlternative()
  {
    return codeAlternative;
  }

  public void setCodeAlternative(String codeAlternative)
  {
    this.codeAlternative = codeAlternative;
  }

  public List<ReviewItem> getRelations()
  {
    return relations;
  }

  public void setRelations(List<ReviewItem> relations)
  {
    this.relations = relations;
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

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ReviewItem that = (ReviewItem) o;

    if (lineEnd != that.lineEnd) return false;
    if (lineStart != that.lineStart) return false;
    if (category != null ? !category.equals(that.category) : that.category != null) return false;
    if (codeAlternative != null ? !codeAlternative.equals(that.codeAlternative) : that.codeAlternative != null)
      return false;
    if (desc != null ? !desc.equals(that.desc) : that.desc != null) return false;
    if (file != null ? !file.equals(that.file) : that.file != null) return false;
    if (history != null ? !history.equals(that.history) : that.history != null) return false;
    if (priority != null ? !priority.equals(that.priority) : that.priority != null) return false;
    if (recipients != null ? !recipients.equals(that.recipients) : that.recipients != null) return false;
    if (relations != null ? !relations.equals(that.relations) : that.relations != null) return false;
    if (resolutionComment != null ? !resolutionComment.equals(that.resolutionComment) : that.resolutionComment != null)
      return false;
    if (resolutionStatus != that.resolutionStatus) return false;
    if (resolutionType != null ? !resolutionType.equals(that.resolutionType) : that.resolutionType != null)
      return false;
    if (resolver != null ? !resolver.equals(that.resolver) : that.resolver != null) return false;
    if (review != null ? !review.getPath().equals(that.review.getPath()) : that.review != null) return false;
    if (summary != null ? !summary.equals(that.summary) : that.summary != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = file != null ? file.hashCode() : 0;
    result = 31 * result + lineStart;
    result = 31 * result + lineEnd;
    result = 31 * result + (history != null ? history.hashCode() : 0);
    result = 31 * result + (((review != null) && (review.getPath() != null)) ? review.getPath().hashCode() : 0);
    result = 31 * result + (resolver != null ? resolver.hashCode() : 0);
    result = 31 * result + (recipients != null ? recipients.hashCode() : 0);
    result = 31 * result + (summary != null ? summary.hashCode() : 0);
    result = 31 * result + (desc != null ? desc.hashCode() : 0);
    result = 31 * result + (resolutionComment != null ? resolutionComment.hashCode() : 0);
    result = 31 * result + (priority != null ? priority.hashCode() : 0);
    result = 31 * result + (category != null ? category.hashCode() : 0);
    result = 31 * result + (resolutionType != null ? resolutionType.hashCode() : 0);
    result = 31 * result + (resolutionStatus != null ? resolutionStatus.hashCode() : 0);
    result = 31 * result + (codeAlternative != null ? codeAlternative.hashCode() : 0);
    result = 31 * result + (relations != null ? relations.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("file", file).
      append("lineStart", lineStart).
      append("lineEnd", lineEnd).
      append("history", history).
      append("review", review).
      append("resolver", resolver).
      append("recipients", recipients).
      append("summary", summary).
      append("desc", desc).
      append("resolutionComment", resolutionComment).
      append("priority", priority).
      append("resolutionStatus", resolutionStatus).
      append("resolutionType", resolutionType).
      append("codeAlternative", codeAlternative).
      append("relations", relations).
      toString();
  }

  public static final class ComparatorCreatedOn implements Comparator<ReviewItem>
  {
    public int compare(ReviewItem o1, ReviewItem o2)
    {
      return o1.getHistory().getCreatedOn().compareTo(o2.getHistory().getCreatedOn());
    }
  }
}
