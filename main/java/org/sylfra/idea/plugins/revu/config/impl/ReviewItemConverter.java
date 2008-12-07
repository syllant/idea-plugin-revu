package org.sylfra.idea.plugins.revu.config.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.ItemResolutionStatus;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

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
    String filePath = RevuVfsUtils.buildRelativePath(project, reviewItem.getFile());
    writer.addAttribute("summary", reviewItem.getSummary());
    writer.addAttribute("filePath", filePath);
    if (reviewItem.getVcsRev() != null)
    {
      writer.addAttribute("vcsRev", reviewItem.getVcsRev());
    }
    if (reviewItem.getLocalRev() != null)
    {
      writer.addAttribute("localRev", reviewItem.getLocalRev());
    }
    writer.addAttribute("lineStart", String.valueOf(reviewItem.getLineStart()));
    writer.addAttribute("lineEnd", String.valueOf(reviewItem.getLineEnd()));
    writer.addAttribute("hash", String.valueOf(reviewItem.getHash()));
    if (reviewItem.getCategory() != null)
    {
      writer.addAttribute("category", reviewItem.getCategory().getName());
    }
    if (reviewItem.getPriority() != null)
    {
      writer.addAttribute("priority", reviewItem.getPriority().getName());
    }
    writer.addAttribute("resolutionStatus", reviewItem.getResolutionStatus().toString().toLowerCase());
    if (reviewItem.getResolutionType() != null)
    {
      writer.addAttribute("resolutionType", reviewItem.getResolutionType().getName());
    }

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
    String summary = reader.getAttribute("summary");
    String filePath = reader.getAttribute("filePath");
    String vcsRev = reader.getAttribute("vcsRev");
    String localRev = reader.getAttribute("localRev");
    String lineStart = reader.getAttribute("lineStart");
    String hash = reader.getAttribute("hash");
    String lineEnd = reader.getAttribute("lineEnd");
    String category = reader.getAttribute("category");
    String priority = reader.getAttribute("priority");
    String resolutionStatus = reader.getAttribute("resolutionStatus");
    String resolutionType = reader.getAttribute("resolutionType");

    ReviewItem reviewItem = new ReviewItem();
    reviewItem.setReview(getReview(context));

    Project project = getProject(context);
    VirtualFile file = RevuVfsUtils.findVFileFromRelativeFile(project, filePath);
    reviewItem.setFile(file);
    reviewItem.setVcsRev(vcsRev);
    reviewItem.setLocalRev(localRev);
    reviewItem.setLineStart(Integer.parseInt(lineStart));
    reviewItem.setLineEnd(Integer.parseInt(lineEnd));
    if (hash != null)
    {
      reviewItem.setHash(Integer.parseInt(hash));
    }
    if (category != null)
    {
      reviewItem.setCategory(getReview(context).getDataReferential().getItemCategory(category));
    }
    if (priority != null)
    {
      reviewItem.setPriority(getReview(context).getDataReferential().getItemPriority(priority));
    }
    reviewItem.setResolutionStatus(ItemResolutionStatus.valueOf(resolutionStatus.toUpperCase()));
    if (resolutionType != null)
    {
      reviewItem.setResolutionType(getReview(context).getDataReferential().getItemResolutionType(resolutionType));
    }
    reviewItem.setSummary(summary);

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