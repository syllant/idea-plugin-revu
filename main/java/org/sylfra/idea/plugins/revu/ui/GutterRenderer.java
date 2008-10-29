package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryAdapter;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.ComponentProvider;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class GutterRenderer extends EditorFactoryAdapter implements IReviewListener
{
  private final Project project;
  private GutterIconRenderer gutterIconRenderer;
  private Editor editor;

  public GutterRenderer(Project project)
  {
    this.project = project;
    gutterIconRenderer = new CustomGutterIconRenderer();
  }

  @Override
  public void editorCreated(EditorFactoryEvent event)
  {
    editor = event.getEditor();

    ReviewManager reviewManager = ComponentProvider.getReviewManager(project);
    Review activeReview = reviewManager.getActiveReview();
    if (activeReview == null)
    {
      return;
    }

    activeReview.addReviewListener(this);

    VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
    String filePath = file.getPath();
    List<ReviewItem> items = activeReview.getItems(filePath);
    if (items != null)
    {
      for (ReviewItem item : items)
      {
        renderGutter(item);
      }
    }
  }

  private void renderGutter(ReviewItem item)
  {
    RangeHighlighter rangeHighlighter =
      editor.getMarkupModel().addRangeHighlighter(
        editor.getDocument().getLineStartOffset(item.getLineStart()),
        editor.getDocument().getLineEndOffset(item.getLineEnd()),
        HighlighterLayer.FIRST - 1,
        null,
        HighlighterTargetArea.LINES_IN_RANGE);
    rangeHighlighter.setGutterIconRenderer(gutterIconRenderer);
  }

  @Override
  public void editorReleased(EditorFactoryEvent event)
  {
  }

  public void itemAdded(ReviewItem item)
  {
    renderGutter(item);
  }

  private static class CustomGutterIconRenderer extends GutterIconRenderer
  {

    @NotNull
    @Override
    public Icon getIcon()
    {
      return RevuIconProvider.getIcon(RevuIconProvider.IconRef.GUTTER_COMMENT);
    }

    @Override
    public String getTooltipText()
    {
      return "tooltip\ntest";
    }

    @Override
    public AnAction getClickAction()
    {
      return new AnAction()
      {
        @Override
        public void actionPerformed(AnActionEvent e)
        {
          Editor editor = e.getData(DataKeys.EDITOR);
//                editor.getDocument().setSelection(start, end);
        }
      };
    }
  }
}
