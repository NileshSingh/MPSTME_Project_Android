package android.support.v4.view;

import android.view.KeyEvent;
import android.view.KeyEvent.Callback;
import android.view.KeyEvent.DispatcherState;
import android.view.View;

class KeyEventCompatEclair
{
  public static boolean dispatch(KeyEvent paramKeyEvent, KeyEvent.Callback paramCallback, Object paramObject1, Object paramObject2)
  {
    return paramKeyEvent.dispatch(paramCallback, (KeyEvent.DispatcherState)paramObject1, paramObject2);
  }

  public static Object getKeyDispatcherState(View paramView)
  {
    return paramView.getKeyDispatcherState();
  }

  public static boolean isTracking(KeyEvent paramKeyEvent)
  {
    return paramKeyEvent.isTracking();
  }

  public static void startTracking(KeyEvent paramKeyEvent)
  {
    paramKeyEvent.startTracking();
  }
}

/* Location:           C:\Users\Mihir patel\Desktop\New folder (3)\dex2jar-0.0.9.15\dex2jar-0.0.9.15\classes-dex2jar.jar
 * Qualified Name:     android.support.v4.view.KeyEventCompatEclair
 * JD-Core Version:    0.6.0
 */