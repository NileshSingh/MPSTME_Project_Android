package android.support.v4.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewParentCompat;
import android.support.v4.view.accessibility.AccessibilityEventCompat;
import android.support.v4.view.accessibility.AccessibilityManagerCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeProviderCompat;
import android.support.v4.view.accessibility.AccessibilityRecordCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class ExploreByTouchHelper extends AccessibilityDelegateCompat
{
  private static final String DEFAULT_CLASS_NAME = View.class.getName();
  public static final int INVALID_ID = -2147483648;
  private int mFocusedVirtualViewId = -2147483648;
  private int mHoveredVirtualViewId = -2147483648;
  private final AccessibilityManager mManager;
  private ExploreByTouchNodeProvider mNodeProvider;
  private final int[] mTempGlobalRect = new int[2];
  private final Rect mTempParentRect = new Rect();
  private final Rect mTempScreenRect = new Rect();
  private final Rect mTempVisibleRect = new Rect();
  private final View mView;

  public ExploreByTouchHelper(View paramView)
  {
    if (paramView == null)
      throw new IllegalArgumentException("View may not be null");
    this.mView = paramView;
    this.mManager = ((AccessibilityManager)paramView.getContext().getSystemService("accessibility"));
  }

  private boolean clearAccessibilityFocus(int paramInt)
  {
    if (isAccessibilityFocused(paramInt))
    {
      this.mFocusedVirtualViewId = -2147483648;
      this.mView.invalidate();
      sendEventForVirtualView(paramInt, 65536);
      return true;
    }
    return false;
  }

  private AccessibilityEvent createEvent(int paramInt1, int paramInt2)
  {
    switch (paramInt1)
    {
    default:
      return createEventForChild(paramInt1, paramInt2);
    case -1:
    }
    return createEventForHost(paramInt2);
  }

  private AccessibilityEvent createEventForChild(int paramInt1, int paramInt2)
  {
    AccessibilityEvent localAccessibilityEvent = AccessibilityEvent.obtain(paramInt2);
    localAccessibilityEvent.setEnabled(true);
    localAccessibilityEvent.setClassName(DEFAULT_CLASS_NAME);
    onPopulateEventForVirtualView(paramInt1, localAccessibilityEvent);
    if ((localAccessibilityEvent.getText().isEmpty()) && (localAccessibilityEvent.getContentDescription() == null))
      throw new RuntimeException("Callbacks must add text or a content description in populateEventForVirtualViewId()");
    localAccessibilityEvent.setPackageName(this.mView.getContext().getPackageName());
    AccessibilityEventCompat.asRecord(localAccessibilityEvent).setSource(this.mView, paramInt1);
    return localAccessibilityEvent;
  }

  private AccessibilityEvent createEventForHost(int paramInt)
  {
    AccessibilityEvent localAccessibilityEvent = AccessibilityEvent.obtain(paramInt);
    ViewCompat.onInitializeAccessibilityEvent(this.mView, localAccessibilityEvent);
    return localAccessibilityEvent;
  }

  private AccessibilityNodeInfoCompat createNode(int paramInt)
  {
    switch (paramInt)
    {
    default:
      return createNodeForChild(paramInt);
    case -1:
    }
    return createNodeForHost();
  }

  private AccessibilityNodeInfoCompat createNodeForChild(int paramInt)
  {
    AccessibilityNodeInfoCompat localAccessibilityNodeInfoCompat = AccessibilityNodeInfoCompat.obtain();
    localAccessibilityNodeInfoCompat.setEnabled(true);
    localAccessibilityNodeInfoCompat.setClassName(DEFAULT_CLASS_NAME);
    onPopulateNodeForVirtualView(paramInt, localAccessibilityNodeInfoCompat);
    if ((localAccessibilityNodeInfoCompat.getText() == null) && (localAccessibilityNodeInfoCompat.getContentDescription() == null))
      throw new RuntimeException("Callbacks must add text or a content description in populateNodeForVirtualViewId()");
    localAccessibilityNodeInfoCompat.getBoundsInParent(this.mTempParentRect);
    if (this.mTempParentRect.isEmpty())
      throw new RuntimeException("Callbacks must set parent bounds in populateNodeForVirtualViewId()");
    int i = localAccessibilityNodeInfoCompat.getActions();
    if ((i & 0x40) != 0)
      throw new RuntimeException("Callbacks must not add ACTION_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
    if ((i & 0x80) != 0)
      throw new RuntimeException("Callbacks must not add ACTION_CLEAR_ACCESSIBILITY_FOCUS in populateNodeForVirtualViewId()");
    localAccessibilityNodeInfoCompat.setPackageName(this.mView.getContext().getPackageName());
    localAccessibilityNodeInfoCompat.setSource(this.mView, paramInt);
    localAccessibilityNodeInfoCompat.setParent(this.mView);
    if (this.mFocusedVirtualViewId == paramInt)
    {
      localAccessibilityNodeInfoCompat.setAccessibilityFocused(true);
      localAccessibilityNodeInfoCompat.addAction(128);
    }
    while (true)
    {
      if (intersectVisibleToUser(this.mTempParentRect))
      {
        localAccessibilityNodeInfoCompat.setVisibleToUser(true);
        localAccessibilityNodeInfoCompat.setBoundsInParent(this.mTempParentRect);
      }
      this.mView.getLocationOnScreen(this.mTempGlobalRect);
      int j = this.mTempGlobalRect[0];
      int k = this.mTempGlobalRect[1];
      this.mTempScreenRect.set(this.mTempParentRect);
      this.mTempScreenRect.offset(j, k);
      localAccessibilityNodeInfoCompat.setBoundsInScreen(this.mTempScreenRect);
      return localAccessibilityNodeInfoCompat;
      localAccessibilityNodeInfoCompat.setAccessibilityFocused(false);
      localAccessibilityNodeInfoCompat.addAction(64);
    }
  }

  private AccessibilityNodeInfoCompat createNodeForHost()
  {
    AccessibilityNodeInfoCompat localAccessibilityNodeInfoCompat = AccessibilityNodeInfoCompat.obtain(this.mView);
    ViewCompat.onInitializeAccessibilityNodeInfo(this.mView, localAccessibilityNodeInfoCompat);
    LinkedList localLinkedList = new LinkedList();
    getVisibleVirtualViews(localLinkedList);
    Iterator localIterator = localLinkedList.iterator();
    while (localIterator.hasNext())
    {
      Integer localInteger = (Integer)localIterator.next();
      localAccessibilityNodeInfoCompat.addChild(this.mView, localInteger.intValue());
    }
    return localAccessibilityNodeInfoCompat;
  }

  private boolean intersectVisibleToUser(Rect paramRect)
  {
    if ((paramRect == null) || (paramRect.isEmpty()));
    ViewParent localViewParent;
    label67: 
    do
    {
      do
        return false;
      while (this.mView.getWindowVisibility() != 0);
      View localView;
      for (localViewParent = this.mView.getParent(); ; localViewParent = localView.getParent())
      {
        if (!(localViewParent instanceof View))
          break label67;
        localView = (View)localViewParent;
        if ((ViewCompat.getAlpha(localView) <= 0.0F) || (localView.getVisibility() != 0))
          break;
      }
    }
    while ((localViewParent == null) || (!this.mView.getLocalVisibleRect(this.mTempVisibleRect)));
    return paramRect.intersect(this.mTempVisibleRect);
  }

  private boolean isAccessibilityFocused(int paramInt)
  {
    return this.mFocusedVirtualViewId == paramInt;
  }

  private boolean manageFocusForChild(int paramInt1, int paramInt2, Bundle paramBundle)
  {
    switch (paramInt2)
    {
    default:
      return false;
    case 64:
      return requestAccessibilityFocus(paramInt1);
    case 128:
    }
    return clearAccessibilityFocus(paramInt1);
  }

  private boolean performAction(int paramInt1, int paramInt2, Bundle paramBundle)
  {
    switch (paramInt1)
    {
    default:
      return performActionForChild(paramInt1, paramInt2, paramBundle);
    case -1:
    }
    return performActionForHost(paramInt2, paramBundle);
  }

  private boolean performActionForChild(int paramInt1, int paramInt2, Bundle paramBundle)
  {
    switch (paramInt2)
    {
    default:
      return onPerformActionForVirtualView(paramInt1, paramInt2, paramBundle);
    case 64:
    case 128:
    }
    return manageFocusForChild(paramInt1, paramInt2, paramBundle);
  }

  private boolean performActionForHost(int paramInt, Bundle paramBundle)
  {
    return ViewCompat.performAccessibilityAction(this.mView, paramInt, paramBundle);
  }

  private boolean requestAccessibilityFocus(int paramInt)
  {
    if ((!this.mManager.isEnabled()) || (!AccessibilityManagerCompat.isTouchExplorationEnabled(this.mManager)));
    do
      return false;
    while (isAccessibilityFocused(paramInt));
    this.mFocusedVirtualViewId = paramInt;
    this.mView.invalidate();
    sendEventForVirtualView(paramInt, 32768);
    return true;
  }

  private void updateHoveredVirtualView(int paramInt)
  {
    if (this.mHoveredVirtualViewId == paramInt)
      return;
    int i = this.mHoveredVirtualViewId;
    this.mHoveredVirtualViewId = paramInt;
    sendEventForVirtualView(paramInt, 128);
    sendEventForVirtualView(i, 256);
  }

  public boolean dispatchHoverEvent(MotionEvent paramMotionEvent)
  {
    int i = 1;
    if ((!this.mManager.isEnabled()) || (!AccessibilityManagerCompat.isTouchExplorationEnabled(this.mManager)));
    do
    {
      return false;
      switch (paramMotionEvent.getAction())
      {
      case 8:
      default:
        return false;
      case 7:
      case 9:
        int j = getVirtualViewAt(paramMotionEvent.getX(), paramMotionEvent.getY());
        updateHoveredVirtualView(j);
        if (j != -2147483648);
        while (true)
        {
          return i;
          i = 0;
        }
      case 10:
      }
    }
    while (this.mFocusedVirtualViewId == -2147483648);
    updateHoveredVirtualView(-2147483648);
    return i;
  }

  public AccessibilityNodeProviderCompat getAccessibilityNodeProvider(View paramView)
  {
    if (this.mNodeProvider == null)
      this.mNodeProvider = new ExploreByTouchNodeProvider(null);
    return this.mNodeProvider;
  }

  public int getFocusedVirtualView()
  {
    return this.mFocusedVirtualViewId;
  }

  protected abstract int getVirtualViewAt(float paramFloat1, float paramFloat2);

  protected abstract void getVisibleVirtualViews(List<Integer> paramList);

  public void invalidateRoot()
  {
    invalidateVirtualView(-1);
  }

  public void invalidateVirtualView(int paramInt)
  {
    sendEventForVirtualView(paramInt, 2048);
  }

  protected abstract boolean onPerformActionForVirtualView(int paramInt1, int paramInt2, Bundle paramBundle);

  protected abstract void onPopulateEventForVirtualView(int paramInt, AccessibilityEvent paramAccessibilityEvent);

  protected abstract void onPopulateNodeForVirtualView(int paramInt, AccessibilityNodeInfoCompat paramAccessibilityNodeInfoCompat);

  public boolean sendEventForVirtualView(int paramInt1, int paramInt2)
  {
    if ((paramInt1 == -2147483648) || (!this.mManager.isEnabled()));
    ViewParent localViewParent;
    do
    {
      return false;
      localViewParent = this.mView.getParent();
    }
    while (localViewParent == null);
    AccessibilityEvent localAccessibilityEvent = createEvent(paramInt1, paramInt2);
    return ViewParentCompat.requestSendAccessibilityEvent(localViewParent, this.mView, localAccessibilityEvent);
  }

  private class ExploreByTouchNodeProvider extends AccessibilityNodeProviderCompat
  {
    private ExploreByTouchNodeProvider()
    {
    }

    public AccessibilityNodeInfoCompat createAccessibilityNodeInfo(int paramInt)
    {
      return ExploreByTouchHelper.this.createNode(paramInt);
    }

    public boolean performAction(int paramInt1, int paramInt2, Bundle paramBundle)
    {
      return ExploreByTouchHelper.this.performAction(paramInt1, paramInt2, paramBundle);
    }
  }
}

/* Location:           C:\Users\Mihir patel\Desktop\New folder (3)\dex2jar-0.0.9.15\dex2jar-0.0.9.15\classes-dex2jar.jar
 * Qualified Name:     android.support.v4.widget.ExploreByTouchHelper
 * JD-Core Version:    0.6.0
 */