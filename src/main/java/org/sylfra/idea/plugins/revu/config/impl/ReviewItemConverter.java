package org.sylfra.idea.plugins.revu.config.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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

    List<ItemTag> tagList = reviewItem.getTags();
    if ((tagList != null) && (!tagList.isEmpty()))
    {
      SortedSet<ItemTag> tags = new TreeSet<ItemTag>(tagList);
      writer.addAttribute("tags", ConverterUtils.toString(tags, true));
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
    String tags = reader.getAttribute("tags");
    String priority = reader.getAttribute("priority");
    String resolutionStatus = reader.getAttribute("resolutionStatus");
    String resolutionType = reader.getAttribute("resolutionType");

    Review review = getReview(context);

    ReviewItem reviewItem = new ReviewItem();
    reviewItem.setReview(review);

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
    if (tags != null)
    {
      String[] tagNames = tags.split(",");
      List<ItemTag> tagSet = new ArrayList<ItemTag>();
      for (String tagName : tagNames)
      {
        ItemTag itemTag = review.getDataReferential().getItemTag(tagName);
        if (itemTag == null)
        {
          // @TODO report error to user
          logger.warn("Can't find tag in referential. Tag:'" + tagName + "', review: " + review.getPath());
        }
        else
        {
          tagSet.add(itemTag);
        }
      }
      reviewItem.setTags(tagSet);
    }
    if (priority != null)
    {
      reviewItem.setPriority(review.getDataReferential().getItemPriority(priority));
    }
    reviewItem.setResolutionStatus(ItemResolutionStatus.valueOf(resolutionStatus.toUpperCase()));
    if (resolutionType != null)
    {
      reviewItem.setResolutionType(review.getDataReferential().getItemResolutionType(resolutionType));
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