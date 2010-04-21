package org.sylfra.idea.plugins.revu.ui.multichooser;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.containers.SortedList;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.ui.DashedBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class MultiChooserPanel<NestedData, Item extends IMultiChooserItem<NestedData>> extends JPanel
{
  protected final Project project;
  private MultiChooserPopup<Item> popup;
  private final SortedList<Item> selectedItems;
  private AnAction editAction;
  private JComponent toolbar;
  private final JLabel label;
  private final String popupTitle;
  private final String dimensionKeySuffix;
  private final RevuIconProvider.IconRef iconRef;

  public MultiChooserPanel(@NotNull Project project, @NotNull JLabel label, @NotNull String popupTitle,
    @Nullable String dimensionKeySuffix, @Nullable RevuIconProvider.IconRef iconRef)
  {
    this.project = project;
    this.label = label;
    this.popupTitle = popupTitle;
    this.dimensionKeySuffix = dimensionKeySuffix;
    this.iconRef = iconRef;

    selectedItems = new SortedList<Item>(new Comparator<Item>()
    {
      public int compare(Item o1, Item o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });
    configureUI();
  }

  private void configureUI()
  {
    setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

    editAction = new AnAction(RevuBundle.message("multiChooser.edit.tip"), null,
      RevuIconProvider.getIcon(RevuIconProvider.IconRef.EDIT_MULTI_CHOOSER))
    {
      @Override
      public void actionPerformed(AnActionEvent e)
      {
        List<NestedData> datas = retrieveAllAvailableElements();

        showEditPopup(toItemsList(datas));
      }

      @Override
      public void update(AnActionEvent e)
      {
        e.getPresentation().setEnabled(getTemplatePresentation().isEnabled());
      }
    };
    // Should use #registerCustomShortcutSet ?
    getActionMap().put(editAction, new AbstractAction()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (editAction.getTemplatePresentation().isEnabled())
        {
          editAction.actionPerformed(null);
        }
      }
    });
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
      KeyStroke.getKeyStroke(label.getDisplayedMnemonic(), KeyEvent.ALT_MASK), editAction);


    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(editAction);
    toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, true).getComponent();
    label.setLabelFor(toolbar.getComponent(0));

    add(toolbar);

    popup = new MultiChooserPopup<Item>(project, popupTitle,
      (dimensionKeySuffix == null) ? null : RevuPlugin.PLUGIN_NAME + "." + dimensionKeySuffix,
      new MultiChooserPopup.IPopupListener<Item>()
      {
        public void apply(@NotNull List<Item> markedElements)
        {
          setSelectedItems(markedElements);
        }
      },
      new MultiChooserPopup.IItemRenderer<Item>()
      {
        public String getText(Item issue)
        {
          return issue.getName();
        }
      });
  }

  private List<Item> toItemsList(@Nullable List<NestedData> datas)
  {
    if (datas == null)
    {
      return Collections.emptyList();
    }
    
    List<Item> items = new ArrayList<Item>(datas.size());
    for (NestedData data : datas)
    {
      items.add(createMultiChooserItem(data));
    }

    return items;
  }

  @SuppressWarnings({"unchecked"})
  @NotNull
  public List<NestedData> getSelectedItemDatas()
  {
    // First component is the edit button
    int count = getComponentCount();

    List<NestedData> result = new ArrayList<NestedData>(count - 1);
    for (int i = 1; i < count; i++)
    {
      result.add(((ItemPanel) getComponent(i)).item.getNestedData());
    }

    return result;
  }

  public void setSelectedItemDatas(@Nullable List<NestedData> nestedDataList)
  {
    setSelectedItems(toItemsList(nestedDataList));
  }

  protected void setSelectedItems(@Nullable List<Item> items)
  {
    int componentCount = getComponentCount();
    for (int i = componentCount - 1; i > 0; i--)
    {
      remove(i);
    }

    selectedItems.clear();
    selectedItems.addAll(items);

    if (items != null)
    {
      for (Item tag : items)
      {
        add(new ItemPanel(tag));
      }
    }

    revalidate();
    repaint();
  }

  public void setEnabled(boolean enabled)
  {
    editAction.getTemplatePresentation().setEnabled(enabled);
  }

  public void showEditPopup(@NotNull List<Item> allTags)
  {
    List<Item> allSortedTags = new ArrayList<Item>(allTags);
    Collections.sort(allSortedTags);
    popup.show(toolbar, false, allSortedTags, selectedItems);
  }

  @Override
  public Dimension getPreferredSize()
  {
    return super.getPreferredSize();
  }

  protected abstract Item createMultiChooserItem(@NotNull NestedData data);
  protected abstract List<NestedData> retrieveAllAvailableElements();

  private class ItemPanel extends JLabel
  {
    private final Item item;

    public ItemPanel(Item item)
    {
      super(item.getName());
      this.item = item;

      if (iconRef != null)
      {
        setIcon(RevuIconProvider.getIcon(iconRef));
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8),
          BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
            BorderFactory.createEmptyBorder(1, 1, 1, 3))));
      }
      else
      {
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3),
          new DashedBorder(UIUtil.getBoundsColor())));
      }
      setHorizontalAlignment(SwingConstants.CENTER);
    }
  }
}
