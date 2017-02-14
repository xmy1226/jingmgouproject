package com.ab.view.carousel;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewDebug.CapturedViewProperty;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Adapter;

public abstract class CarouselAdapter<T extends Adapter> extends ViewGroup
{
  public static final int INVALID_POSITION = -1;
  public static final long INVALID_ROW_ID = -9223372036854775808L;
  public static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;
  public static final int ITEM_VIEW_TYPE_IGNORE = -1;
  static final int SYNC_FIRST_POSITION = 1;
  static final int SYNC_MAX_DURATION_MILLIS = 100;
  static final int SYNC_SELECTED_POSITION = 0;
  boolean mBlockLayoutRequests = false;
  boolean mDataChanged;
  private boolean mDesiredFocusableInTouchModeState;
  private boolean mDesiredFocusableState;
  private View mEmptyView;

  @ViewDebug.ExportedProperty
  int mFirstPosition = 0;
  boolean mInLayout = false;

  @ViewDebug.ExportedProperty
  int mItemCount;
  private int mLayoutHeight;
  boolean mNeedSync = false;

  @ViewDebug.ExportedProperty
  int mNextSelectedPosition = -1;
  long mNextSelectedRowId = -9223372036854775808L;
  int mOldItemCount;
  int mOldSelectedPosition = -1;
  long mOldSelectedRowId = -9223372036854775808L;
  OnItemClickListener mOnItemClickListener;
  OnItemLongClickListener mOnItemLongClickListener;
  OnItemSelectedListener mOnItemSelectedListener;

  @ViewDebug.ExportedProperty
  int mSelectedPosition = -1;
  long mSelectedRowId = -9223372036854775808L;
  private CarouselAdapter<T>.SelectionNotifier mSelectionNotifier;
  int mSpecificTop;
  long mSyncHeight;
  int mSyncMode;
  int mSyncPosition;
  long mSyncRowId = -9223372036854775808L;

  public CarouselAdapter(Context paramContext)
  {
    super(paramContext);
  }

  public CarouselAdapter(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
  }

  public CarouselAdapter(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
  }

  private void fireOnSelected()
  {
    if (this.mOnItemSelectedListener == null)
      return;
    int i = getSelectedItemPosition();
    if (i >= 0)
    {
      View localView = getSelectedView();
      this.mOnItemSelectedListener.onItemSelected(this, localView, i, getAdapter().getItemId(i));
      return;
    }
    this.mOnItemSelectedListener.onNothingSelected(this);
  }

  private void updateEmptyStatus(boolean paramBoolean)
  {
    if (isInFilterMode())
      paramBoolean = false;
    if (paramBoolean)
    {
      if (this.mEmptyView != null)
      {
        this.mEmptyView.setVisibility(0);
        setVisibility(8);
      }
      while (true)
      {
        if (this.mDataChanged)
          onLayout(false, getLeft(), getTop(), getRight(), getBottom());
        return;
        setVisibility(0);
      }
    }
    if (this.mEmptyView != null)
      this.mEmptyView.setVisibility(8);
    setVisibility(0);
  }

  public void addView(View paramView)
  {
    throw new UnsupportedOperationException("addView(View) is not supported in CarouselAdapter");
  }

  public void addView(View paramView, int paramInt)
  {
    throw new UnsupportedOperationException("addView(View, int) is not supported in CarouselAdapter");
  }

  public void addView(View paramView, int paramInt, ViewGroup.LayoutParams paramLayoutParams)
  {
    throw new UnsupportedOperationException("addView(View, int, LayoutParams) is not supported in CarouselAdapter");
  }

  public void addView(View paramView, ViewGroup.LayoutParams paramLayoutParams)
  {
    throw new UnsupportedOperationException("addView(View, LayoutParams) is not supported in CarouselAdapter");
  }

  protected boolean canAnimate()
  {
    return (super.canAnimate()) && (this.mItemCount > 0);
  }

  void checkFocus()
  {
    boolean bool2 = false;
    Adapter localAdapter = getAdapter();
    int i;
    if ((localAdapter != null) && (localAdapter.getCount() != 0))
    {
      i = 0;
      if ((i == 0) || (isInFilterMode()))
        break label109;
      i = 0;
      label38: if ((i == 0) || (!this.mDesiredFocusableInTouchModeState))
        break label114;
      bool1 = true;
      label51: super.setFocusableInTouchMode(bool1);
      if ((i == 0) || (!this.mDesiredFocusableState))
        break label119;
      bool1 = true;
      label69: super.setFocusable(bool1);
      if (this.mEmptyView != null)
        if ((localAdapter == null) || (localAdapter.isEmpty()))
          break label124;
    }
    label109: label114: label119: label124: for (boolean bool1 = bool2; ; bool1 = true)
    {
      updateEmptyStatus(bool1);
      return;
      i = 1;
      break;
      i = 1;
      break label38;
      bool1 = false;
      break label51;
      bool1 = false;
      break label69;
    }
  }

  void checkSelectionChanged()
  {
    if ((this.mSelectedPosition != this.mOldSelectedPosition) || (this.mSelectedRowId != this.mOldSelectedRowId))
    {
      selectionChanged();
      this.mOldSelectedPosition = this.mSelectedPosition;
      this.mOldSelectedRowId = this.mSelectedRowId;
    }
  }

  public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent paramAccessibilityEvent)
  {
    boolean bool = false;
    if (paramAccessibilityEvent.getEventType() == 8)
      paramAccessibilityEvent.setEventType(4);
    View localView = getSelectedView();
    if (localView != null)
      bool = localView.dispatchPopulateAccessibilityEvent(paramAccessibilityEvent);
    if (!bool)
    {
      if (localView != null)
        paramAccessibilityEvent.setEnabled(localView.isEnabled());
      paramAccessibilityEvent.setItemCount(getCount());
      paramAccessibilityEvent.setCurrentItemIndex(getSelectedItemPosition());
    }
    return bool;
  }

  protected void dispatchRestoreInstanceState(SparseArray<Parcelable> paramSparseArray)
  {
    dispatchThawSelfOnly(paramSparseArray);
  }

  protected void dispatchSaveInstanceState(SparseArray<Parcelable> paramSparseArray)
  {
    dispatchFreezeSelfOnly(paramSparseArray);
  }

  int findSyncPosition()
  {
    int i2 = this.mItemCount;
    int n;
    if (i2 == 0)
    {
      n = -1;
      return n;
    }
    long l1 = this.mSyncRowId;
    int i = this.mSyncPosition;
    if (l1 == -9223372036854775808L)
      return -1;
    i = Math.min(i2 - 1, Math.max(0, i));
    long l2 = SystemClock.uptimeMillis();
    int k = i;
    int m = i;
    int j = 0;
    Adapter localAdapter = getAdapter();
    if (localAdapter == null)
      return -1;
    label131: label137: label201: 
    while (true)
    {
      n = i;
      if (localAdapter.getItemId(i) == l1)
        break;
      if (m == i2 - 1)
      {
        n = 1;
        if (k != 0)
          break label131;
      }
      for (int i1 = 1; ; i1 = 0)
      {
        if ((n == 0) || (i1 == 0))
          break label137;
        return -1;
        n = 0;
        break;
      }
      if ((i1 != 0) || ((j != 0) && (n == 0)))
      {
        m += 1;
        i = m;
      }
      for (j = 0; ; j = 1)
      {
        do
        {
          if (SystemClock.uptimeMillis() <= l2 + 100L)
            break label201;
          break;
        }
        while ((n == 0) && ((j != 0) || (i1 != 0)));
        k -= 1;
        i = k;
      }
    }
  }

  public abstract T getAdapter();

  @ViewDebug.CapturedViewProperty
  public int getCount()
  {
    return this.mItemCount;
  }

  public View getEmptyView()
  {
    return this.mEmptyView;
  }

  public int getFirstVisiblePosition()
  {
    return this.mFirstPosition;
  }

  public Object getItemAtPosition(int paramInt)
  {
    Adapter localAdapter = getAdapter();
    if ((localAdapter == null) || (paramInt < 0))
      return null;
    return localAdapter.getItem(paramInt);
  }

  public long getItemIdAtPosition(int paramInt)
  {
    Adapter localAdapter = getAdapter();
    if ((localAdapter == null) || (paramInt < 0))
      return -9223372036854775808L;
    return localAdapter.getItemId(paramInt);
  }

  public int getLastVisiblePosition()
  {
    return this.mFirstPosition + getChildCount() - 1;
  }

  public final OnItemClickListener getOnItemClickListener()
  {
    return this.mOnItemClickListener;
  }

  public final OnItemLongClickListener getOnItemLongClickListener()
  {
    return this.mOnItemLongClickListener;
  }

  public final OnItemSelectedListener getOnItemSelectedListener()
  {
    return this.mOnItemSelectedListener;
  }

  public int getPositionForView(View paramView)
  {
    while (true)
    {
      int i;
      try
      {
        View localView = (View)paramView.getParent();
        boolean bool = localView.equals(this);
        if (bool)
        {
          int j = getChildCount();
          i = 0;
          if (i >= j)
            return -1;
        }
        else
        {
          paramView = localView;
          continue;
        }
      }
      catch (ClassCastException paramView)
      {
        return -1;
      }
      if (getChildAt(i).equals(paramView))
        return this.mFirstPosition + i;
      i += 1;
    }
  }

  public Object getSelectedItem()
  {
    Adapter localAdapter = getAdapter();
    int i = getSelectedItemPosition();
    if ((localAdapter != null) && (localAdapter.getCount() > 0) && (i >= 0))
      return localAdapter.getItem(i);
    return null;
  }

  @ViewDebug.CapturedViewProperty
  public long getSelectedItemId()
  {
    return this.mNextSelectedRowId;
  }

  @ViewDebug.CapturedViewProperty
  public int getSelectedItemPosition()
  {
    return this.mNextSelectedPosition;
  }

  public abstract View getSelectedView();

  void handleDataChanged()
  {
    int m = this.mItemCount;
    int j = 0;
    int k = 0;
    if (m > 0)
    {
      int i = k;
      if (this.mNeedSync)
      {
        this.mNeedSync = false;
        j = findSyncPosition();
        i = k;
        if (j >= 0)
        {
          i = k;
          if (lookForSelectablePosition(j, true) == j)
          {
            setNextSelectedPositionInt(j);
            i = 1;
          }
        }
      }
      j = i;
      if (i == 0)
      {
        k = getSelectedItemPosition();
        j = k;
        if (k >= m)
          j = m - 1;
        k = j;
        if (j < 0)
          k = 0;
        j = lookForSelectablePosition(k, true);
        m = j;
        if (j < 0)
          m = lookForSelectablePosition(k, false);
        j = i;
        if (m >= 0)
        {
          setNextSelectedPositionInt(m);
          checkSelectionChanged();
          j = 1;
        }
      }
    }
    if (j == 0)
    {
      this.mSelectedPosition = -1;
      this.mSelectedRowId = -9223372036854775808L;
      this.mNextSelectedPosition = -1;
      this.mNextSelectedRowId = -9223372036854775808L;
      this.mNeedSync = false;
      checkSelectionChanged();
    }
  }

  boolean isInFilterMode()
  {
    return false;
  }

  int lookForSelectablePosition(int paramInt, boolean paramBoolean)
  {
    return paramInt;
  }

  protected void onLayout(boolean paramBoolean, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.mLayoutHeight = getHeight();
  }

  public boolean performItemClick(View paramView, int paramInt, long paramLong)
  {
    boolean bool = false;
    if (this.mOnItemClickListener != null)
    {
      playSoundEffect(0);
      this.mOnItemClickListener.onItemClick(this, paramView, paramInt, paramLong);
      bool = true;
    }
    return bool;
  }

  void rememberSyncState()
  {
    if (getChildCount() > 0)
    {
      this.mNeedSync = true;
      this.mSyncHeight = this.mLayoutHeight;
      if (this.mSelectedPosition >= 0)
      {
        localView = getChildAt(this.mSelectedPosition - this.mFirstPosition);
        this.mSyncRowId = this.mNextSelectedRowId;
        this.mSyncPosition = this.mNextSelectedPosition;
        if (localView != null)
          this.mSpecificTop = localView.getTop();
        this.mSyncMode = 0;
      }
    }
    else
    {
      return;
    }
    View localView = getChildAt(0);
    Adapter localAdapter = getAdapter();
    if ((this.mFirstPosition >= 0) && (this.mFirstPosition < localAdapter.getCount()));
    for (this.mSyncRowId = localAdapter.getItemId(this.mFirstPosition); ; this.mSyncRowId = -1L)
    {
      this.mSyncPosition = this.mFirstPosition;
      if (localView != null)
        this.mSpecificTop = localView.getTop();
      this.mSyncMode = 1;
      return;
    }
  }

  public void removeAllViews()
  {
    throw new UnsupportedOperationException("removeAllViews() is not supported in CarouselAdapter");
  }

  public void removeView(View paramView)
  {
    throw new UnsupportedOperationException("removeView(View) is not supported in CarouselAdapter");
  }

  public void removeViewAt(int paramInt)
  {
    throw new UnsupportedOperationException("removeViewAt(int) is not supported in CarouselAdapter");
  }

  void selectionChanged()
  {
    if (this.mOnItemSelectedListener != null)
    {
      if ((!this.mInLayout) && (!this.mBlockLayoutRequests))
        break label81;
      if (this.mSelectionNotifier == null)
        this.mSelectionNotifier = new SelectionNotifier(null);
      this.mSelectionNotifier.post(this.mSelectionNotifier);
    }
    while (true)
    {
      if ((this.mSelectedPosition != -1) && (isShown()) && (!isInTouchMode()))
        sendAccessibilityEvent(4);
      return;
      label81: fireOnSelected();
    }
  }

  public abstract void setAdapter(T paramT);

  public void setEmptyView(View paramView)
  {
    this.mEmptyView = paramView;
    paramView = getAdapter();
    if ((paramView != null) && (!paramView.isEmpty()));
    for (boolean bool = false; ; bool = true)
    {
      updateEmptyStatus(bool);
      return;
    }
  }

  public void setFocusable(boolean paramBoolean)
  {
    boolean bool = true;
    Adapter localAdapter = getAdapter();
    int i;
    if ((localAdapter != null) && (localAdapter.getCount() != 0))
    {
      i = 0;
      this.mDesiredFocusableState = paramBoolean;
      if (!paramBoolean)
        this.mDesiredFocusableInTouchModeState = false;
      if (!paramBoolean)
        break label69;
      paramBoolean = bool;
      if (i != 0)
        if (!isInFilterMode())
          break label69;
    }
    label69: for (paramBoolean = bool; ; paramBoolean = false)
    {
      super.setFocusable(paramBoolean);
      return;
      i = 1;
      break;
    }
  }

  public void setFocusableInTouchMode(boolean paramBoolean)
  {
    boolean bool = true;
    Adapter localAdapter = getAdapter();
    int i;
    if ((localAdapter != null) && (localAdapter.getCount() != 0))
    {
      i = 0;
      this.mDesiredFocusableInTouchModeState = paramBoolean;
      if (paramBoolean)
        this.mDesiredFocusableState = true;
      if (!paramBoolean)
        break label69;
      paramBoolean = bool;
      if (i != 0)
        if (!isInFilterMode())
          break label69;
    }
    label69: for (paramBoolean = bool; ; paramBoolean = false)
    {
      super.setFocusableInTouchMode(paramBoolean);
      return;
      i = 1;
      break;
    }
  }

  void setNextSelectedPositionInt(int paramInt)
  {
    this.mNextSelectedPosition = paramInt;
    this.mNextSelectedRowId = getItemIdAtPosition(paramInt);
    if ((this.mNeedSync) && (this.mSyncMode == 0) && (paramInt >= 0))
    {
      this.mSyncPosition = paramInt;
      this.mSyncRowId = this.mNextSelectedRowId;
    }
  }

  public void setOnClickListener(View.OnClickListener paramOnClickListener)
  {
    throw new RuntimeException("Don't call setOnClickListener for an CarouselAdapter. You probably want setOnItemClickListener instead");
  }

  public void setOnItemClickListener(OnItemClickListener paramOnItemClickListener)
  {
    this.mOnItemClickListener = paramOnItemClickListener;
  }

  public void setOnItemLongClickListener(OnItemLongClickListener paramOnItemLongClickListener)
  {
    if (!isLongClickable())
      setLongClickable(true);
    this.mOnItemLongClickListener = paramOnItemLongClickListener;
  }

  public void setOnItemSelectedListener(OnItemSelectedListener paramOnItemSelectedListener)
  {
    this.mOnItemSelectedListener = paramOnItemSelectedListener;
  }

  void setSelectedPositionInt(int paramInt)
  {
    this.mSelectedPosition = paramInt;
    this.mSelectedRowId = getItemIdAtPosition(paramInt);
  }

  public abstract void setSelection(int paramInt);

  public static class AdapterContextMenuInfo
    implements ContextMenu.ContextMenuInfo
  {
    public long id;
    public int position;
    public View targetView;

    public AdapterContextMenuInfo(View paramView, int paramInt, long paramLong)
    {
      this.targetView = paramView;
      this.position = paramInt;
      this.id = paramLong;
    }
  }

  class AdapterDataSetObserver extends DataSetObserver
  {
    private Parcelable mInstanceState = null;

    AdapterDataSetObserver()
    {
    }

    public void clearSavedState()
    {
      this.mInstanceState = null;
    }

    public void onChanged()
    {
      CarouselAdapter.this.mDataChanged = true;
      CarouselAdapter.this.mOldItemCount = CarouselAdapter.this.mItemCount;
      CarouselAdapter.this.mItemCount = CarouselAdapter.this.getAdapter().getCount();
      if ((CarouselAdapter.this.getAdapter().hasStableIds()) && (this.mInstanceState != null) && (CarouselAdapter.this.mOldItemCount == 0) && (CarouselAdapter.this.mItemCount > 0))
      {
        CarouselAdapter.this.onRestoreInstanceState(this.mInstanceState);
        this.mInstanceState = null;
      }
      while (true)
      {
        CarouselAdapter.this.checkFocus();
        CarouselAdapter.this.requestLayout();
        return;
        CarouselAdapter.this.rememberSyncState();
      }
    }

    public void onInvalidated()
    {
      CarouselAdapter.this.mDataChanged = true;
      if (CarouselAdapter.this.getAdapter().hasStableIds())
        this.mInstanceState = CarouselAdapter.this.onSaveInstanceState();
      CarouselAdapter.this.mOldItemCount = CarouselAdapter.this.mItemCount;
      CarouselAdapter.this.mItemCount = 0;
      CarouselAdapter.this.mSelectedPosition = -1;
      CarouselAdapter.this.mSelectedRowId = -9223372036854775808L;
      CarouselAdapter.this.mNextSelectedPosition = -1;
      CarouselAdapter.this.mNextSelectedRowId = -9223372036854775808L;
      CarouselAdapter.this.mNeedSync = false;
      CarouselAdapter.this.checkSelectionChanged();
      CarouselAdapter.this.checkFocus();
      CarouselAdapter.this.requestLayout();
    }
  }

  public static abstract interface OnItemClickListener
  {
    public abstract void onItemClick(CarouselAdapter<?> paramCarouselAdapter, View paramView, int paramInt, long paramLong);
  }

  public static abstract interface OnItemLongClickListener
  {
    public abstract boolean onItemLongClick(CarouselAdapter<?> paramCarouselAdapter, View paramView, int paramInt, long paramLong);
  }

  public static abstract interface OnItemSelectedListener
  {
    public abstract void onItemSelected(CarouselAdapter<?> paramCarouselAdapter, View paramView, int paramInt, long paramLong);

    public abstract void onNothingSelected(CarouselAdapter<?> paramCarouselAdapter);
  }

  private class SelectionNotifier extends Handler
    implements Runnable
  {
    private SelectionNotifier()
    {
    }

    public void run()
    {
      if (CarouselAdapter.this.mDataChanged)
      {
        post(this);
        return;
      }
      CarouselAdapter.this.fireOnSelected();
    }
  }
}

/* Location:           F:\一周备份\面试apk\希望代码没混淆\jingmgou\jingmgou2\classes-dex2jar.jar
 * Qualified Name:     com.ab.view.carousel.CarouselAdapter
 * JD-Core Version:    0.6.2
 */