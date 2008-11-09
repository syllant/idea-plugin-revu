package org.sylfra.idea.plugins.revu.ui.forms.reviewitem;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.impl.UsagePreviewPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemPreviewForm extends AbstractReviewItemForm
{
  private JPanel contentPane;
  private UsagePreviewPanel usagePreviewPanel;

  public ReviewItemPreviewForm(@NotNull Project project)
  {
    super(project);
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  public void internalUpdateUI()
  {
    usagePreviewPanel.updateLayout(buildUsageInfos());
  }

  protected void internalUpdateData(@Nullable ReviewItem reviewItemToUpdate)
  {
  }

  private
  @Nullable
  List<UsageInfo> buildUsageInfos()
  {
    if (reviewItem == null)
    {
      return null;
    }

    PsiFile psiFile = RevuUtils.getPsiFile(project, reviewItem);
    if (psiFile == null)
    {
      return null;
    }

    Document document = RevuUtils.getDocument(project, reviewItem);
    if (document == null)
    {
      return null;
    }

    UsageInfo usageInfo = new UsageInfo(psiFile,
      document.getLineStartOffset(reviewItem.getLineStart() - 1),
      document.getLineEndOffset(reviewItem.getLineEnd() - 1) + 1);

    List<UsageInfo> result = new ArrayList<UsageInfo>(1);
    result.add(usageInfo);

    return result;
  }

  private void createUIComponents()
  {
    usagePreviewPanel = new UsagePreviewPanel(project);
  }
}
