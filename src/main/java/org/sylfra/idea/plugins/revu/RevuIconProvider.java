package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Provides an unified way to retrieve an icon defined in plugin resources
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class RevuIconProvider
{
  @NonNls
  private static final String PACKAGE_ROOT = "/org/sylfra/idea/plugins/revu/resources/icons";

  /**
   * Enumeration for all defined images. Forces callers to use one of these value when retrieving
   * icons
   */
  public static enum IconRef
  {
    DESYNCHRONIZED("desynchronized"),
    GUTTER_ISSUE("gutterIssue"),
    GUTTER_ISSUE_DESYNCHRONIZED("gutterIssueDesynchronized"),
    GUTTER_ISSUES("gutterIssues"),
    GUTTER_ISSUES_DESYNCHRONIZED("gutterIssuesDesynchronized"),
    EDIT_CONFIG("editConfig"),
    EDIT_MULTI_CHOOSER("editMultiChooser"),
    FIELD_ERROR("fieldError"),
    REVIEW_LOCAL("reviewLocal"),
    REVIEW_SHARED("reviewShared"),
    REVU("revu"),
    REVU_LARGE("revuLarge"),
    STATUS_BAR_DEFAULT("statusBarDefault"),
    STATUS_BAR_INFO("statusBarInfo"),
    STATUS_BAR_WARNING("statusBarWarning"),
    STATUS_BAR_ERROR("statusBarError"),
    TAG("tag");
    
    private final String imgName;

    IconRef(String imgName)
    {
      this.imgName = imgName;
    }
  }

  /**
   * Retrieve an icon from its reference
   *
   * @param iconRef the icon reference
   *
   * @return the loaded icon
   */
  public static Icon getIcon(@NotNull IconRef iconRef)
  {
    return IconLoader.getIcon(PACKAGE_ROOT + "/" + iconRef.imgName + ".png");
  }
}