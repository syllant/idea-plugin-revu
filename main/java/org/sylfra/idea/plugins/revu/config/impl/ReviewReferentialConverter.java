package org.sylfra.idea.plugins.revu.config.impl;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.sylfra.idea.plugins.revu.model.ReviewCategory;
import org.sylfra.idea.plugins.revu.model.ReviewPriority;
import org.sylfra.idea.plugins.revu.model.ReviewReferential;
import org.sylfra.idea.plugins.revu.model.User;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class ReviewReferentialConverter extends AbstractConverter
{
  public boolean canConvert(Class type)
  {
    return ReviewReferential.class.equals(type);
  }

  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context)
  {
    ReviewReferential referential = (ReviewReferential) source;

    // Priorities
    writer.startNode("priorities");
    SortedSet<ReviewPriority> priorities = new TreeSet<ReviewPriority>(
      referential.getPrioritiesByName().values());
    for (ReviewPriority priority : priorities)
    {
      writer.startNode("priority");
      context.convertAnother(priority);
      writer.endNode();
    }
    writer.endNode();

    // Categories
    writer.startNode("categories");
    SortedSet<ReviewCategory> categories = new TreeSet<ReviewCategory>(
      referential.getCategoriesByName().values());
    for (ReviewCategory category : categories)
    {
      writer.startNode("category");
      context.convertAnother(category);
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
    ReviewReferential referential = new ReviewReferential();

    while (reader.hasMoreChildren())
    {
      reader.moveDown();
      if ("priorities".equals(reader.getNodeName()))
      {
        Set<ReviewPriority> priorities = new HashSet<ReviewPriority>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          priorities.add((ReviewPriority) context.convertAnother(priorities, ReviewPriority.class));
          reader.moveUp();
        }
        referential.setPriorities(priorities);
      }
      else if ("categories".equals(reader.getNodeName()))
      {
        Set<ReviewCategory> categories = new HashSet<ReviewCategory>();
        while (reader.hasMoreChildren())
        {
          reader.moveDown();
          categories.add((ReviewCategory) context.convertAnother(categories, ReviewCategory.class));
          reader.moveUp();
        }
        referential.setCategories(categories);
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