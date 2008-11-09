package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryAdapter;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class GutterRenderer extends EditorFactoryAdapter implements IReviewItemListener
{
  private final Project project;
  private Editor editor;

  public GutterRenderer(Project project)
  {
    this.project = project;
  }

  @Override
  public void editorCreated(EditorFactoryEvent event)
  {
    editor = event.getEditor();

    ReviewManager reviewManager = ServiceManager.getService(project, ReviewManager.class);
    Review activeReview = reviewManager.getActiveReview();
    if (activeReview == null)
    {
      return;
    }

    activeReview.addReviewItemListener(this);

    VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
    List<ReviewItem> items = activeReview.getItems(file);
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
        editor.getDocument().getLineStartOffset(item.getLineStart() - 1),
        editor.getDocument().getLineEndOffset(item.getLineEnd() - 1) + 1,
        HighlighterLayer.FIRST - 1,
        null,
        HighlighterTargetArea.LINES_IN_RANGE);
    rangeHighlighter.setLineMarkerRenderer(new CustomGutterRenderer(item, rangeHighlighter));
    rangeHighlighter.setGutterIconRenderer(new CustomGutterIconRenderer(item, rangeHighlighter));
  }

  @Override
  public void editorReleased(EditorFactoryEvent event)
  {
  }

  public void itemAdded(ReviewItem item)
  {
    renderGutter(item);
  }

  public void itemDeleted(ReviewItem item)
  {
  }

  private static class CustomGutterRenderer implements ActiveGutterRenderer
  {
    private final ReviewItem item;
    private final RangeHighlighter rangeHighlighter;

    public CustomGutterRenderer(ReviewItem item, RangeHighlighter rangeHighlighter)
    {
      this.item = item;
      this.rangeHighlighter = rangeHighlighter;
    }

    public void doAction(Editor editor, MouseEvent e)
    {
    }

    public boolean canDoAction(MouseEvent e)
    {
      return true;
    }

    public void paint(Editor editor, Graphics g, Rectangle r)
    {
    }
  }

  private static class CustomGutterIconRenderer extends GutterIconRenderer
  {
    private final ReviewItem item;
    private final RangeHighlighter rangeHighlighter;

    public CustomGutterIconRenderer(ReviewItem item, RangeHighlighter rangeHighlighter)
    {
      this.item = item;
      this.rangeHighlighter = rangeHighlighter;
    }

    @NotNull
    @Override
    public Icon getIcon()
    {
      return RevuIconProvider.getIcon(RevuIconProvider.IconRef.GUTTER_COMMENT);
    }

    @Override
    public String getTooltipText()
    {
      return "<html>" + item.getDesc() + "</br><i>"
        + item.getHistory().getCreatedBy().getDisplayName()
        + " - "
        + DateFormat.getDateTimeInstance().format(new Date(item.getHistory().getCreatedOn()));
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
          editor.getSelectionModel().setSelection(
            rangeHighlighter.getStartOffset(),
            rangeHighlighter.getEndOffset());
        }
      };
    }
  }
}
