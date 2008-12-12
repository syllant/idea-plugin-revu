package org.sylfra.idea.plugins.revu.config.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.*;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class DataReferentialConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return DataReferential.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    DataReferential referential = (DataReferential) source;

    // Priorities
    writer.startNode("priorities");
    SortedSet<ItemPriority> priorities = new TreeSet<ItemPriority>(
      referential.getItemPrioritiesByName(false).values());
    for (ItemPriority priority : priorities)
    {
      writer.startNode("priority");
      context.convertAnother(priority);
      writer.endNode();
    }
    writer.endNode();

    // Tags
    writer.startNode("tags");
    SortedSet<ItemTag> tags = new TreeSet<ItemTag>(
      referential.getItemTagsByName(false).values());
    for (ItemTag tag : tags)
    {
      writer.startNode("tag");
      context.convertAnother(tag);
      writer.endNode();
    }
    writer.endNode();

    // Resolution types
    writer.startNode("resolutionTypes");
    SortedSet<ItemResolutionType> resolutionTypes = new TreeSet<ItemResolutionType>(
      referential.getItemResolutionTypesByName(false).values());
    for (ItemResolutionType resolutionType : resolutionTypes)
    {
      writer.startNode("resolutionType");
      context.convertAnother(resolutionType);
      writer.endNode();
    }
    writer.endNode();

    // Users
    writer.startNode("users");
    SortedSet<User> users = new TreeSet<User>(referential.getUsersByLogin(false).values());
    for (User user : users)
    {
      writer.startNode("user");
      context.convertAnother(user);
      writer.endNode();
    }
    writer.endNode();
  }

  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
  {
    DataReferential referential = new DataReferential(getReview(context));

    while (reader.hasMoreChildren())
    {
      reader.moveDown();
      if ("priorities".equals(reader.getNodeName()))
      {
        Set<ItemPriority> priorities = new HashSet<ItemPriority>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          priorities.add((ItemPriority) context.convertAnother(priorities, ItemPriority.class));
          reader.moveUp();
        }
        referential.setItemPriorities(priorities);
      }
      else if ("tags".equals(reader.getNodeName()))
      {
        Set<ItemTag> tags = new HashSet<ItemTag>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          tags.add((ItemTag) context.convertAnother(tags, ItemTag.class));
          reader.moveUp();
        }
        referential.setItemTags(tags);
      }
      else if ("resolutionTypes".equals(reader.getNodeName()))
      {
        Set<ItemResolutionType> itemResolutionTypes = new HashSet<ItemResolutionType>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          itemResolutionTypes.add((ItemResolutionType) context.convertAnother(itemResolutionTypes, ItemResolutionType.class));
          reader.moveUp();
        }
        referential.setItemResolutionTypes(itemResolutionTypes);
      }
      else if ("users".equals(reader.getNodeName()))
      {
        Set<User> users = new HashSet<User>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          users.add((User) context.convertAnother(users, User.class));
          reader.moveUp();
        }
        referential.setUsers(users);
      }
      reader.moveUp();
    }

    return referential;
  }
}