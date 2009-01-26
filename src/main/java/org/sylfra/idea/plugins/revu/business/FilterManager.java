package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.Filter;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssueTag;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FilterManager implements ApplicationComponent
{
  private static final Pattern PATTERN_WILDCARD = Pattern.compile("\\*");

  private Map<String, Pattern> patternCache;

  public FilterManager()
  {
    patternCache = new HashMap<String, Pattern>();
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".FilterManager";
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
  }

  public void filter(@NotNull Project project, @NotNull List<Issue> issues)
  {
    Filter filter = RevuUtils.getWorkspaceSettings(project).getSelectedFilter();
    if (filter == null)
    {
      return;
    }

    for (Iterator<Issue> it = issues.listIterator(); it.hasNext();)
    {
      Issue issue = it.next();
      if (match(issue, filter))
      {
        it.remove();
      }
    }
  }

  public boolean match(@NotNull Issue issue, @NotNull Filter filter)
  {
    // Summary
    if (!match(issue.getSummary(), filter.getSummary()))
    {
      return false;
    }

    // File reference
    // TODO use file reference instead of path
    if (!match(issue.getFile().getPath(), filter.getFileRef()))
    {
      return false;
    }

    // Priority
    if ((issue.getPriority() != null) && (!match(issue.getPriority().getName(), filter.getPrioritieNames())))
    {
      return false;
    }

    // Status
    if (!match(issue.getStatus(), filter.getStatuses()))
    {
      return false;
    }

    // Recipients
    List<String> recipientLogins;
    if (issue.getRecipients() == null)
    {
      recipientLogins = null;
    }
    else
    {
      recipientLogins = new ArrayList<String>(issue.getRecipients().size());
      for (User user : issue.getRecipients())
      {
        recipientLogins.add(user.getLogin());
      }
    }
    if (!match(recipientLogins, filter.getRecipientLogins()))
    {
      return false;
    }

    // Resolver
    if ((issue.getResolver() != null) && (!match(issue.getResolver().getLogin(), filter.getRecipientLogins())))
    {
      return false;
    }

    // Tags
    List<String> tagNames;
    if (issue.getTags() == null)
    {
      tagNames = null;
    }
    else
    {
      tagNames = new ArrayList<String>(issue.getTags().size());
      for (IssueTag tag : issue.getTags())
      {
        tagNames.add(tag.getName());
      }
    }
    if (!match(tagNames, filter.getTagNames()))
    {
      return false;
    }

    return true;
  }

  private boolean match(@Nullable String issueValue, @Nullable String filterValue)
  {
    if ((filterValue == null) || (issueValue == null))
    {
      return false;
    }

    Pattern pattern = getPatternFromCache(filterValue);

    return pattern.matcher(issueValue).matches();
  }

  private synchronized Pattern getPatternFromCache(String value)
  {
    Pattern pattern = patternCache.get(value);
    if (pattern == null)
    {
      // Replace '*' wildcard to a real regexp wildcard
      value = PATTERN_WILDCARD.matcher(value).replaceAll(".*");

      pattern = Pattern.compile(value);
      patternCache.put(value, pattern);
    }

    return pattern;
  }

  private <T> boolean match(@Nullable T issueValue, @Nullable List<T> filterValues)
  {
    return ((filterValues != null) && (issueValue != null) && (filterValues.contains(issueValue)));
  }

  private <T> boolean match(@Nullable List<T> issueValues, @Nullable List<T> filterValues)
  {
    if ((filterValues == null) || (issueValues == null))
    {
      return false;
    }

    for (T issueValue : issueValues)
    {
      if (filterValues.contains(issueValue))
      {
        return true;
      }
    }

    return false;
  }
}
