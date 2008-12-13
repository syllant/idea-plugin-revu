package org.sylfra.idea.plugins.revu.ui.forms.issue;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.SortedList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettings;
import org.sylfra.idea.plugins.revu.settings.app.RevuAppSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.ElementsChooserPopup;
import org.sylfra.idea.plugins.revu.ui.editor.RevuEditorHandler;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueMainForm extends AbstractIssueForm
{
  private Issue currentIssue;
  private final boolean createMode;
  private TagsPane tagsPane;
  private JPanel contentPane;
  private JTextArea taDesc;
  private JComboBox cbPriority;
  private JTextArea taSummary;
  private JLabel lbLocation;
  private JComboBox cbReview;
  private JRadioButton rbLocationFile;
  private JRadioButton rbLocationGlobal;
  private JRadioButton rbLocationLineRange;
  private JLabel lbSync;
  private ButtonGroup bgLocation;

  public IssueMainForm(@NotNull Project project, boolean createMode)
  {
    super(project);
    this.createMode = createMode;
    configureUI();
  }

  private void configureUI()
  {
    RevuUtils.configureTextAreaAsStandardField(taDesc, taSummary);

    RevuAppSettings appSettings = ServiceManager.getService(RevuAppSettingsComponent.class).getState();

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    cbReview.setEnabled(createMode);
    cbReview.setModel(new ReviewComboBoxModel(buildComboItemsArray(reviewManager.getReviews(null, true,
      false, appSettings.getLogin()), true)));
    cbReview.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message("general.selectComboValue.text");
        }
        else
        {
          value = ((Review) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbPriority.setRenderer(new DefaultListCellRenderer()
    {
      public Component getListCellRendererComponent(JList list, Object value, int index,
        boolean isSelected, boolean cellHasFocus)
      {
        if (value == null)
        {
          value = RevuBundle.message(cbReview.getSelectedItem() == null
            ? "general.selectReviewBeforeFillingCombo.text" : "general.selectComboValue.text");
        }
        else
        {
          value = ((IssuePriority) value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      }
    });

    cbReview.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Object selectedReview = cbReview.getSelectedItem();
        if (selectedReview instanceof Review)
        {
          DataReferential referential = ((Review) selectedReview).getDataReferential();

          cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(
            new TreeSet<IssuePriority>(referential.getItemPrioritiesByName(true).values()), true)));

          tagsPane.setEnabled(true);
        }
        else
        {
          // "[Select a value]" String is selected
          cbPriority.setModel(new DefaultComboBoxModel(buildComboItemsArray(new ArrayList(0), true)));

          tagsPane.setEnabled(false);
        }
      }
    });

    ActionListener locationTypeListener = new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        Issue.LocationType locationType = (rbLocationGlobal.isSelected())
          ? Issue.LocationType.GLOBAL
          : ((rbLocationFile.isSelected()) ? Issue.LocationType.FILE : Issue.LocationType.LINE_RANGE);
        updateLocation(locationType);
      }
    };
    rbLocationGlobal.addActionListener(locationTypeListener);
    rbLocationFile.addActionListener(locationTypeListener);
    rbLocationLineRange.addActionListener(locationTypeListener);

    lbSync.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        assert currentIssue != null;

        RevuEditorHandler editorHandler = project.getComponent(RevuEditorHandler.class);
        currentIssue.setHash(editorHandler.buildNewHash(currentIssue));
        currentIssue.getReview().fireItemUpdated(currentIssue);
      }
    });
  }

  public JComponent getPreferredFocusedComponent()
  {
    return taSummary;
  }

  @NotNull
  public JPanel getContentPane()
  {
    return contentPane;
  }

  protected void internalUpdateUI(@Nullable Issue data, boolean requestFocus)
  {
    // This is not the standard behaviour used in other forms, but this one is not cancelable, so current review
    // item may be modified at any time, don't need to manage a copy before applying changes
    currentIssue = data;

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);

    Collection<Review> reviews = reviewManager.getReviews(true, false);

    Review defaultReview = ((data == null) || (data.getReview() == null))
      ? ((reviews.size() == 1) ? reviews.iterator().next() : null)
      : data.getReview();

    cbReview.setSelectedItem(defaultReview);

    taDesc.setText((data == null) ? "" : data.getDesc());
    taSummary.setText((data == null) ? "" : data.getSummary());
    cbPriority.setSelectedItem((data == null) ? null : data.getPriority());
    lbSync.setVisible((data != null) && (!isIssueSynchronized(data)));
    tagsPane.updateUI((data == null) ? null : data.getTags());
    tagsPane.setEnabled(defaultReview != null);

    Issue.LocationType locationType = (data == null) ? null : data.getLocationType();
    rbLocationFile.setEnabled(!Issue.LocationType.GLOBAL.equals(locationType));
    rbLocationLineRange.setEnabled(!Issue.LocationType.GLOBAL.equals(locationType)
      && !Issue.LocationType.FILE.equals(locationType));
    updateLocation(locationType);
  }

  protected void internalUpdateData(@NotNull Issue data)
  {
    Review review = (Review) cbReview.getSelectedItem();

    data.setReview(review);

    data.setDesc(taDesc.getText());
    data.setSummary(taSummary.getText());
    data.setPriority((IssuePriority) cbPriority.getSelectedItem());
    data.setTags(tagsPane.getSelectedTags());

    // Location
    if (rbLocationGlobal.isSelected())
    {
      data.setFile(null);
      data.setLineStart(-1);
    }
    else if (rbLocationFile.isSelected())
    {
      data.setLineStart(-1);
    }
  }

  public boolean isModified(@NotNull Issue data)
  {
    if (!checkEquals(taDesc.getText(), data.getDesc()))
    {
      return true;
    }

    if (!checkEquals(taSummary.getText(), data.getSummary()))
    {
      return true;
    }

    if (!checkEquals(cbReview.getSelectedItem(), data.getReview()))
    {
      return true;
    }

    if (!checkEquals(cbPriority.getSelectedItem(), data.getPriority()))
    {
      return true;
    }

    if (!checkEquals(tagsPane.getSelectedTags(), data.getTags()))
    {
      return true;
    }

    if (!checkEquals(bgLocation.getSelection().getActionCommand(), data.getLocationType().toString()))
    {
      return true;
    }

    return false;
  }

  @Override
  protected void internalUpdateWriteAccess(@Nullable User user)
  {
    // @TODO
    RevuUtils.setWriteAccess((((currentIssue == null) || (currentIssue.getReview() == null))
      || ((user != null) && (user.hasRole(User.Role.ADMIN)))), cbReview);

    boolean mayReview = ((currentIssue == null) || (currentIssue.getReview() == null))
      || (user != null) && (user.hasRole(User.Role.REVIEWER));
    RevuUtils.setWriteAccess(mayReview, cbPriority,
      rbLocationFile, rbLocationGlobal, rbLocationLineRange);
    tagsPane.setEnabled(mayReview && (cbReview.getSelectedIndex() > 0));
  }

  public void internalValidateInput()
  {
    updateRequiredError(taSummary, "".equals(taSummary.getText().trim()));
    updateRequiredError(cbReview, (!(cbReview.getSelectedItem() instanceof Review)));

    // Check is user is declared in selected review
    if (cbReview.getSelectedItem() instanceof Review)
    {
      Review review = (Review) cbReview.getSelectedItem();

      RevuAppSettings appSettings = ServiceManager.getService(RevuAppSettingsComponent.class).getState();
      User user = review.getDataReferential().getUser(appSettings.getLogin(), true);

      updateRequiredError(cbReview, user == null);
    }
  }

  private boolean isIssueSynchronized(Issue issue)
  {
    RevuEditorHandler editorHandler = project.getComponent(RevuEditorHandler.class);
    return editorHandler.isSynchronized(issue, true);
  }

  private void updateLocation(@Nullable Issue.LocationType locationType)
  {
    if (locationType == null)
    {
      rbLocationGlobal.setSelected(true);
      lbLocation.setText("");
    }

    String filePath;
    String locationPath;
    switch (locationType)
    {
      case GLOBAL:
        rbLocationGlobal.setSelected(true);
        locationPath = RevuBundle.message("form.issue.main.location.global.text");
        break;

      case FILE:
        rbLocationFile.setSelected(true);
        filePath = RevuVfsUtils.buildRelativePath(project, currentIssue.getFile());
        locationPath = (currentIssue.getVcsRev() == null)
          ? filePath
          : RevuBundle.message("form.issue.main.location.pathWithVcsRev.text", filePath,
          currentIssue.getVcsRev());
        break;

      default:
        rbLocationLineRange.setSelected(true);
        filePath = RevuVfsUtils.buildRelativePath(project, currentIssue.getFile());
        String filePathWithVcsRev = (currentIssue.getVcsRev() == null)
          ? filePath
          : RevuBundle.message("form.issue.main.location.pathWithVcsRev.text", filePath,
          currentIssue.getVcsRev());
        locationPath = RevuBundle.message("form.issue.main.location.range.path.text",
          filePathWithVcsRev, (currentIssue.getLineStart() + 1), (currentIssue.getLineEnd() + 1));
    }

    lbLocation.setText(locationPath);
  }

  private void createUIComponents()
  {
    tagsPane = new TagsPane();
  }

  private class ReviewComboBoxModel extends DefaultComboBoxModel implements IReviewListener
  {
    private ReviewComboBoxModel(Object[] objects)
    {
      super(objects);
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.addReviewListener(this);
    }

    public void reviewAdded(Review review)
    {
      super.addElement(review);
    }

    public void reviewChanged(Review review)
    {
    }

    public void reviewDeleted(Review review)
    {
      super.removeElement(review);
    }
  }

  private final class TagsPane extends JPanel
  {
    private JPanel contentPane;
    private ElementsChooserPopup<IssueTag> popup;
    private final JPanel pnTags;
    private final SortedList<IssueTag> selectedTags;
    private AnAction editAction;
    private JComponent toolbar;

    public TagsPane()
    {
      super(new FlowLayout(FlowLayout.LEFT, 0, 0));

      pnTags = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

      editAction = new AnAction(null, RevuBundle.message("form.issue.editTags.tip"),
        RevuIconProvider.getIcon(RevuIconProvider.IconRef.EDIT_TAGS))
      {
        @Override
        public void actionPerformed(AnActionEvent e)
        {
          List<IssueTag> tags = ((Review) cbReview.getSelectedItem()).getDataReferential().getItemTags(true);
          showEditPopup(tags);
        }

        @Override
        public void update(AnActionEvent e)
        {
          e.getPresentation().setEnabled(getTemplatePresentation().isEnabled());
        }
      };

      DefaultActionGroup actionGroup = new DefaultActionGroup();
      actionGroup.add(editAction);
      toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true).getComponent();

      add(pnTags);
      add(toolbar);

      selectedTags = new SortedList<IssueTag>(new Comparator<IssueTag>()
      {
        public int compare(IssueTag o1, IssueTag o2)
        {
          return o1.getName().compareTo(o2.getName());
        }
      });

      popup = new ElementsChooserPopup<IssueTag>(RevuBundle.message("form.issue.tagsPopup.title"),
        new ElementsChooserPopup.IPopupListener<IssueTag>()
        {
          public void apply(@NotNull List<IssueTag> markedElements)
          {
            updateUI(markedElements);
          }
        },
        new ElementsChooserPopup.IItemRenderer<IssueTag>()
        {
          public String getText(IssueTag issue)
          {
            return issue.getName();
          }
        });
    }

    @Override
    public void setEnabled(boolean enabled)
    {
      editAction.getTemplatePresentation().setEnabled(enabled);
    }

    public void showEditPopup(@NotNull List<IssueTag> allTags)
    {
      popup.show(toolbar, false, allTags, selectedTags);
    }

    public void updateUI(@Nullable List<IssueTag> issueTags)
    {
      pnTags.removeAll();

      selectedTags.clear();
      selectedTags.addAll(issueTags);

      if (issueTags != null)
      {
        for (IssueTag tag : issueTags)
        {
          pnTags.add(new ItemTagPanel(tag));
        }
      }
    }

    public List<IssueTag> getSelectedTags()
    {
      int count = pnTags.getComponentCount();

      List<IssueTag> result = new ArrayList<IssueTag>(count);
      for (int i = 0; i < count; i++)
      {
        result.add(((ItemTagPanel) pnTags.getComponent(i)).issueTag);
      }

      return result;
    }

    private class ItemTagPanel extends JLabel
    {
      private final IssueTag issueTag;

      public ItemTagPanel(IssueTag issueTag)
      {
        super(issueTag.getName());
        this.issueTag = issueTag;

        setIcon(RevuIconProvider.getIcon(RevuIconProvider.IconRef.TAG));
        setHorizontalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8),
          BorderFactory.createEtchedBorder()));
      }
    }
  }
}
