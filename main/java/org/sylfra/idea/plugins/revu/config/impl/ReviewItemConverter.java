package org.sylfra.idea.plugins.revu.config.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
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

    Project project = getProject(context);
    String filePath = RevuUtils.buildRelativePath(project, reviewItem);
    writer.addAttribute("title", reviewItem.getTitle());
    writer.addAttribute("filePath", filePath);
    writer.addAttribute("lineStart", String.valueOf(reviewItem.getLineStart()));
    writer.addAttribute("lineEnd", String.valueOf(reviewItem.getLineEnd()));
    if (reviewItem.getCategory() != null)
    {
      writer.addAttribute("category", reviewItem.getCategory().getName());
    }
    if (reviewItem.getPriority() != null)
    {
      writer.addAttribute("priority", reviewItem.getPriority().getName());
    }
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
    String title = reader.getAttribute("title");
    String filePath = reader.getAttribute("filePath");
    String lineStart = reader.getAttribute("lineStart");
    String lineEnd = reader.getAttribute("lineEnd");
    String category = reader.getAttribute("category");
    String priority = reader.getAttribute("priority");
    String status = reader.getAttribute("status");

    ReviewItem reviewItem = new ReviewItem();
    reviewItem.setReview(getReview(context));

    Project project = getProject(context);
    VirtualFile file = RevuUtils.findFileFromRelativeFile(project, filePath);
    reviewItem.setFile(file);
    reviewItem.setLineStart(Integer.parseInt(lineStart));
    reviewItem.setLineEnd(Integer.parseInt(lineEnd));
    reviewItem.setCategory(getReview(context).getReviewReferential().getCategory(category));
    reviewItem.setPriority(getReview(context).getReviewReferential().getPriority(priority));
    reviewItem.setStatus(ReviewItem.Status.valueOf(status.toUpperCase()));
    reviewItem.setTitle(title);

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