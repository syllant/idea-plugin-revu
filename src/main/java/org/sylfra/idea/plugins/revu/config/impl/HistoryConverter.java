package org.sylfra.idea.plugins.revu.config.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.History;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class HistoryConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return History.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    History history = (History) source;

    writer.addAttribute("createdBy", history.getCreatedBy().getLogin());
    writer.addAttribute("lastUpdatedBy", history.getLastUpdatedBy().getLogin());
    writer.addAttribute("createdOn", String.valueOf(history.getCreatedOn()));
    writer.addAttribute("lastUpdatedOn", String.valueOf(history.getLastUpdatedOn()));
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    String createdBy = reader.getAttribute("createdBy");
    String createdOn = reader.getAttribute("createdOn");
    String lastUpdatedBy = reader.getAttribute("lastUpdatedBy");
    String lastUpdatedOn = reader.getAttribute("lastUpdatedOn");

    History history = new History();

    if (createdBy != null)
    {
      history.setCreatedBy(retrieveUser(context, createdBy));
    }
    if (lastUpdatedBy != null)
    {
      history.setLastUpdatedBy(retrieveUser(context, lastUpdatedBy));
    }
    if (createdOn != null)
    {
      history.setCreatedOn(Long.parseLong(createdOn));
    }
    if (lastUpdatedOn != null)
    {
      history.setLastUpdatedOn(Long.parseLong(lastUpdatedOn));
    }

    return history;
  }
}
