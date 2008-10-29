package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.IReviewListener;

import java.io.Serializable;
import java.util.*;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class Review implements Serializable
{
  private History history;
  private String title;
  private String desc;
  private boolean active;
  private ReviewReferential reviewReferential;
  private Map<String, List<ReviewItem>> itemsByFilePath;
  private final transient List<IReviewListener> reviewListeners;

  public Review()
  {
    history = new History();
    itemsByFilePath = new HashMap<String, List<ReviewItem>>();
    reviewListeners = new LinkedList<IReviewListener>();
    reviewReferential = new ReviewReferential();
  }

  @NotNull
  public History getHistory()
  {
    return history;
  }

  public void setHistory(@NotNull History history)
  {
    this.history = history;
  }

  public boolean isActive()
  {
    return active;
  }

  public void setActive(boolean active)
  {
    this.active = active;
  }

  @NotNull
  public String getTitle()
  {
    return title;
  }

  public void setTitle(@NotNull String title)
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

  @NotNull
  public ReviewReferential getReviewReferential()
  {
    return reviewReferential;
  }

  public void setReviewReferential(@NotNull ReviewReferential reviewReferential)
  {
    this.reviewReferential = reviewReferential;
  }

  @NotNull
  public Map<String, List<ReviewItem>> getItemsByFilePath()
  {
    return Collections.unmodifiableMap(itemsByFilePath);
  }

  public void setItems(@NotNull List<ReviewItem> items)
  {
    itemsByFilePath.clear();
    for (ReviewItem item : items)
    {
      List<ReviewItem> fileItems = itemsByFilePath.get(item.getFilePath());
      if (fileItems == null)
      {
        fileItems = new ArrayList<ReviewItem>();
        itemsByFilePath.put(item.getFilePath(), fileItems);
      }
      fileItems.add(item);
    }
  }

  @Nullable
  public List<ReviewItem> getItems(String filePath)
  {
    List<ReviewItem> fileItems = itemsByFilePath.get(filePath);
    return (fileItems == null) ? null : Collections.unmodifiableList(fileItems);
  }

  public void addItem(@NotNull ReviewItem item)
  {
    String filePath = item.getFilePath();
    List<ReviewItem> fileItems = itemsByFilePath.get(filePath);
    if (fileItems == null)
    {
      fileItems = new ArrayList<ReviewItem>();
      itemsByFilePath.put(filePath, fileItems);
    }
    fileItems.add(item);

    for (IReviewListener listener : reviewListeners)
    {
      listener.itemAdded(item);
    }
  }

  public void addReviewListener(@NotNull IReviewListener listener)
  {
    reviewListeners.add(listener);
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
    if (desc != null ? !desc.equals(review.desc) : review.desc != null)
    {
      return false;
    }
    if (history != null ? !history.equals(review.history) : review.history != null)
    {
      return false;
    }
    if (itemsByFilePath != null ? !itemsByFilePath.equals(review.itemsByFilePath) :
      review.itemsByFilePath != null)
    {
      return false;
    }
    if (reviewListeners != null ? !reviewListeners.equals(review.reviewListeners) :
      review.reviewListeners != null)
    {
      return false;
    }
    if (reviewReferential != null ? !reviewReferential.equals(review.reviewReferential) :
      review.reviewReferential != null)
    {
      return false;
    }
    if (title != null ? !title.equals(review.title) : review.title != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = history != null ? history.hashCode() : 0;
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (desc != null ? desc.hashCode() : 0);
    result = 31 * result + (active ? 1 : 0);
    result = 31 * result + (itemsByFilePath != null ? itemsByFilePath.hashCode() : 0);
    result = 31 * result + (reviewListeners != null ? reviewListeners.hashCode() : 0);
    result = 31 * result + (reviewReferential != null ? reviewReferential.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("history", history).
      append("title", title).
      append("desc", desc).
      append("active", active).
      append("itemsByFilePath", itemsByFilePath).
      append("reviewListeners", reviewListeners).
      append("reviewReferential", reviewReferential).
      toString();
  }
}
