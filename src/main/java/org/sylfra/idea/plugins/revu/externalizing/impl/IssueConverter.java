package org.sylfra.idea.plugins.revu.externalizing.impl;

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
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class IssueConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return Issue.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    Issue issue = (Issue) source;

    Project project = getProject(context);

    if (issue.getFile() != null)
    {
      String filePath = RevuVfsUtils.buildRelativePath(project, issue.getFile());
      writer.addAttribute("filePath", filePath);
    }

    writer.addAttribute("summary", issue.getSummary());
    if (issue.getVcsRev() != null)
    {
      writer.addAttribute("vcsRev", issue.getVcsRev());
    }
    if (issue.getLocalRev() != null)
    {
      writer.addAttribute("localRev", issue.getLocalRev());
    }
    writer.addAttribute("lineStart", String.valueOf(issue.getLineStart()));
    writer.addAttribute("lineEnd", String.valueOf(issue.getLineEnd()));
    writer.addAttribute("hash", String.valueOf(issue.getHash()));

    List<IssueTag> tagList = issue.getTags();
    if ((tagList != null) && (!tagList.isEmpty()))
    {
      SortedSet<IssueTag> tags = new TreeSet<IssueTag>(tagList);
      writer.addAttribute("tags", ConverterUtils.toString(tags, false));
    }

    if (issue.getPriority() != null)
    {
      writer.addAttribute("priority", issue.getPriority().getName());
    }
    writer.addAttribute("status", issue.getStatus().toString().toLowerCase());

    // Recipients
    List<User> recipients = issue.getRecipients();
    if ((recipients != null) && (!recipients.isEmpty()))
    {
      SortedSet<User> users = new TreeSet<User>(recipients);
      writer.addAttribute("recipients", ConverterUtils.toString(users, false));
    }

    // History
    writer.startNode("history");
    context.convertAnother(issue.getHistory());
    writer.endNode();

    // Desc
    if ((issue.getDesc() != null) && (issue.getDesc().length() > 0))
    {
      writer.startNode("desc");
      writer.setValue(issue.getDesc());
      writer.endNode();
    }

    // Notes
    List<IssueNote> notes = issue.getNotes();
    if ((notes != null) && (!notes.isEmpty()))
    {
      writer.startNode("notes");
      for (IssueNote note : notes)
      {
        writer.startNode("note");
        context.convertAnother(note);
        writer.endNode();
      }
      writer.endNode();
    }
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
    String status = reader.getAttribute("status");
    String recipients = reader.getAttribute("recipients");

    Review review = getReview(context);

    Issue issue = new Issue();
    issue.setReview(review);

    Project project = getProject(context);

    if (filePath != null)
    {
      VirtualFile file = RevuVfsUtils.findVFileFromRelativeFile(project, filePath);
      issue.setFile(file);
    }
    
    issue.setVcsRev(vcsRev);
    issue.setLocalRev(localRev);
    issue.setLineStart(Integer.parseInt(lineStart));
    issue.setLineEnd(Integer.parseInt(lineEnd));
    if (hash != null)
    {
      issue.setHash(Integer.parseInt(hash));
    }
    if (tags != null)
    {
      String[] tagNames = tags.split(",");
      List<IssueTag> tagSet = new ArrayList<IssueTag>();
      for (String tagName : tagNames)
      {
        IssueTag issueTag = review.getDataReferential().getIssueTag(tagName);
        if (issueTag == null)
        {
          // @TODO report error to user
          logger.warn("Can't find tag in referential. Tag: '" + tagName + "', review: " + review.getPath());
        }
        else
        {
          tagSet.add(issueTag);
        }
      }
      issue.setTags(tagSet);
    }
    if (recipients != null)
    {
      String[] userLogins = recipients.split(",");
      List<User> userSet = new ArrayList<User>();
      for (String login : userLogins)
      {
        User user = review.getDataReferential().getUser(login, true);
        if (user == null)
        {
          // @TODO report error to user
          logger.warn("Can't find user in referential. Login:'" + login + "', review: " + review.getPath());
        }
        else
        {
          userSet.add(user);
        }
      }
      issue.setRecipients(userSet);
    }
    if (priority != null)
    {
      issue.setPriority(review.getDataReferential().getIssuePriority(priority));
    }
    issue.setStatus(IssueStatus.valueOf(status.toUpperCase()));
    issue.setSummary(summary);

    while (reader.hasMoreChildren())
    {
      reader.moveDown();
      if ("history".equals(reader.getNodeName()))
      {
        issue.setHistory((History) context.convertAnother(issue, History.class));
      }
      else if ("desc".equals(reader.getNodeName()))
      {
        issue.setDesc(reader.getValue());
      }
      else if ("notes".equals(reader.getNodeName()))
      {
        List<IssueNote> notes = new ArrayList<IssueNote>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          notes.add((IssueNote) context.convertAnother(notes, IssueNote.class));
          reader.moveUp();
        }
        issue.setNotes(notes);
      }
      reader.moveUp();
    }

    return issue;
  }
}