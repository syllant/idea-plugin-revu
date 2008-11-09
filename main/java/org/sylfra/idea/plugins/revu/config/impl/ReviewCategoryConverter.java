package org.sylfra.idea.plugins.revu.config.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.ReviewCategory;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class ReviewCategoryConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return ReviewCategory.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    ReviewCategory category = (ReviewCategory) source;

    writer.addAttribute("name", category.getName());
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    String name = reader.getAttribute("name");

    return new ReviewCategory(name);
  }
}