package org.sylfra.idea.plugins.revu.ui.forms.review;

import com.intellij.ide.util.scopeChooser.ScopeEditorPanel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.psi.search.scope.packageSet.PackageSetFactory;
import com.intellij.psi.search.scope.packageSet.ParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.FileScope;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVcsUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FileScopeForm extends AbstractUpdatableForm<FileScope>
{
  private static final Logger LOGGER = Logger.getInstance(FileScopeForm.class.getName());
  private static final String DEFAULT_FILESCOPE_PATTERN = "file[*]:*//*";

  private JPanel contentPane;
  private JCheckBox ckVcsAfterRev;
  private JTextField tfVcsAfterRev;
  private JButton bnVcsAfterRev;
  private JCheckBox ckVcsBeforeRev;
  private JTextField tfVcsBeforeRev;
  private JButton bnVcsBeforeRev;
  private JComponent pnScopeEditor;
  private JLabel lbWarningNoVcs;
  private JPanel pnVcsRev;
  private JLabel lbWarningVcsSlow;
  private ScopeEditorPanel scopeEditorPanel;
  private final Project project;

  public FileScopeForm(@NotNull final Project project)
  {
    this.project = project;

    configureUI(project);
  }

  private void createUIComponents()
  {
    scopeEditorPanel = new ScopeEditorPanel(project);
    pnScopeEditor = scopeEditorPanel.getPanel();
  }

  private void configureUI(@NotNull final Project project)
  {
    ckVcsBeforeRev.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        boolean mayBrowseChangeLists = RevuVcsUtils.mayBrowseChangeLists(project);
        tfVcsBeforeRev.setEditable(ckVcsBeforeRev.isEnabled() && ckVcsBeforeRev.isSelected());
        bnVcsBeforeRev.setEnabled(tfVcsBeforeRev.isEditable() && mayBrowseChangeLists);
      }
    });
    ckVcsAfterRev.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        boolean mayBrowseChangeLists = RevuVcsUtils.mayBrowseChangeLists(project);
        tfVcsAfterRev.setEditable(ckVcsAfterRev.isEnabled() && ckVcsAfterRev.isSelected());
        bnVcsAfterRev.setEnabled(tfVcsAfterRev.isEditable() && mayBrowseChangeLists);
      }
    });

    bnVcsBeforeRev.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        CommittedChangeList changeList = selectChangeList();
        if (changeList != null)
        {
          tfVcsBeforeRev.setText(String.valueOf(changeList.getNumber()));
        }
      }
    });
    bnVcsAfterRev.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        CommittedChangeList changeList = selectChangeList();
        if (changeList != null)
        {
          tfVcsAfterRev.setText(String.valueOf(changeList.getNumber()));
        }
      }
    });
  }

  @Override
  public boolean isModified(@NotNull FileScope data)
  {
    if (data.getVcsAfterRev() != null)
    {
      if (!ckVcsAfterRev.isSelected() || !tfVcsAfterRev.getText().equals(data.getVcsAfterRev()))
      {
        return true;
      }
    }
    else
    {
      if (ckVcsAfterRev.isSelected() && !tfVcsAfterRev.getText().equals(data.getVcsAfterRev()))
      {
        return true;
      }
    }

    if (data.getVcsBeforeRev() != null)
    {
      if (!ckVcsBeforeRev.isSelected() || !tfVcsBeforeRev.getText().equals(data.getVcsBeforeRev()))
      {
        return true;
      }
    }
    else
    {
      if (ckVcsBeforeRev.isSelected() && !tfVcsBeforeRev.getText().equals(data.getVcsBeforeRev()))
      {
        return true;
      }
    }

    return (data.getPathPattern() == null)
      ? ((scopeEditorPanel.getCurrentScope() != null) && (scopeEditorPanel.getCurrentScope().getText().length() > 0))
      : ((scopeEditorPanel.getCurrentScope() == null) ||
      (!data.getPathPattern().equals(scopeEditorPanel.getCurrentScope().getText())));
  }

  @Override
  protected void internalUpdateWriteAccess(FileScope data, @Nullable User user)
  {
    boolean isHabilited = isHabilitedToEditReview(data, user);

    RevuUtils.setWriteAccess(isHabilited, scopeEditorPanel.getPanel());
    RevuUtils.setWriteAccess(isHabilited && isProjectUnderVcs(), ckVcsBeforeRev, ckVcsAfterRev);
    RevuUtils.setWriteAccess(isHabilited && ckVcsBeforeRev.isEnabled() && ckVcsBeforeRev.isSelected(),
      tfVcsBeforeRev, bnVcsBeforeRev);
    RevuUtils.setWriteAccess(isHabilited && ckVcsAfterRev.isEnabled() && ckVcsAfterRev.isSelected(),
      tfVcsAfterRev, bnVcsAfterRev);
  }

  @Override
  protected void internalValidateInput(@Nullable FileScope data)
  {
    if (data == null)
    {
      updateError(tfVcsAfterRev, false, null);
      updateError(tfVcsBeforeRev, false, null);
      updateError(scopeEditorPanel.getPanel(), false, null);

      return;
    }

    AbstractVcs[] vcss = ProjectLevelVcsManager.getInstance(project).getAllActiveVcss();
    if (vcss.length > 0)
    {
      // @TODO handle case where projet has several VCS roots
      // Here, I use the first VCS connection
      AbstractVcs vcs = vcss[0];
      if (ckVcsAfterRev.isSelected())
      {
        updateError(tfVcsAfterRev, vcs.parseRevisionNumber(tfVcsAfterRev.getText()) == null,
          RevuBundle.message("projectSettings.review.scope.invalidRev.text"));
      }

      if (ckVcsBeforeRev.isSelected())
      {
        updateError(tfVcsBeforeRev, vcs.parseRevisionNumber(tfVcsBeforeRev.getText()) == null,
          RevuBundle.message("projectSettings.review.scope.invalidRev.text"));
      }
    }

    if (scopeEditorPanel.getCurrentScope() != null)
    {
      boolean patternError;
      try
      {
        scopeEditorPanel.apply();
        patternError = false;
      }
      catch (ConfigurationException e)
      {
        patternError = true;
      }
      updateError(scopeEditorPanel.getPanel(), patternError,
        RevuBundle.message("projectSettings.review.scope.invalidPattern.text"));
    }
  }

  @Override
  protected void internalUpdateUI(@Nullable FileScope data, boolean requestFocus)
  {
    lbWarningNoVcs.setVisible(!isProjectUnderVcs());

    tfVcsBeforeRev.setText((data == null) || (data.getVcsBeforeRev() == null) ? "" : data.getVcsBeforeRev());
    tfVcsAfterRev.setText((data == null) || (data.getVcsAfterRev() == null) ? "" : data.getVcsAfterRev());

    ckVcsBeforeRev.setSelected((tfVcsBeforeRev.getText().length() > 0));
    ckVcsAfterRev.setSelected((tfVcsAfterRev.getText().length() > 0));

    PackageSet packageSet;
    String pathPattern = ((data == null) || (data.getPathPattern() == null) || (data.getPathPattern().length() == 0))
      ? DEFAULT_FILESCOPE_PATTERN : data.getPathPattern();

    try
    {
      packageSet = PackageSetFactory.getInstance().compile(pathPattern);
    }
    catch (ParsingException e)
    {
      LOGGER.warn("Failed to compile file scope path pattern: <" + pathPattern + ">");
      packageSet = null;
    }

    scopeEditorPanel.reset(packageSet, null);
    scopeEditorPanel.restoreCanceledProgress();
  }

  @Override
  protected void internalUpdateData(@NotNull FileScope data)
  {
    data.setVcsBeforeRev(ckVcsBeforeRev.isEnabled() && ckVcsBeforeRev.isSelected() ? tfVcsBeforeRev.getText() : null);
    data.setVcsAfterRev(ckVcsAfterRev.isEnabled() && ckVcsAfterRev.isSelected() ? tfVcsAfterRev.getText() : null);

    data.setPathPattern(scopeEditorPanel.getCurrentScope() == null
      ? null : scopeEditorPanel.getCurrentScope().getText());
    scopeEditorPanel.cancelCurrentProgress();
  }

  public JComponent getPreferredFocusedComponent()
  {
    return contentPane;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  @Nullable
  public CommittedChangeList selectChangeList()
  {
    try
    {
      return RevuVcsUtils.chooseCommittedChangeList(project);
    }
    catch (VcsException e)
    {
      LOGGER.warn(e);

      final String details = ((e.getLocalizedMessage() == null) ? e.toString() : e.getLocalizedMessage());
      Messages.showErrorDialog(
        RevuBundle.message("fileScopeForm.vcsError.text", details),
        RevuBundle.message("general.plugin.title"));

      return null;
    }
  }

  private boolean isProjectUnderVcs()
  {
    return ProjectLevelVcsManager.getInstance(project).getAllActiveVcss().length > 0;
  }
}
