package org.sylfra.idea.plugins.revu.config.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.ItemPriority;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class ReviewPriorityConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return ItemPriority.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    ItemPriority priority = (ItemPriority) source;

    writer.addAttribute("order", String.valueOf(priority.getOrder()));
    writer.addAttribute("name", priority.getName());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    String order = reader.getAttribute("order");
    String name = reader.getAttribute("name");

    return new ItemPriority(Byte.valueOf(order), name);
  }
}