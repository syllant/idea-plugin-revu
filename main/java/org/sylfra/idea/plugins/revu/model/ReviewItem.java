package org.sylfra.idea.plugins.revu.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItem implements Serializable
{
  public static enum Status
  {
    TO_RESOLVE,
    RESOLVED,
    CLOSED
  }

  private VirtualFile file;
  private int lineStart;
  private int lineEnd;
  private History history;
  private Review review;
  private User resolver;
  private List<User> recipients;
  private String title;
  private String desc;
  private String resolutionComment;
  private ReviewPriority priority;
  private ReviewCategory category;
  private Status status;
  private String codeAlternative;
  private List<ReviewItem> relations;

  public ReviewItem(Review review)
  {
    this.review = review;
    history = new History();

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

  public
  @NotNull
  Review getReview()
  {
    return review;
  }

  public void setReview(@NotNull Review review)
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

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
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

  public ReviewPriority getPriority()
  {
    return priority;
  }

  public void setPriority(ReviewPriority priority)
  {
    this.priority = priority;
  }

  public ReviewCategory getCategory()
  {
    return category;
  }

  public void setCategory(ReviewCategory category)
  {
    this.category = category;
  }

  public Status getStatus()
  {
    return status;
  }

  public void setStatus(Status status)
  {
    this.status = status;
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
    if (codeAlternative != null ? !codeAlternative.equals(that.codeAlternative) :
      that.codeAlternative != null)
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
    if (priority != null ? !priority.equals(that.priority) : that.priority != null)
    {
      return false;
    }
    if (recipients != null ? !recipients.equals(that.recipients) : that.recipients != null)
    {
      return false;
    }
    if (relations != null ? !relations.equals(that.relations) : that.relations != null)
    {
      return false;
    }
    if (resolutionComment != null ? !resolutionComment.equals(that.resolutionComment) :
      that.resolutionComment != null)
    {
      return false;
    }
    if (resolver != null ? !resolver.equals(that.resolver) : that.resolver != null)
    {
      return false;
    }
    if (review != null ? !review.equals(that.review) : that.review != null)
    {
      return false;
    }
    if (status != that.status)
    {
      return false;
    }
    if (title != null ? !title.equals(that.title) : that.title != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = file != null ? file.hashCode() : 0;
    result = 31 * result + lineStart;
    result = 31 * result + lineEnd;
    result = 31 * result + (history != null ? history.hashCode() : 0);
    result = 31 * result + (resolver != null ? resolver.hashCode() : 0);
    result = 31 * result + (recipients != null ? recipients.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (desc != null ? desc.hashCode() : 0);
    result = 31 * result + (resolutionComment != null ? resolutionComment.hashCode() : 0);
    result = 31 * result + (priority != null ? priority.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
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
      append("title", title).
      append("desc", desc).
      append("resolutionComment", resolutionComment).
      append("priority", priority).
      append("status", status).
      append("codeAlternative", codeAlternative).
      append("relations", relations).
      toString();
  }
}
