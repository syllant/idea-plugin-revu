package org.sylfra.idea.plugins.revu.ui.statusbar;

import com.intellij.openapi.ui.MessageType;
import org.sylfra.idea.plugins.revu.RevuIconProvider;

import java.awt.event.ActionListener;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StatusBarMessage
{
  public static enum Type
  {
    INFO(0, RevuIconProvider.IconRef.STATUS_BAR_INFO, MessageType.INFO),
    WARNING(1, RevuIconProvider.IconRef.STATUS_BAR_WARNING, MessageType.WARNING),
    ERROR(2, RevuIconProvider.IconRef.STATUS_BAR_ERROR, MessageType.ERROR);

    private final int priority;
    private final RevuIconProvider.IconRef iconRef;
    private final MessageType messageType;

    Type(int priority, RevuIconProvider.IconRef iconRef, MessageType messageType)
    {
      this.priority = priority;
      this.iconRef = iconRef;
      this.messageType = messageType;
    }

    public int getPriority()
    {
      return priority;
    }

    public RevuIconProvider.IconRef getIconRef()
    {
      return iconRef;
    }

    public MessageType getMessageType()
    {
      return messageType;
    }
  }

  private Type type;
  private String title;
  private String details;
  private String actionText;
  private ActionListener action;

  public StatusBarMessage(Type type, String title, String details, String actionText, ActionListener action)
  {
    this.type = type;
    this.title = title;
    this.details = details;
    this.actionText = actionText;
    this.action = action;
  }

  public StatusBarMessage(Type type, String title, String details)
  {
    this(type, title, details, null, null);
  }

  public StatusBarMessage(Type type, String title)
  {
    this(type, title, null, null, null);
  }

  public Type getType()
  {
    return type;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getDetails()
  {
    return details;
  }

  public void setDetails(String details)
  {
    this.details = details;
  }

  public String getActionText()
  {
    return actionText;
  }

  public void setActionText(String actionText)
  {
    this.actionText = actionText;
  }

  public ActionListener getAction()
  {
    return action;
  }

  public void setAction(ActionListener action)
  {
    this.action = action;
  }
}
