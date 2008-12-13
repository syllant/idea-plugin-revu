package org.sylfra.idea.plugins.revu.externalizing.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.History;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class HistoryConverter extends AbstractConverter
{
  private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

  public boolean canConvert(Class type)
  {
    return History.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    History history = (History) source;

    writer.addAttribute("createdBy", RevuUtils.getNonNullUser(history.getCreatedBy()).getLogin());
    writer.addAttribute("lastUpdatedBy", RevuUtils.getNonNullUser(history.getLastUpdatedBy()).getLogin());
    synchronized (DATE_FORMATTER)
    {
      writer.addAttribute("createdOn", DATE_FORMATTER.format((history.getCreatedOn() == null)
        ? new Date() : history.getCreatedOn()));
      writer.addAttribute("lastUpdatedOn", DATE_FORMATTER.format((history.getLastUpdatedOn() == null)
        ? new Date() : history.getLastUpdatedOn()));
    }
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
    synchronized (DATE_FORMATTER)
    {
      if (createdOn != null)
      {
        try
        {
          history.setCreatedOn(DATE_FORMATTER.parse(createdOn));
        }
        catch (ParseException e)
        {
          history.setCreatedOn(new Date());
          logger.warn("Found bad date: " + createdOn + ". Will use current date.");
        }
      }
      if (lastUpdatedOn != null)
      {
        try
        {
          history.setLastUpdatedOn(DATE_FORMATTER.parse(lastUpdatedOn));
        }
        catch (ParseException e)
        {
          history.setLastUpdatedOn(new Date());
          logger.warn("Found bad date: " + lastUpdatedOn + ". Will use current date.");
        }
      }
    }

    return history;
  }
}
