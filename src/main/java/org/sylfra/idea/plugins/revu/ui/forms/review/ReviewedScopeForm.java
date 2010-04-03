package org.sylfra.idea.plugins.revu.ui.forms.review;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.VirtualFile;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewedScopeForm extends AbstractUpdatableForm<FileScope>
{
  private static final Logger LOGGER = Logger.getInstance(ReviewedScopeForm.class.getName());
  private final static DateFormat DATE_FORMAT = SimpleDateFormat
    .getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT, Locale.getDefault());

  private JPanel contentPane;
  private JRadioButton rbFromRev;
  private JTextField tfFromRev;
  private JButton bnRevHistory;
  private JRadioButton rbFromDate;
  private JTextField tfFromDate;
  private JButton bnDateHistory;
  private JRadioButton rbFromNone;
  private Project project;
  private ActionListener fromChoiceListener;

  public ReviewedScopeForm(@NotNull final Project project)
  {
    this.project = project;

    $$$setupUI$$$();

    configureUI(project);
  }

  private void configureUI(@NotNull final Project project)
  {
    fromChoiceListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        boolean mayBrowseChangeLists = RevuVcsUtils.mayBrowseChangeLists(project);

        tfFromRev.setEnabled(rbFromRev.isEnabled() && rbFromRev.isSelected());
        tfFromDate.setEnabled(rbFromDate.isEnabled() && rbFromDate.isSelected());
        bnRevHistory.setEnabled(tfFromRev.isEnabled() && mayBrowseChangeLists);
        bnDateHistory.setEnabled(tfFromDate.isEnabled() && mayBrowseChangeLists);
      }
    };
    rbFromNone.addActionListener(fromChoiceListener);
    rbFromDate.addActionListener(fromChoiceListener);
    rbFromRev.addActionListener(fromChoiceListener);

    bnDateHistory.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        CommittedChangeList changeList = selectChangeList();
        if (changeList != null)
        {
          tfFromDate.setText(DATE_FORMAT.format(changeList.getCommitDate()));
        }
      }
    });
    bnRevHistory.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        CommittedChangeList changeList = selectChangeList();
        if (changeList != null)
        {
          tfFromRev.setText(String.valueOf(changeList.getNumber()));
        }
      }
    });
  }

  @Override
  public boolean isModified(@NotNull FileScope data)
  {
    try
    {
      if (data.getRev() != null)
      {
        return !rbFromRev.isSelected() || !tfFromRev.getText().equals(data.getRev());
      }
      if (data.getDate() != null)
      {
        return !rbFromDate.isSelected() || DATE_FORMAT.parse(tfFromDate.getText()) != data.getDate();
      }

      return !rbFromNone.isSelected();
    }
    catch (ParseException e)
    {
      LOGGER.error(e);
      return false;
    }
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    boolean isHabilited = isHabilitedToEditReview(user);

    RevuUtils.setWriteAccess(isHabilited, rbFromDate, rbFromRev, tfFromDate, tfFromRev);
    fromChoiceListener.actionPerformed(null);
  }

  @Override
  protected void internalValidateInput()
  {
    if (rbFromDate.isSelected())
    {
      boolean dateError;
      try
      {
        DATE_FORMAT.parse(tfFromDate.getText());
        dateError = false;
      }
      catch (ParseException e)
      {
        dateError = true;
      }
      updateError(tfFromDate, dateError, RevuBundle.message("general.invalidDate.text"));
    }

    if (rbFromRev.isSelected())
    {
      boolean revError;
      try
      {
        Long.parseLong(tfFromRev.getText());
        revError = false;
      }
      catch (NumberFormatException e)
      {
        revError = true;
      }
      updateError(tfFromRev, revError, RevuBundle.message("general.invalidRev.text"));
    }
  }

  @Override
  protected void internalUpdateUI(@Nullable FileScope data, boolean requestFocus)
  {
    tfFromDate.setText((data == null) || (data.getDate() == null) ? "" : DATE_FORMAT.format(data.getDate()));
    tfFromRev.setText((data == null) || (data.getRev() == null) ? "" : data.getRev());
    
    if (tfFromDate.getText().length() > 0)
    {
      rbFromDate.setSelected(true);
    }
    else if (tfFromRev.getText().length() > 0)
    {
      rbFromRev.setSelected(true);
    }
    else
    {
      rbFromNone.setSelected(true);
    }
  }

  @Override
  protected void internalUpdateData(@NotNull FileScope data)
  {
    try
    {
      data.setRev(rbFromRev.isEnabled() && rbFromRev.isSelected() ? tfFromRev.getText() : null);
      data.setDate(rbFromDate.isEnabled() && rbFromDate.isSelected() ? DATE_FORMAT.parse(tfFromDate.getText()) : null);
    }
    catch (ParseException e)
    {
      LOGGER.error(e);
    }
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

    AbstractVcs vcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(baseDir);
    assert ((vcs != null) && (vcs.getCommittedChangesProvider() != null));

    RepositoryLocation location = vcs.getCommittedChangesProvider().getLocationFor(filePath);

    return AbstractVcsHelper.getInstance(project)
      .chooseCommittedChangeList(vcs.getCommittedChangesProvider(), location);
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
    rbFromRev = new JRadioButton();
    rbFromRev.setActionCommand("");
    this.$$$loadButtonText$$$(rbFromRev,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.scope.from.rev.title"));
    panel2.add(rbFromRev, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    tfFromRev = new JTextField();
    panel2.add(tfFromRev, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
      false));
    bnRevHistory = new JButton();
    bnRevHistory.setIcon(new ImageIcon(getClass().getResource("/objectBrowser/browser.png")));
    bnRevHistory.setMargin(new Insets(0, 0, 0, 0));
    bnRevHistory.setText("");
    panel2.add(bnRevHistory,
      new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
        GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 1, -1));
    panel2.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      null, null, null, 0, false));
    rbFromNone = new JRadioButton();
    rbFromNone.setActionCommand("");
    rbFromNone.setSelected(true);
    this.$$$loadButtonText$$$(rbFromNone,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.scope.from.none.title"));
    panel3.add(rbFromNone, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel2.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, 1, null, new Dimension(20, -1), null, 0, false));
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), 1, -1));
    panel1.add(panel4, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      null, null, null, 0, false));
    rbFromDate = new JRadioButton();
    rbFromDate.setActionCommand("");
    this.$$$loadButtonText$$$(rbFromDate,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.scope.from.date.title"));
    panel4.add(rbFromDate, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
      null, null, null, 0, false));
    tfFromDate = new JTextField();
    panel4.add(tfFromDate, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
      false));
    bnDateHistory = new JButton();
    bnDateHistory.setIcon(new ImageIcon(getClass().getResource("/objectBrowser/browser.png")));
    bnDateHistory.setMargin(new Insets(0, 0, 0, 0));
    bnDateHistory.setText("");
    panel4.add(bnDateHistory,
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
    buttonGroup.add(rbFromRev);
    buttonGroup.add(rbFromDate);
    buttonGroup.add(rbFromNone);
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
