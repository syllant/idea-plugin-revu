package org.sylfra.idea.plugins.revu.model;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueNote extends AbstractRevuEntity<IssueNote> implements Comparable<IssueNote>,
   IRevuHistoryHolderEntity<IssueNote>
{
  private History history;
  private String content;

  public IssueNote()
  {
  }

  public IssueNote(String content)
  {
    this.content = content;
  }

  public String getContent()
  {
    return content;
  }

  public void setContent(String content)
  {
    this.content = content;
  }

  public History getHistory()
  {
    return history;
  }

  public void setHistory(History history)
  {
    this.history = history;
  }

  public int compareTo(IssueNote o)
  {
    return history.compareTo(o.getHistory());
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

    IssueNote issueNote = (IssueNote) o;

    if (content != null ? !content.equals(issueNote.content) : issueNote.content != null)
    {
      return false;
    }
    if (history != null ? !history.equals(issueNote.history) : issueNote.history != null)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = history != null ? history.hashCode() : 0;
    result = 31 * result + (content != null ? content.hashCode() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return new ToStringBuilder(this).
      append("history", history).
      append("content", content).
      toString();
  }
}