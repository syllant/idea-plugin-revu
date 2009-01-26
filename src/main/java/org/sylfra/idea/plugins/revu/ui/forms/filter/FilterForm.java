package org.sylfra.idea.plugins.revu.ui.forms.filter;

import com.intellij.openapi.project.Project;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractUpdatableForm;
import org.sylfra.idea.plugins.revu.ui.multichooser.AbstractMultiChooserItem;
import org.sylfra.idea.plugins.revu.ui.multichooser.MultiChooserPanel;
import org.sylfra.idea.plugins.revu.ui.multichooser.UniqueNameMultiChooserPanel;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FilterForm extends AbstractUpdatableForm<Filter>
{
  private final Project project;
  private JPanel contentPane;
  private JTextField tfName;
  private JLabel lbStatus;
  private JLabel lbRecipients;
  private JLabel lbPriority;
  private JLabel lbResolver;
  private JLabel lbTags;
  private JTextField tfSummary;
  private JTextField tfFileRef;
  private MultiChooserPanel<IssueStatus, IssueStatusMCItem> mcpStatus;
  private UniqueNameMultiChooserPanel<User> mcpRecipients;
  private UniqueNameMultiChooserPanel<User> mcpResolver;
  private UniqueNameMultiChooserPanel<IssuePriority> mcpPriority;
  private UniqueNameMultiChooserPanel<IssueTag> mcpTags;

  public FilterForm(@NotNull final Project project)
  {
    this.project = project;

    $$$setupUI$$$();
  }

  private void createUIComponents()
  {
    // Multi Chooser Priority
    lbPriority = new JLabel();
    mcpPriority = new UniqueNameMultiChooserPanel<IssuePriority>(project, lbStatus,
      RevuBundle.message("filterForm.priorityPopup.title"), null, null)
    {
      @NotNull
      protected List<IssuePriority> getReferentialItems(@NotNull DataReferential dataReferential)
      {
        return dataReferential.getIssuePriorities(true);
      }
    };

    // Multi Chooser Status
    lbStatus = new JLabel();
    mcpStatus = new MultiChooserPanel<IssueStatus, IssueStatusMCItem>(project, lbStatus,
      RevuBundle.message("filterForm.statusPopup.title"), null, null)
    {
      @Override
      protected IssueStatusMCItem createMultiChooserItem(@NotNull IssueStatus issueStatus)
      {
        return new IssueStatusMCItem(issueStatus);
      }

      @Override
      protected List<IssueStatus> retrieveAllAvailableElements()
      {
        return Arrays.asList(IssueStatus.values());
      }
    };

    // Multi Chooser Status
    lbRecipients = new JLabel();
    mcpRecipients = new UniqueNameMultiChooserPanel<User>(project, lbRecipients,
      RevuBundle.message("filterForm.recipientsPopup.title"), null, null)
    {
      @NotNull
      protected List<User> getReferentialItems(@NotNull DataReferential dataReferential)
      {
        List<User> result = dataReferential.getUsers(true);
        result.add(0, new User(RevuBundle.message("filterSettings.me.text"), "",
          RevuBundle.message("filterSettings.me.text")));
        return result;
      }
    };
  }

  public boolean isModified(@NotNull Filter data)
  {
    if (!checkEquals(tfName.getText(), data.getName()))
    {
      return true;
    }

    if (!mcpStatus.getSelectedNestedData().equals(data.getStatuses()))
    {
      return true;
    }

    if (!mcpRecipients.getSelectedNestedData().equals(data.getRecipientLogins()))
    {
      return false;
    }

    return true;
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
  }

  protected void internalValidateInput()
  {
    updateRequiredError(tfName, "".equals(tfName.getText().trim()));
//    boolean nameAlreadyExists = ((review != null) && (getEnclosingReview() != null)
//      && (!review.getPath().equals(getEnclosingReview().getPath())));
//    updateError(tfName, nameAlreadyExists,
//      RevuBundle.message("projectSettings.review.importDialog.nameAlreadyExists.text"));
  }

  protected void internalUpdateUI(Filter data, boolean requestFocus)
  {
    tfName.setText((data == null) ? "" : data.getName());
    mcpStatus.setSelectedNestedData((data == null) ? null : data.getStatuses());
    mcpRecipients.setSelectedNestedData((data == null) ? null : data.getRecipientLogins());
  }

  protected void internalUpdateData(@NotNull Filter data)
  {
    data.setName(tfName.getText());

    // Filter lists must not be empty, it seems to cause bus in IDEA deserialization
    data.setStatuses(nullIfEmpty(mcpStatus.getSelectedNestedData()));
    data.setRecipientLogins(nullIfEmpty(mcpRecipients.getSelectedNestedData()));
  }

  @Nullable
  private <T> List<T> nullIfEmpty(@NotNull List<T> list)
  {
    return (list.isEmpty()) ? null : list;
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
    contentPane.setLayout(new BorderLayout(0, 0));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1, BorderLayout.NORTH);
    tfName = new JTextField();
    panel1.add(tfName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL,
      GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0,
      false));
    final JLabel label1 = new JLabel();
    label1.setFont(new Font(label1.getFont().getName(), Font.BOLD, label1.getFont().getSize()));
    this.$$$loadLabelText$$$(label1,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("reviewForm.name.label"));
    panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE,
      GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridBagLayout());
    contentPane.add(panel2, BorderLayout.CENTER);
    panel2.setBorder(BorderFactory.createTitledBorder(
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString(
        "filterForm.definition.title")));
    this.$$$loadLabelText$$$(lbStatus,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.status.label"));
    GridBagConstraints gbc;
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    panel2.add(lbStatus, gbc);
    this.$$$loadLabelText$$$(lbRecipients,
      ResourceBundle.getBundle("org/sylfra/idea/plugins/revu/resources/Bundle").getString("filterForm.recipients.label"));
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.NORTHEAST;
    panel2.add(lbRecipients, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(mcpStatus, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(mcpRecipients, gbc);
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
    gbc = new GridBagConstraints();
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    panel2.add(panel3, gbc);
    label1.setLabelFor(tfName);
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
  public JComponent $$$getRootComponent$$$()
  {
    return contentPane;
  }

  private final static class IssueStatusMCItem extends AbstractMultiChooserItem<IssueStatus>
  {
    protected IssueStatusMCItem(IssueStatus issueStatus)
    {
      super(issueStatus);
    }

    public String getName()
    {
      return RevuUtils.buildIssueStatusLabel(getNestedData());
    }
  }
}