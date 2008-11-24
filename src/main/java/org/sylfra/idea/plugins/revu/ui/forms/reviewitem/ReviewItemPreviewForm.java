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
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
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

  public JComponent getPreferredFocusedComponent()
  {
    return usagePreviewPanel;
  }

  public void internalValidateInput()
  {
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  public void internalUpdateUI(ReviewItem data)
  {
    usagePreviewPanel.updateLayout(buildUsageInfos());
  }

  protected void internalUpdateData(@Nullable ReviewItem reviewItemToUpdate)
  {
  }

  @Nullable
  private List<UsageInfo> buildUsageInfos()
  {
    ReviewItem reviewItem = getData();

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

  public void dispose()
  {
    usagePreviewPanel.dispose();
  }

  public boolean isModified(@NotNull ReviewItem reviewItem)
  {
    return false;
  }

}
