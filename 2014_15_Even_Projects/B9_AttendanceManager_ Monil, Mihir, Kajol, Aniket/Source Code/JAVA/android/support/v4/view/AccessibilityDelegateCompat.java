package android.support.v4.view;

import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeProviderCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;

public class AccessibilityDelegateCompat
{
  private static final Object DEFAULT_DELEGATE;
  private static final AccessibilityDelegateImpl IMPL;
  final Object mBridge = IMPL.newAccessiblityDelegateBridge(this);

  static
  {
    if (Build.VERSION.SDK_INT >= 16)
      IMPL = new AccessibilityDelegateJellyBeanImpl();
    while (true)
    {
      DEFAULT_DELEGATE = IMPL.newAccessiblityDelegateDefaultImpl();
      return;
      if (Build.VERSION.SDK_INT >= 14)
      {
        IMPL = new AccessibilityDelegateIcsImpl();
        continue;
      }
      IMPL = new AccessibilityDelegateStubImpl();
    }
  }

  public boolean dispatchPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    return IMPL.dispatchPopulateAccessibilityEvent(DEFAULT_DELEGATE, paramView, paramAccessibilityEvent);
  }

  public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(View paramView)
  {
    return IMPL.getAccessibilityNodeProvider(DEFAULT_DELEGATE, paramView);
  }

  Object getBridge()
  {
    return this.mBridge;
  }

  public void onInitializeAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    IMPL.onInitializeAccessibilityEvent(DEFAULT_DELEGATE, paramView, paramAccessibilityEvent);
  }

  public void onInitializeAccessibilityNodeInfo(View paramView, AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat)
  {
    IMPL.onInitializeAccessibilityNodeInfo(DEFAULT_DELEGATE, paramView, paramAccessibilityNodeInfoCompat);
  }

  public void onPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    IMPL.onPopulateAccessibilityEvent(DEFAULT_DELEGATE, paramView, paramAccessibilityEvent);
  }

  public boolean onRequestSendAccessibilityEvent(ViewGroup paramViewGroup, View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    return IMPL.onRequestSendAccessibilityEvent(DEFAULT_DELEGATE, paramViewGroup, paramView, paramAccessibilityEvent);
  }

  public boolean performAccessibilityAction(View paramView, int paramInt, Bundle paramBundle)
  {
    return IMPL.performAccessibilityAction(DEFAULT_DELEGATE, paramView, paramInt, paramBundle);
  }

  public void sendAccessibilityEvent(View paramView, int paramInt)
  {
    IMPL.sendAccessibilityEvent(DEFAULT_DELEGATE, paramView, paramInt);
  }

  public void sendAccessibilityEventUnchecked(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    IMPL.sendAccessibilityEventUnchecked(DEFAULT_DELEGATE, paramView, paramAccessibilityEvent);
  }

  static class AccessibilityDelegateIcsImpl extends AccessibilityDelegateCompat.AccessibilityDelegateStubImpl
  {
    public boolean dispatchPopulateAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
      return AccessibilityDelegateCompatIcs.dispatchPopulateAccessibilityEvent(paramObject, paramView, paramAccessibilityEvent);
    }

    public Object newAccessiblityDelegateBridge(AccessibilityDelegateCompat paramAccessibilityDelegateCompat)
    {
      return AccessibilityDelegateCompatIcs.newAccessibilityDelegateBridge(new AccessibilityDelegateCompatIcs.AccessibilityDelegateBridge(paramAccessibilityDelegateCompat)
      {
        public boolean dispatchPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          return this.val$compat.dispatchPopulateAccessibilityEvent(paramView, paramAccessibilityEvent);
        }

        public void onInitializeAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          this.val$compat.onInitializeAccessibilityEvent(paramView, paramAccessibilityEvent);
        }

        public void onInitializeAccessibilityNodeInfo(View paramView, Object paramObject)
        {
          this.val$compat.onInitializeAccessibilityNodeInfo(paramView, new AccessibilityNodeInfoCompat(paramObject));
        }

        public void onPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          this.val$compat.onPopulateAccessibilityEvent(paramView, paramAccessibilityEvent);
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup paramViewGroup, View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          return this.val$compat.onRequestSendAccessibilityEvent(paramViewGroup, paramView, paramAccessibilityEvent);
        }

        public void sendAccessibilityEvent(View paramView, int paramInt)
        {
          this.val$compat.sendAccessibilityEvent(paramView, paramInt);
        }

        public void sendAccessibilityEventUnchecked(View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          this.val$compat.sendAccessibilityEventUnchecked(paramView, paramAccessibilityEvent);
        }
      });
    }

    public Object newAccessiblityDelegateDefaultImpl()
    {
      return AccessibilityDelegateCompatIcs.newAccessibilityDelegateDefaultImpl();
    }

    public void onInitializeAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
      AccessibilityDelegateCompatIcs.onInitializeAccessibilityEvent(paramObject, paramView, paramAccessibilityEvent);
    }

    public void onInitializeAccessibilityNodeInfo(Object paramObject, View paramView, AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat)
    {
      AccessibilityDelegateCompatIcs.onInitializeAccessibilityNodeInfo(paramObject, paramView, paramAccessibilityNodeInfoCompat.getInfo());
    }

    public void onPopulateAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
      AccessibilityDelegateCompatIcs.onPopulateAccessibilityEvent(paramObject, paramView, paramAccessibilityEvent);
    }

    public boolean onRequestSendAccessibilityEvent(Object paramObject, ViewGroup paramViewGroup, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
      return AccessibilityDelegateCompatIcs.onRequestSendAccessibilityEvent(paramObject, paramViewGroup, paramView, paramAccessibilityEvent);
    }

    public void sendAccessibilityEvent(Object paramObject, View paramView, int paramInt)
    {
      AccessibilityDelegateCompatIcs.sendAccessibilityEvent(paramObject, paramView, paramInt);
    }

    public void sendAccessibilityEventUnchecked(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
      AccessibilityDelegateCompatIcs.sendAccessibilityEventUnchecked(paramObject, paramView, paramAccessibilityEvent);
    }
  }

  static abstract interface AccessibilityDelegateImpl
  {
    public abstract boolean dispatchPopulateAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent);

    public abstract AccessibilityNodeProviderCompat getAccessibilityNodeProvider(Object paramObject, View paramView);

    public abstract Object newAccessiblityDelegateBridge(AccessibilityDelegateCompat paramAccessibilityDelegateCompat);

    public abstract Object newAccessiblityDelegateDefaultImpl();

    public abstract void onInitializeAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent);

    public abstract void onInitializeAccessibilityNodeInfo(Object paramObject, View paramView, AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat);

    public abstract void onPopulateAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent);

    public abstract boolean onRequestSendAccessibilityEvent(Object paramObject, ViewGroup paramViewGroup, View paramView, AccessibilityEvent paramAccessibilityEvent);

    public abstract boolean performAccessibilityAction(Object paramObject, View paramView, int paramInt, Bundle paramBundle);

    public abstract void sendAccessibilityEvent(Object paramObject, View paramView, int paramInt);

    public abstract void sendAccessibilityEventUnchecked(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent);
  }

  static class AccessibilityDelegateJellyBeanImpl extends AccessibilityDelegateCompat.AccessibilityDelegateIcsImpl
  {
    public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(Object paramObject, View paramView)
    {
      Object localObject = AccessibilityDelegateCompatJellyBean.getAccessibilityNodeProvider(paramObject, paramView);
      if (localObject != null)
        return new AccessibilityNodeProviderCompat(localObject);
      return null;
    }

    public Object newAccessiblityDelegateBridge(AccessibilityDelegateCompat paramAccessibilityDelegateCompat)
    {
      return AccessibilityDelegateCompatJellyBean.newAccessibilityDelegateBridge(new AccessibilityDelegateCompatJellyBean.AccessibilityDelegateBridgeJellyBean(paramAccessibilityDelegateCompat)
      {
        public boolean dispatchPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          return this.val$compat.dispatchPopulateAccessibilityEvent(paramView, paramAccessibilityEvent);
        }

        public Object getAccessibilityNodeProvider(View paramView)
        {
          AccessibilityNodeProviderCompat localAccessibilityNodeProviderCompat = this.val$compat.getAccessibilityNodeProvider(paramView);
          if (localAccessibilityNodeProviderCompat != null)
            return localAccessibilityNodeProviderCompat.getProvider();
          return null;
        }

        public void onInitializeAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          this.val$compat.onInitializeAccessibilityEvent(paramView, paramAccessibilityEvent);
        }

        public void onInitializeAccessibilityNodeInfo(View paramView, Object paramObject)
        {
          this.val$compat.onInitializeAccessibilityNodeInfo(paramView, new AccessibilityNodeInfoCompat(paramObject));
        }

        public void onPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          this.val$compat.onPopulateAccessibilityEvent(paramView, paramAccessibilityEvent);
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup paramViewGroup, View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          return this.val$compat.onRequestSendAccessibilityEvent(paramViewGroup, paramView, paramAccessibilityEvent);
        }

        public boolean performAccessibilityAction(View paramView, int paramInt, Bundle paramBundle)
        {
          return this.val$compat.performAccessibilityAction(paramView, paramInt, paramBundle);
        }

        public void sendAccessibilityEvent(View paramView, int paramInt)
        {
          this.val$compat.sendAccessibilityEvent(paramView, paramInt);
        }

        public void sendAccessibilityEventUnchecked(View paramView, AccessibilityEvent paramAccessibilityEvent)
        {
          this.val$compat.sendAccessibilityEventUnchecked(paramView, paramAccessibilityEvent);
        }
      });
    }

    public boolean performAccessibilityAction(Object paramObject, View paramView, int paramInt, Bundle paramBundle)
    {
      return AccessibilityDelegateCompatJellyBean.performAccessibilityAction(paramObject, paramView, paramInt, paramBundle);
    }
  }

  static class AccessibilityDelegateStubImpl
    implements AccessibilityDelegateCompat.AccessibilityDelegateImpl
  {
    public boolean dispatchPopulateAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
      return false;
    }

    public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(Object paramObject, View paramView)
    {
      return null;
    }

    public Object newAccessiblityDelegateBridge(AccessibilityDelegateCompat paramAccessibilityDelegateCompat)
    {
      return null;
    }

    public Object newAccessiblityDelegateDefaultImpl()
    {
      return null;
    }

    public void onInitializeAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
    }

    public void onInitializeAccessibilityNodeInfo(Object paramObject, View paramView, AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat)
    {
    }

    public void onPopulateAccessibilityEvent(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
    }

    public boolean onRequestSendAccessibilityEvent(Object paramObject, ViewGroup paramViewGroup, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
      return true;
    }

    public boolean performAccessibilityAction(Object paramObject, View paramView, int paramInt, Bundle paramBundle)
    {
      return false;
    }

    public void sendAccessibilityEvent(Object paramObject, View paramView, int paramInt)
    {
    }

    public void sendAccessibilityEventUnchecked(Object paramObject, View paramView, AccessibilityEvent paramAccessibilityEvent)
    {
    }
  }
}

/* Location:           C:\Users\Mihir patel\Desktop\New folder (3)\dex2jar-0.0.9.15\dex2jar-0.0.9.15\classes-dex2jar.jar
 * Qualified Name:     android.support.v4.view.AccessibilityDelegateCompat
 * JD-Core Version:    0.6.0
 */