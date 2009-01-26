package org.sylfra.idea.plugins.revu.ui.forms.review;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.forms.HistoryForm;
import org.sylfra.idea.plugins.revu.ui.forms.review.referential.ReferentialTabbedPane;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewForm extends AbstractUpdatableForm<Review>
{
  private final List<Review> editedReviews;
  private final Project project;
  private HistoryForm<Review> historyForm;
  private JTextField tfName;
  private JPanel contentPane;
  private JCheckBox ckShare;
  private ReferentialTabbedPane referentialForm;
  private JLabel lbExtends;
  private JButton bnImport;
  private JLabel lbFile;
  private JTextArea taGoal;
  private JComboBox cbStatus;
  private JTabbedPane tabbedPane;
  private Review extendedReview;

  public ReviewForm(@NotNull final Project project, @NotNull List<Review> editedReviews)
  {
    this.project = project;
    this.editedReviews = editedReviews;

    $$$setupUI$$$();
    configureUI(project);
  }

  private void createUIComponents()
  {
    referentialForm = new ReferentialTabbedPane(project);
    cbStatus = new JComboBox(ReviewStatus.values());
  }

  private void configureUI(final Project project)
  {
    RevuUtils.configureTextAreaAsStandardField(taGoal);

    ckShare.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if ((ckShare.isSelected()) && (extendedReview != null) && (!extendedReview.isShared()))
        {
          int result = Messages.showOkCancelDialog(ckShare,
            RevuBundle.message("projectSettings.review.shareWithPrivateLink.text", extendedReview.getName()),
            RevuBundle.message("projectSettings.confirmRemoveReview.title"),
            Messages.getWarningIcon());
          if (result == DialogWrapper.OK_EXIT_CODE)
          {
            Review tmpReview = extendedReview;
            while (tmpReview != null)
            {
              tmpReview.setShared(true);
              tmpReview = tmpReview.getExtendedReview();
            }
          }
        }
      }
    });

    bnImport.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Review currentReview = new Review();
        internalUpdateData(currentReview);

        if (extendedReview == null)
        {
          CreateReviewDialog dialog = new CreateReviewDialog(project, false);
          dialog.show(editedReviews, currentReview);
          if (!dialog.isOK())
          {
            return;
          }

          switch (dialog.getImportType())
          {
            case COPY:
              currentReview.copyFrom(dialog.getImportedReview());
              break;
            case LINK:
              extendedReview = dialog.getImportedReview();
              currentReview.setExtendedReview(extendedReview);
              break;
          }

          updateUI(getEnclosingReview(), currentReview, true);
        }
        else
        {
          extendedReview = null;
          currentReview.setExtendedReview(null);
          updateUI(getEnclosingReview(), currentReview, true);
        }
      }
    });

    cbStatus.setRenderer(new DefaultListCellRenderer()
    {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus)
      {
        value = (value == null) ? "?" : RevuUtils.buildReviewStatusLabel((ReviewStatus) value);
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });
  }

  @Override
  public void dispose()
  {
    historyForm.dispose();
    referentialForm.dispose();
  }

  public boolean isModified(@NotNull Review data)
  {
    if (!checkEquals(data.getExtendedReview(), extendedReview))
    {
      return true;
    }

    if (!checkEquals(tfName.getText(), data.getName()))
    {
      return true;
    }

    if (!checkEquals(cbStatus.getSelectedItem(), data.getStatus()))
    {
      return true;
    }

    if (!checkEquals(ckShare.isSelected(), data.isShared()))
    {
      return true;
    }

    if (referentialForm.isModified(data.getDataReferential()))
    {
      return true;
    }

    return false;
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    boolean canWrite = (getEnclosingReview() != null)
      && (!getEnclosingReview().isEmbedded())
      && (user != null)
      && (user.hasRole(User.Role.ADMIN));
    RevuUtils.setWriteAccess(canWrite, tfName, taGoal, cbStatus, ckShare, bnImport);
  }

  protected void internalValidateInput()
  {
    updateRequiredError(tfName, "".equals(tfName.getText().trim()));
    updateError(referentialForm.getContentPane(), !referentialForm.validateInput(), null);

    updateTabIcons(tabbedPane);

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    Review review = reviewManager.getReviewByName(tfName.getText());
    boolean nameAlreadyExists = ((review != null) && (getEnclosingReview() != null)
      && (!review.getPath().equals(getEnclosingReview().getPath())));
    updateError(tfName, nameAlreadyExists,
      RevuBundle.message("projectSettings.review.importDialog.nameAlreadyExists.text"));
  }

  protected void internalUpdateUI(Review data, boolean requestFocus)
  {
    updateTabIcons(tabbedPane);

    tfName.setText((data == null) ? "" : data.getName());
    taGoal.setText((data == null) ? "" : data.getGoal());
    lbFile.setText((data == null) ? "" : (data.getPath() == null)
      ? "" : RevuVfsUtils.buildPresentablePath(data.getPath()));
    cbStatus.setSelectedItem((data == null) ? ReviewStatus.DRAFT : data.getStatus());
    ckShare.setSelected((data != null) && data.isShared());

    extendedReview = (data == null) ? null : data.getExtendedReview();

    boolean noExtendedReview = (data == null) || (data.getExtendedReview() == null);
    if (noExtendedReview)
    {
      lbExtends.setVisible(false);
    }
    else
    {
      lbExtends.setVisible(true);
      lbExtends.setText(RevuBundle.message("projectSettings.review.referential.extends.text",
        data.getExtendedReview().getName()));
    }
    bnImport.setText(RevuBundle.message(noExtendedReview
      ? "projectSettings.review.referential.import.text"
      : "projectSettings.review.referential.deleteLink.text"));

    referentialForm.updateUI(getEnclosingReview(), (data == null) ? null : data.getDataReferential(), true);

    historyForm.updateUI(getEnclosingReview(), data, false);
  }

  protected void internalUpdateData(@NotNull Review data)
  {
    data.setName(tfName.getText());
    data.setGoal(taGoal.getText());
    data.setPath(lbFile.getText());
    data.setStatus((ReviewStatus) cbStatus.getSelectedItem());
    data.setShared(ckShare.isSelected());

    data.setExtendedReview(extendedReview);

    referentialForm.updateData(data.getDataReferential());

    data.getHistory().setLastUpdatedBy(RevuUtils.getCurrentUser(data));
    data.getHistory().setLastUpdatedOn(new Date());
  }

  public JComponent getPreferredFocusedComponent()
  {
    return tfName;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
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
    createUIComponents();
    contentPane = new JPanel();
    contentPane.setLayout(new CardLayout(0, 0));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new BorderLayout(0, 0));
    contentPane.add(panel1, "review");
    tabbedPane = new JTabbedPane();
    panel1.add(tabbedPane, BorderLayout.CENTER);
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    tabbedPane.addTab(ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
      "projectSettings.review.main.title"), panel2);
    final JScrollPane scrollPane1 = new JScrollPane();
    panel2.add(scrollPane1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    taGoal = new JTextArea();
    taGoal.setLineWrap(true);
    taGoal.setWrapStyleWord(true);
    scrollPane1.setViewportView(taGoal);
    final JLabel label1 = new JLabel();
    this.$$$loadLabelText$$$(label1,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.main.goal.label"));
    panel2.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new BorderLayout(0, 0));
    tabbedPane.addTab(ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
      "projectSettings.review.referential.title"), panel3);
    panel3.add(referentialForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new BorderLayout(0, 0));
    tabbedPane.addTab(ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
      "projectSettings.history.title"), panel4);
    historyForm = new HistoryForm();
    panel4.add(historyForm.$$$getRootComponent$$$(), BorderLayout.CENTER);
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), 0, 0));
    panel1.add(panel5, BorderLayout.NORTH);
    final JPanel panel6 = new JPanel();
    panel6.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
    panel5.add(panel6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    tfName = new JTextField();
    panel6.add(tfName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
      false));
    final JLabel label2 = new JLabel();
    label2.setFont(new Font(label2.getFont().getName(), Font.BOLD, label2.getFont().getSize()));
    this.$$$loadLabelText$$$(label2,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("reviewForm.status.label"));
    panel6.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    panel6.add(cbStatus, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    lbFile = new JLabel();
    lbFile.setText("file");
    lbFile.setVerticalAlignment(1);
    lbFile.setVerticalTextPosition(1);
    panel6.add(lbFile, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_NORTHEAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension(50, -1), null, null, 0,
      false));
    final JLabel label3 = new JLabel();
    label3.setFont(new Font(label3.getFont().getName(), Font.BOLD, label3.getFont().getSize()));
    this.$$$loadLabelText$$$(label3,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("reviewForm.name.label"));
    panel6.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel7 = new JPanel();
    panel7.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel7, BorderLayout.SOUTH);
    ckShare = new JCheckBox();
    this.$$$loadButtonText$$$(ckShare,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.share.text"));
    panel7.add(ckShare, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, 1,
      GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel8 = new JPanel();
    panel8.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    panel7.add(panel8, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    lbExtends = new JLabel();
    lbExtends
      .setIcon(new ImageIcon(getClass().getResource("/org/sylfra/idea/plugins/revu/resources/icons/linkedReview.png")));
    this.$$$loadLabelText$$$(lbExtends,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.referential.extends.text"));
    panel8.add(lbExtends, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    bnImport = new JButton();
    this.$$$loadButtonText$$$(bnImport,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "projectSettings.review.referential.import.text"));
    panel8.add(bnImport, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    label1.setLabelFor(taGoal);
    label2.setLabelFor(cbStatus);
    label3.setLabelFor(tfName);
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadLabelText$$$(JLabel component, String text)
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
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
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
