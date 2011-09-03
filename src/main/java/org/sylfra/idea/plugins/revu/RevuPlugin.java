package org.sylfra.idea.plugins.revu;

import com.intellij.javaee.ExternalResourceManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * The main application component available as a singleton and providing convenient methods
 * to access services declared in plugin.xml
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuPlugin implements ProjectComponent
{
  public static final String PLUGIN_NAME = "reVu";

  /**
   * {@inheritDoc}
   */
  @NonNls
  @NotNull
  public String getComponentName()
  {
    return PLUGIN_NAME;
  }

  /**
   * {@inheritDoc}
   */
  public void initComponent()
  {
    UIUtil.invokeLaterIfNeeded(new Runnable()
    {
      public void run()
      {
        addSchemaResource("1.0");
      }
    });
  }
  
  private void addSchemaResource(String version)
  {
    File file = new File(
      getClass().getResource("/org/sylfra/idea/plugins/revu/resources/schemas/revu_" + version + ".xsd").getFile());
    ExternalResourceManager.getInstance().addResource(
      "http://plugins.intellij.net/revu/ns/revu_" + version + ".xsd",
      file.getAbsolutePath());
  }

  /**
   * {@inheritDoc}
   */
  public void disposeComponent()
  {
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }
}
