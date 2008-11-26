package org.sylfra.idea.plugins.revu.config.impl;

import com.intellij.openapi.project.Project;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.model.*;

import java.io.File;
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

    if (referential.getLinkedFile() != null)
    {
      writer.addAttribute("link", RevuUtils.buildRelativePath(getProject(context), referential.getLinkedFile()));
    }

    // Priorities
    writer.startNode("priorities");
    SortedSet<ItemPriority> priorities = new TreeSet<ItemPriority>(
      referential.getItemPrioritiesByName().values());
    for (ItemPriority priority : priorities)
    {
      writer.startNode("priority");
      context.convertAnother(priority);
      writer.endNode();
    }
    writer.endNode();

    // Categories
    writer.startNode("categories");
    SortedSet<ItemCategory> categories = new TreeSet<ItemCategory>(
      referential.getItemCategoriesByName().values());
    for (ItemCategory category : categories)
    {
      writer.startNode("category");
      context.convertAnother(category);
      writer.endNode();
    }
    writer.endNode();

    // Resolution types
    writer.startNode("resolutionTypes");
    SortedSet<ItemResolutionType> resolutionTypes = new TreeSet<ItemResolutionType>(
      referential.getItemResolutionTypesByName().values());
    for (ItemResolutionType resolutionType : resolutionTypes)
    {
      writer.startNode("resolutionType");
      context.convertAnother(resolutionType);
      writer.endNode();
    }
    writer.endNode();

    // Users
    writer.startNode("users");
    SortedSet<User> users = new TreeSet<User>(referential.getUsersByLogin().values());
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
    DataReferential referential = new DataReferential();

    String link = reader.getAttribute("link");
    if (link != null)
    {
      Project project = getProject(context);
      File file = RevuUtils.findFileFromRelativeFile(project, link);
      referential.setLinkedFile(file);
    }

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
      else if ("categories".equals(reader.getNodeName()))
      {
        Set<ItemCategory> categories = new HashSet<ItemCategory>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          categories.add((ItemCategory) context.convertAnother(categories, ItemCategory.class));
          reader.moveUp();
        }
        referential.setItemCategories(categories);
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