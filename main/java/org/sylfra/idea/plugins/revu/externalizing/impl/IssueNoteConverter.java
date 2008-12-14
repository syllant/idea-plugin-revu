package org.sylfra.idea.plugins.revu.externalizing.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.model.IssueNote;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class IssueNoteConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return IssueNote.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    IssueNote note = (IssueNote) source;

    writer.addAttribute("createdBy", RevuUtils.getNonNullUser(note.getHistory().getCreatedBy()).getLogin());
    writer.addAttribute("createdOn", formatDate(note.getHistory().getCreatedOn()));
    writer.setValue(note.getContent());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    String createdBy = reader.getAttribute("createdBy");
    String createdOn = reader.getAttribute("createdOn");

    IssueNote note = new IssueNote();
    note.setContent(reader.getValue());
    History history = new History();
    note.setHistory(history);

    if (createdBy != null)
    {
      history.setCreatedBy(retrieveUser(context, createdBy));
    }
    history.setCreatedOn(parseDate(createdOn));

    return note;
  }
}
