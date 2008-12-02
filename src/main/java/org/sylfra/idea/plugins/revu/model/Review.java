package org.sylfra.idea.plugins.revu.model;

import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;

import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class Review extends AbstractRevuEntity<Review> implements IHistoryHolder
{
  private Review extendedReview;
  private String path;
  private History history;
  private String title;
  private String desc;
  private boolean template;
  private boolean shared;
  private boolean active;
  private boolean embedded;
  private DataReferential dataReferential;
  private Map<VirtualFile, List<ReviewItem>> itemsByFiles;
  private final transient List<IReviewItemListener> reviewItemListeners;

  public Review(@Nullable String title)
  {
    this.title = title;
    history = new History();
    itemsByFiles = new HashMap<VirtualFile, List<ReviewItem>>();
    reviewItemListeners = new LinkedList<IReviewItemListener>();
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
  public Map<VirtualFile, List<ReviewItem>> getItemsByFiles()
  {
    return Collections.unmodifiableMap(itemsByFiles);
  }

  public void setItems(List<ReviewItem> items)
  {
    itemsByFiles.clear();
    for (ReviewItem item : items)
    {
      List<ReviewItem> fileItems = itemsByFiles.get(item.getFile());
      if (fileItems == null)
      {
        fileItems = new ArrayList<ReviewItem>();
        itemsByFiles.put(item.getFile(), fileItems);
      }
      fileItems.add(item);
    }
  }

  @NotNull
  public List<ReviewItem> getItems(VirtualFile file)
  {
    List<ReviewItem> fileItems = itemsByFiles.get(file);
    return (fileItems == null) ? new ArrayList<ReviewItem>(0) : Collections.unmodifiableList(fileItems);
  }

  @NotNull
  public List<ReviewItem> getItems()
  {
    List<ReviewItem> result = new ArrayList<ReviewItem>();

    for (List<ReviewItem> items : itemsByFiles.values())
    {
      for (ReviewItem item : items)
      {
        result.add(item);
      }
    }

    return result;
  }

  public void addItem(ReviewItem item)
  {
    List<ReviewItem> fileItems = itemsByFiles.get(item.getFile());
    if (fileItems == null)
    {
      fileItems = new ArrayList<ReviewItem>();
      itemsByFiles.put(item.getFile(), fileItems);
    }
    fileItems.add(item);

    for (IReviewItemListener listener : reviewItemListeners)
    {
      listener.itemAdded(item);
    }
  }

  public void removeItem(ReviewItem item)
  {
    List<ReviewItem> fileItems = itemsByFiles.get(item.getFile());
    if (fileItems != null)
    {
      fileItems.remove(item);
    }

    for (IReviewItemListener listener : reviewItemListeners)
    {
      listener.itemDeleted(item);
    }
  }

  public void fireItemUpdated(ReviewItem item)
  {
    for (IReviewItemListener listener : reviewItemListeners)
    {
      listener.itemUpdated(item);
    }
  }

  public void addReviewItemListener(IReviewItemListener listener)
  {
    reviewItemListeners.add(listener);
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
    if (desc != null ? !desc.equals(review.desc) : review.desc != null)
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
    if (reviewItemListeners != null ? !reviewItemListeners.equals(review.reviewItemListeners) :
      review.reviewItemListeners != null)
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
    int result = extendedReview != null ? extendedReview.hashCode() : 0;
    result = 31 * result + (path != null ? path.hashCode() : 0);
    result = 31 * result + (history != null ? history.hashCode() : 0);
    result = 31 * result + (title != null ? title.hashCode() : 0);
    result = 31 * result + (desc != null ? desc.hashCode() : 0);
    result = 31 * result + (template ? 1 : 0);
    result = 31 * result + (shared ? 1 : 0);
    result = 31 * result + (active ? 1 : 0);
    result = 31 * result + (embedded ? 1 : 0);
    result = 31 * result + (dataReferential != null ? dataReferential.hashCode() : 0);
    result = 31 * result + (itemsByFiles != null ? itemsByFiles.hashCode() : 0);
    result = 31 * result + (reviewItemListeners != null ? reviewItemListeners.hashCode() : 0);
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
      append("embedded", embedded).
      append("itemsByFiles", itemsByFiles).
      append("reviewItemListeners", reviewItemListeners).
      append("dataReferential", dataReferential).
      toString();
  }
}
