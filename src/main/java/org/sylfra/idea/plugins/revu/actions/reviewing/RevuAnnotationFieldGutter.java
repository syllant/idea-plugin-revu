/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sylfra.idea.plugins.revu.actions.reviewing;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.actions.ActiveAnnotationGutter;
import com.intellij.openapi.vcs.annotate.AnnotationListener;
import com.intellij.openapi.vcs.annotate.AnnotationSource;
import com.intellij.openapi.vcs.annotate.FileAnnotation;
import com.intellij.openapi.vcs.annotate.LineAnnotationAspect;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xml.util.XmlStringUtil;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.FileScopeManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.awt.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Irina Chernushina
 * @author Konstantin Bulenkov
 */
class RevuAnnotationFieldGutter implements ActiveAnnotationGutter
{
  private static final Color BG_COLOR_HIGHLIGHTED = new Color(0xeeffee);

  private final FileAnnotation myAnnotation;
  private final FileScopeManager fileScopeManager;
  private final Editor myEditor;
  private final AnnotationListener myListener;

  RevuAnnotationFieldGutter(FileAnnotation annotation, Editor editor)
  {
    myAnnotation = annotation;
    myEditor = editor;
    fileScopeManager = ApplicationManager.getApplication().getComponent(FileScopeManager.class);

    myListener = new AnnotationListener()
    {
      public void onAnnotationChanged()
      {
        myEditor.getGutter().closeAllAnnotations();
      }
    };

    myAnnotation.addListener(myListener);
  }

  public String getLineText(int line, Editor editor)
  {
    if (!isLineHightlighted(line, editor))
    {
      return null;
    }

    StringBuilder buffer = new StringBuilder();
    LineAnnotationAspect[] aspects = myAnnotation.getAspects();
    for (LineAnnotationAspect aspect : aspects)
    {
      buffer.append(aspect.getValue(line)).append(' ');
    }

    return buffer.toString();
  }

  @Nullable
  public String getToolTip(final int line, final Editor editor)
  {
    return isLineHightlighted(line, editor) ? XmlStringUtil.escapeString(myAnnotation.getToolTip(line)) : null;
  }

  public void doAction(int line)
  {
  }

  public Cursor getCursor(final int line)
  {
    return Cursor.getDefaultCursor();
  }

  public EditorFontType getStyle(final int line, final Editor editor)
  {
    return EditorFontType.PLAIN;
  }

  @Nullable
  public ColorKey getColor(final int line, final Editor editor)
  {
    return AnnotationSource.LOCAL.getColor();
  }

  // For Idea 9 compatibility
  public List<AnAction> getPopupActions(Editor editor)
  {
    return Collections.emptyList();
  }

  public List<AnAction> getPopupActions(int line, Editor editor)
  {
    return Collections.emptyList();
  }

  public void gutterClosed()
  {
    myAnnotation.removeListener(myListener);
    myAnnotation.dispose();
    final Collection<ActiveAnnotationGutter> gutters = myEditor.getUserData(RevuAnnotateToggleAction.KEY_IN_EDITOR);
    if (gutters != null)
    {
      gutters.remove(this);
    }
  }

  @Nullable
  public Color getBgColor(int line, Editor editor)
  {
    if (!isLineHightlighted(line, editor))
    {
      return null;
    }

    return BG_COLOR_HIGHLIGHTED;
  }

  private boolean isLineHightlighted(final int line, final Editor editor)
  {
    final VcsRevisionNumber number = myAnnotation.getLineRevisionNumber(line);
    if (number != null)
    {
      Project project = editor.getProject();
      Review review = RevuUtils.getReviewingReview(project);
      if (review != null)
      {
        VirtualFile vFile = FileDocumentManager.getInstance().getFile(editor.getDocument());
        if (!fileScopeManager.matchFrom(project, review.getFileScope(), vFile, number))
        {
          return false;
        }
      }
    }

    return true;
  }
}