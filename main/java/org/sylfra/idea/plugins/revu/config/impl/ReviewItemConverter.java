package org.sylfra.idea.plugins.revu.config.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class ReviewItemConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return ReviewItem.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    ReviewItem reviewItem = (ReviewItem) source;

    writer.addAttribute("filePath", reviewItem.getFilePath());
    writer.addAttribute("lineStart", String.valueOf(reviewItem.getLineStart()));
    writer.addAttribute("lineEnd", String.valueOf(reviewItem.getLineEnd()));
    writer.addAttribute("priority", reviewItem.getPriority().getName());
    writer.addAttribute("status", reviewItem.getStatus().toString().toLowerCase());

    // History
    writer.startNode("history");
    context.convertAnother(reviewItem.getHistory());
    writer.endNode();

    // Desc
    writer.startNode("desc");
    writer.setValue(reviewItem.getDesc());
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    String filePath = reader.getAttribute("filePath");
    String lineStart = reader.getAttribute("lineStart");
    String lineEnd = reader.getAttribute("lineEnd");
    String priority = reader.getAttribute("priority");
    String status = reader.getAttribute("status");

    ReviewItem reviewItem = new ReviewItem();

    reviewItem.setFilePath(filePath);
    reviewItem.setLineStart(Integer.parseInt(lineStart));
    reviewItem.setLineEnd(Integer.parseInt(lineEnd));
    reviewItem.setPriority(getReview(context).getReviewReferential().getPriority(priority));
    reviewItem.setStatus(ReviewItem.Status.valueOf(status.toUpperCase()));

    while (reader.hasMoreChildren())
    {
      reader.moveDown();
      if ("history".equals(reader.getNodeName()))
      {
        reviewItem.setHistory((History) context.convertAnother(reviewItem, History.class));
      }
      else if ("desc".equals(reader.getNodeName()))
      {
        reviewItem.setDesc(reader.getValue());
      }
      reader.moveUp();
    }

    return reviewItem;
  }
}