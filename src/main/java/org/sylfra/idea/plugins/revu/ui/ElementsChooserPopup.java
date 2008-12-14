package org.sylfra.idea.plugins.revu.ui;

import com.intellij.ide.util.ElementsChooser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * @author <a href="mailto:sylfrade@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ElementsChooserPopup<T>
{
  private final ElementsChooser<T> chooser;
  private final Project project;
  private final String title;
  private final IPopupListener<T> popupListener;
  private final String dimensionServiceKey;
  private JBPopup popup;

  public ElementsChooserPopup(@NotNull Project project, @NotNull String title, @Nullable String dimensionServiceKey,
    @NotNull IPopupListener<T> popupListener, @NotNull final IItemRenderer<T> itemRenderer)
  {
    this.project = project;
    this.title = title;
    this.popupListener = popupListener;
    this.dimensionServiceKey = dimensionServiceKey;

    chooser = new ElementsChooser<T>(true)
    {
      @Override
      protected String getItemText(T item)
      {
        return itemRenderer.getText(item);
      }
    };
    chooser.setColorUnmarkedElements(false);
  }

  private JPanel buildContentPane()
  {
    JButton bnOK = new JButton(RevuBundle.message("general.ok.action"));
    Action okAction = new AbstractAction()
    {
      public void actionPerformed(ActionEvent e)
      {
        popupListener.apply(chooser.getMarkedElements());
        popup.cancel();
      }
    };
    bnOK.addActionListener(okAction);

    chooser.getComponent().getActionMap().put(okAction, okAction);
    chooser.getComponent().getInputMap(JComponent.WHEN_FOCUSED).put(
      KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), okAction);

    JButton bnCancel = new JButton(RevuBundle.message("general.cancel.action"));
    bnCancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        popup.cancel();
      }
    });

    JPanel toolbar = new JPanel(new GridLayout(1, 2));
    toolbar.add(bnOK);
    toolbar.add(bnCancel);

    JPanel contentPane = new JPanel(new BorderLayout());
    contentPane.add(chooser, BorderLayout.CENTER);
    contentPane.add(toolbar, BorderLayout.SOUTH);

    return contentPane;
  }

  public void show(@NotNull Component owner, boolean showUnderneath, @NotNull java.util.List<T> allElements,
    @NotNull java.util.List<T> markedElements)
  {
    chooser.setElements(allElements, false);
    chooser.markElements(markedElements);

    ComponentPopupBuilder popupBuilder =
      JBPopupFactory.getInstance().createComponentPopupBuilder(buildContentPane(), chooser)
        .setMovable(true)
        .setFocusable(true)
        .setResizable(true)
        .setRequestFocus(true)
        .setCancelOnClickOutside(false)
        .setTitle(title);

    if (dimensionServiceKey != null)
    {
      popupBuilder.setDimensionServiceKey(project, dimensionServiceKey, true);
    }

    popup = popupBuilder.createPopup();

    Point locationOnScreen = owner.getLocationOnScreen();

    int x = (int) (locationOnScreen.getX());
    int y = (showUnderneath)
      ? (int) locationOnScreen.getY()
      : (int) locationOnScreen.getY() - popup.getContent().getPreferredSize().height;

    popup.showInScreenCoordinates(owner, new Point(x, y));

    // @TODO don't manage to request focus in elements chooser table
  }

  public static interface IItemRenderer<T>
  {
    String getText(T item);
  }

  public static interface IPopupListener<T>
  {
    void apply(@NotNull List<T> markedElements);
  }
}
