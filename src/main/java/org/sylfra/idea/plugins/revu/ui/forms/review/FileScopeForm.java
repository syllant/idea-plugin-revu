package org.sylfra.idea.plugins.revu.ui.forms.review;

import com.intellij.ide.util.scopeChooser.ScopeEditorPanel;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.psi.search.scope.packageSet.PackageSetFactory;
import com.intellij.psi.search.scope.packageSet.ParsingException;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.FileScope;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVcsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FileScopeForm extends AbstractUpdatableForm<FileScope>
{
  private static final Logger LOGGER = Logger.getInstance(FileScopeForm.class.getName());
  private final static DateFormat DATE_FORMAT = SimpleDateFormat
    .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, Locale.getDefault());

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
  private final Project project;
  private final ScopeEditorPanel scopeEditorPanel;

  public FileScopeForm(@NotNull final Project project)
  {
    this.project = project;
    scopeEditorPanel = new ScopeEditorPanel(project);

    $$$setupUI$$$();

    configureUI(project);
  }

  private void createUIComponents()
  {
    pnScopeEditor = scopeEditorPanel.getPanel();
  }

  private void configureUI(@NotNull final Project project)
  {
    ckVcsBeforeRev.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        boolean mayBrowseChangeLists = RevuVcsUtils.mayBrowseChangeLists(project);
        tfVcsBeforeRev.setEnabled(ckVcsBeforeRev.isEnabled() && ckVcsBeforeRev.isSelected());
        bnVcsBeforeRev.setEnabled(tfVcsBeforeRev.isEnabled() && mayBrowseChangeLists);
      }
    });
    ckVcsAfterRev.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        boolean mayBrowseChangeLists = RevuVcsUtils.mayBrowseChangeLists(project);
        tfVcsAfterRev.setEnabled(ckVcsAfterRev.isEnabled() && ckVcsAfterRev.isSelected());
        bnVcsAfterRev.setEnabled(tfVcsAfterRev.isEnabled() && mayBrowseChangeLists);
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

      if (data.getVcsBeforeRev() != null)
      {
        if (!ckVcsBeforeRev.isSelected() || !tfVcsBeforeRev.getText().equals(data.getVcsBeforeRev()))
        {
          return true;
        }
      }

    return (data.getPathPattern() == null) 
      ? ((scopeEditorPanel.getCurrentScope() != null) && (scopeEditorPanel.getCurrentScope().getText().length() > 0))
      : ((scopeEditorPanel.getCurrentScope() == null) || (!data.getPathPattern().equals(scopeEditorPanel.getCurrentScope().getText())));
  }

  @Override
  protected void internalUpdateWriteAccess(FileScope data, @Nullable User user)
  {
    boolean isHabilited = isHabilitedToEditReview(data, user);

    RevuUtils.setWriteAccess(isHabilited, ckVcsBeforeRev, ckVcsAfterRev, tfVcsBeforeRev, tfVcsAfterRev, scopeEditorPanel.getPanel());
    RevuUtils.setWriteAccess(isHabilited && isProjectUnderVcs(), pnVcsRev);
  }

  @Override
  protected void internalValidateInput(FileScope data)
  {
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
    lbWarningNoVcs.setVisible(isProjectUnderVcs());

    tfVcsBeforeRev.setText((data == null) || (data.getVcsBeforeRev() == null) ? "" : data.getVcsBeforeRev());
    tfVcsAfterRev.setText((data == null) || (data.getVcsAfterRev() == null) ? "" : data.getVcsAfterRev());
    
    if (tfVcsBeforeRev.getText().length() > 0)
    {
      ckVcsBeforeRev.setSelected(true);
    }

    if (tfVcsAfterRev.getText().length() > 0)
    {
      ckVcsAfterRev.setSelected(true);
    }

    PackageSet packageSet;
    if ((data == null) || (data.getPathPattern() == null) || (data.getPathPattern().length() == 0))
    {
      packageSet = null;
    }
    else
    {
      try
      {
        packageSet = PackageSetFactory.getInstance().compile(data.getPathPattern());
      }
      catch (ParsingException e)
      {
        LOGGER.warn("Failed to compile file scope path pattern: <" + data.getPathPattern() + ">");
        packageSet = null;
      }
    }

    scopeEditorPanel.reset(packageSet, null);
  }

  @Override
  protected void internalUpdateData(@NotNull FileScope data)
  {
    data.setVcsBeforeRev(ckVcsBeforeRev.isEnabled() && ckVcsBeforeRev.isSelected() ? tfVcsBeforeRev.getText() : null);
    data.setVcsAfterRev(ckVcsAfterRev.isEnabled() && ckVcsAfterRev.isSelected() ? tfVcsAfterRev.getText() : null);
    data.setPathPattern(scopeEditorPanel.getCurrentScope() == null 
      ? null : scopeEditorPanel.getCurrentScope().getText());
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
    VirtualFile baseDir = project.getBaseDir();
    assert (baseDir != null);

    FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(baseDir);
    assert (filePath != null);

    AbstractVcs[] vcss = ProjectLevelVcsManager.getInstance(project).getAllActiveVcss();
    assert (vcss.length > 0);

    // @TODO handle case where projet has several VCS roots
    // Here, I use the first VCS connection
    AbstractVcs vcs = vcss[0];
    assert ((vcs != null) && (vcs.getCommittedChangesProvider() != null));

    RepositoryLocation location = vcs.getCommittedChangesProvider().getLocationFor(filePath);

    return AbstractVcsHelper.getInstance(project)
      .chooseCommittedChangeList(vcs.getCommittedChangesProvider(), location);
  }

  private boolean isProjectUnderVcs()
  {
    return ProjectLevelVcsManager.getInstance(project).getAllActiveVcss().length == 0;
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$()
  {
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1,
      new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panel1.setBorder(BorderFactory.createTitledBorder(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.scope.from.title")));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), 1, -1));
    panel1.add(panel2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      null, null, null, 0, false));
    ckVcsAfterRev = new JCheckBox();
    ckVcsAfterRev.setActionCommand("");
    this.$$$loadButtonText$$$(ckVcsAfterRev,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.scope.from.rev.title"));
    panel2.add(ckVcsAfterRev, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    tfVcsAfterRev = new JTextField();
    panel2.add(tfVcsAfterRev, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
      false));
    bnVcsAfterRev = new JButton();
    bnVcsAfterRev.setIcon(new ImageIcon(getClass().getResource("/objectBrowser/browser.png")));
    bnVcsAfterRev.setMargin(new Insets(0, 0, 0, 0));
    bnVcsAfterRev.setText("");
    panel2.add(bnVcsAfterRev,
      new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 1, -1));
    panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel2.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(20, -1), null, 0, false));
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), 1, -1));
    panel1.add(panel4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      null, null, null, 0, false));
    ckVcsBeforeRev = new JCheckBox();
    ckVcsBeforeRev.setActionCommand("");
    this.$$$loadButtonText$$$(ckVcsBeforeRev,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.scope.from.date.title"));
    panel4.add(ckVcsBeforeRev, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    tfVcsBeforeRev = new JTextField();
    panel4.add(tfVcsBeforeRev, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
      false));
    bnVcsBeforeRev = new JButton();
    bnVcsBeforeRev.setIcon(new ImageIcon(getClass().getResource("/objectBrowser/browser.png")));
    bnVcsBeforeRev.setMargin(new Insets(0, 0, 0, 0));
    bnVcsBeforeRev.setText("");
    panel4.add(bnVcsBeforeRev,
      new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    panel1.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(20, -1), null, 0, false));
    final Spacer spacer3 = new Spacer();
    panel1.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    ButtonGroup buttonGroup;
    buttonGroup = new ButtonGroup();
    buttonGroup.add(ckVcsAfterRev);
    buttonGroup.add(ckVcsBeforeRev);
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadButtonText$$$(AbstractButton component, String text)
  {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == '&')
      {
        i++;
        if (i == text.length())
        {
          break;
        }
        if (!haveMnemonic && text.charAt(i) != '&')
        {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic)
    {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }
}
