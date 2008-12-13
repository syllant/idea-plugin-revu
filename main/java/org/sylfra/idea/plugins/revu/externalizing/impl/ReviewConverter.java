package org.sylfra.idea.plugins.revu.externalizing.impl;

import com.intellij.openapi.vfs.VirtualFile;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.DataReferential;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;

import java.util.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class ReviewConverter extends AbstractConverter
{
  private static final String REVU_SCHEMA_ID = "http://plugins.intellij.net/revu";
  private static final String REVU_SCHEMA_LOCATION = "http://plugins.intellij.net/xstructure/ns/revu_1_0.xsd";

  public boolean canConvert(Class type)
  {
    return Review.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    Review review = (Review) source;

    writer.addAttribute("xmlns", REVU_SCHEMA_ID);
    writer.addAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    writer.addAttribute("xsi:schemaLocation", REVU_SCHEMA_ID + " " + REVU_SCHEMA_LOCATION);

    writer.addAttribute("name", review.getName());
    writer.addAttribute("template", String.valueOf(review.isTemplate()));
    writer.addAttribute("active", String.valueOf(review.isActive()));
    writer.addAttribute("shared", String.valueOf(review.isShared()));

    if (review.getExtendedReview() != null)
    {
      writer.addAttribute("extends", review.getExtendedReview().getName());
    }

    // History
    writer.startNode("history");
    context.convertAnother(review.getHistory());
    writer.endNode();

    // Desc
    if (review.getGoal() != null)
    {
      writer.startNode("goal");
      writer.setValue(review.getGoal());
      writer.endNode();
    }

    // Referential
    writer.startNode("referential");
    context.convertAnother(review.getDataReferential());
    writer.endNode();

    // Items
    writer.startNode("items");
    SortedMap<VirtualFile, List<Issue>> itemsByFiles
      = new TreeMap<VirtualFile, List<Issue>>(new VirtualFileComparator());
    itemsByFiles.putAll(review.getItemsByFiles());
    for (List<Issue> items : itemsByFiles.values())
    {
      for (Issue item : items)
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
    String name = reader.getAttribute("name");
    String template = reader.getAttribute("template");
    String active = reader.getAttribute("active");
    String shared = reader.getAttribute("shared");
    String extendedReviewName = reader.getAttribute("extends");

    Review review = getReview(context);
    
    review.setName(name);
    review.setTemplate("true".equals(template));
    review.setActive("true".equals(active));
    review.setShared("true".equals(shared));

    if (extendedReviewName != null)
    {
      review.setExtendedReview(new Review(extendedReviewName));
    }

    while (reader.hasMoreChildren())
    {
      reader.moveDown();
      if ("items".equals(reader.getNodeName()))
      {
        List<Issue> items = new ArrayList<Issue>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          items.add((Issue) context.convertAnother(items, Issue.class));
          reader.moveUp();
        }
        review.setItems(items);
      }
      else if ("history".equals(reader.getNodeName()))
      {
        review.setHistory((History) context.convertAnother(review, History.class));
      }
      else if ("goal".equals(reader.getNodeName()))
      {
        review.setGoal(reader.getValue());
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