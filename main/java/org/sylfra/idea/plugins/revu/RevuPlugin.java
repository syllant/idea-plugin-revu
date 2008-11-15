package org.sylfra.idea.plugins.revu;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.ui.GutterManager;

/**
 * The main application component available as a singleton and providing convenient methods
 * to access services declared in plugin.xml
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuPlugin implements ProjectComponent
{
  public static final String PLUGIN_NAME = "reVu";
  public static final String PLUGIN_ID = "org.sylfra.idea.plugins.revu";

  private static final Logger LOGGER = Logger.getInstance(RevuPlugin.class.getName());

  private Project project;

  public RevuPlugin(Project project)
  {
    this.project = project;
  }

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
    EditorFactory.getInstance().addEditorFactoryListener(new GutterManager(project));
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
