package android.support.v4.view;

import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

class ViewCompatICS
{
  public static boolean canScrollHorizontally(View paramView, int paramInt)
  {
    return paramView.canScrollHorizontally(paramInt);
  }

  public static boolean canScrollVertically(View paramView, int paramInt)
  {
    return paramView.canScrollVertically(paramInt);
  }

  public static void onInitializeAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    paramView.onInitializeAccessibilityEvent(paramAccessibilityEvent);
  }

  public static void onInitializeAccessibilityNodeInfo(View paramView, Object paramObject)
  {
    paramView.onInitializeAccessibilityNodeInfo((AccessibilityNodeInfo)paramObject);
  }

  public static void onPopulateAccessibilityEvent(View paramView, AccessibilityEvent paramAccessibilityEvent)
  {
    paramView.onPopulateAccessibilityEvent(paramAccessibilityEvent);
  }

  public static void setAccessibilityDelegate(View paramView, Object paramObject)
  {
    paramView.setAccessibilityDelegate((View.AccessibilityDelegate)paramObject);
  }
}

/* Location:           C:\Users\Mihir patel\Desktop\New folder (3)\dex2jar-0.0.9.15\dex2jar-0.0.9.15\classes-dex2jar.jar
 * Qualified Name:     android.support.v4.view.ViewCompatICS
 * JD-Core Version:    0.6.0
 */