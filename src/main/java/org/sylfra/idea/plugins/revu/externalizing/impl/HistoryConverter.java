package org.sylfra.idea.plugins.revu.externalizing.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
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

    writer.addAttribute("createdBy", RevuUtils.getNonNullUser(history.getCreatedBy()).getLogin());
    writer.addAttribute("lastUpdatedBy", RevuUtils.getNonNullUser(history.getLastUpdatedBy()).getLogin());
    writer.addAttribute("createdOn", formatDate(history.getCreatedOn()));
    writer.addAttribute("lastUpdatedOn", formatDate(history.getLastUpdatedOn()));
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
    history.setCreatedOn(parseDate(createdOn));
    history.setLastUpdatedOn(parseDate(lastUpdatedOn));

    return history;
  }
}
