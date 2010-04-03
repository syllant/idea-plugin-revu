package org.sylfra.idea.plugins.revu.externalizing.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.FileScope;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id: FileScopeConverter.java 15 2008-12-14 22:49:21Z syllant $
 */
class FileScopeConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return FileScope.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    FileScope fileScope = (FileScope) source;

    if (fileScope.getDate() != null)
    {
      writer.addAttribute("afterDate", formatDate(fileScope.getDate()));
    }
    else if (fileScope.getRev() != null)
    {
      writer.addAttribute("afterRev", fileScope.getRev());
    }
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    FileScope fileScope = new FileScope();

    String value = reader.getAttribute("afterDate");
    if (value != null)
    {
      fileScope.setDate(parseDate(value));
    }
    else
    {
      value = reader.getAttribute("afterRev");
      if (value != null)
      {
        fileScope.setRev(value);
      }
    }

    return fileScope;
  }
}