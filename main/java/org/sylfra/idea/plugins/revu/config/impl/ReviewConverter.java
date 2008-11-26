package org.sylfra.idea.plugins.revu.config.impl;

import com.intellij.openapi.vfs.VirtualFile;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.DataReferential;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class ReviewConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return Review.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    Review review = (Review) source;

    writer.addAttribute("title", review.getTitle());
    writer.addAttribute("active", String.valueOf(review.isActive()));
    writer.addAttribute("shared", String.valueOf(review.isShared()));

    // Referential
    writer.startNode("referential");
    context.convertAnother(review.getDataReferential());
    writer.endNode();

    // History
    writer.startNode("history");
    context.convertAnother(review.getHistory());
    writer.endNode();

    // Desc
    writer.startNode("desc");
    writer.setValue(review.getDesc());
    writer.endNode();

    // Items
    writer.startNode("items");
    SortedMap<VirtualFile, List<ReviewItem>> itemsByFiles
      = new TreeMap<VirtualFile, List<ReviewItem>>(new VirtualFileComparator());
    itemsByFiles.putAll(review.getItemsByFiles());
    for (List<ReviewItem> items : itemsByFiles.values())
    {
      for (ReviewItem item : items)
      {
        writer.startNode("item");
        context.convertAnother(item);
        writer.endNode();
      }
    }
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    String title = reader.getAttribute("title");
    String active = reader.getAttribute("active");
    String shared = reader.getAttribute("shared");

    Review review = getReview(context);
    
    review.setTitle(title);
    review.setActive("true".equals(active));
    review.setShared("true".equals(shared));

    while (reader.hasMoreChildren())
    {
      reader.moveDown();
      if ("items".equals(reader.getNodeName()))
      {
        List<ReviewItem> items = new ArrayList<ReviewItem>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          items.add((ReviewItem) context.convertAnother(items, ReviewItem.class));
          reader.moveUp();
        }
        review.setItems(items);
      }
      else if ("history".equals(reader.getNodeName()))
      {
        review.setHistory((History) context.convertAnother(review, History.class));
      }
      else if ("desc".equals(reader.getNodeName()))
      {
        review.setDesc(reader.getValue());
      }
      else if ("referential".equals(reader.getNodeName()))
      {
        review.setDataReferential((DataReferential)
          context.convertAnother(review, DataReferential.class));
      }
      reader.moveUp();
    }

    return review;
  }

  private class VirtualFileComparator implements Comparator<VirtualFile>
  {
    public int compare(VirtualFile o1, VirtualFile o2)
    {
      return o1.getPath().compareTo(o2.getPath());
    }
  }
}