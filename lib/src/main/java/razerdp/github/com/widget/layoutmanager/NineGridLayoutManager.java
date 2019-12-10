package razerdp.github.com.widget.layoutmanager;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;

import razerdp.github.com.widget.PhotoContents;
import razerdp.github.com.widget.util.SimplePool;

/**
 * Created by 大灯泡 on 2019/7/2
 * <p>
 * Description：
 */
public class NineGridLayoutManager extends PhotoContents.LayoutManager {
    private static final String TAG = "NineGridLayoutManager";
    private int itemSpace = 0;
    private LayoutState mLayoutState;
    SimplePool<PhotoContents.ViewHolder> mSinglePool;

    public NineGridLayoutManager(int itemSpace) {
        this.itemSpace = itemSpace;
        mSinglePool = new SimplePool<>(9);
        mLayoutState = new LayoutState();
    }

    @Override
    public void onMeasure(@NonNull SimplePool<PhotoContents.ViewHolder> pool, @NonNull PhotoContents.State state, int widthSpec, int heightSpec) {
        mLayoutState.widthMeasureSpec = widthSpec;
        mLayoutState.heightMeasureSpec = heightSpec;
        mLayoutState.itemCount = state.getItemCount();
        if (state.isDataChanged()) {
            doRecycler();
        }

        if (state.getItemCount() == 1) {
            int parentHeight = View.MeasureSpec.getSize(heightSpec) - getParent().getPaddingTop() - getParent().getPaddingBottom();
            int parentWidth = View.MeasureSpec.getSize(widthSpec) - getParent().getPaddingLeft() - getParent().getPaddingRight();
            View v = measureChild(getChildAt(0), 0, widthSpec, View.MeasureSpec.makeMeasureSpec(parentHeight <= 0 ? parentWidth : parentHeight, View.MeasureSpec.AT_MOST));
            mLayoutState.widthMeasureSpec = widthSpec;
            mLayoutState.heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(v.getMeasuredHeight(), View.MeasureSpec.AT_MOST);
        } else {
            if (View.MeasureSpec.getMode(widthSpec) == View.MeasureSpec.EXACTLY && View.MeasureSpec.getMode(heightSpec) == View.MeasureSpec.EXACTLY) {
                measureWithExactly(state, widthSpec, heightSpec);
            } else {
                measureWithAtMost(state, widthSpec, heightSpec);
            }
        }

        setMeasuredDimension(mLayoutState.widthMeasureSpec, mLayoutState.heightMeasureSpec);
    }

    private void measureWithExactly(PhotoContents.State state, int widthSpec, int heightSpec) {
        int parentHeight = View.MeasureSpec.getSize(heightSpec) - getParent().getPaddingTop() - getParent().getPaddingBottom();
        int parentWidth = View.MeasureSpec.getSize(widthSpec) - getParent().getPaddingLeft() - getParent().getPaddingRight();

        final int itemCount = state.getItemCount();

        int childWidth = (parentWidth - (itemSpace << 1)) / 3;
        int childHeight = (parentHeight - (itemSpace << 1)) / 3;
        int size = Math.min(childWidth, childHeight);

        for (int i = 0; i < itemCount; i++) {
            measureChild(getChildAt(i), i,
                    View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY));
        }

        mLayoutState.widthMeasureSpec = widthSpec;
        mLayoutState.heightMeasureSpec = heightSpec;
    }

    private void measureWithAtMost(PhotoContents.State state, int widthSpec, int heightSpec) {
        int widthMode = View.MeasureSpec.getMode(widthSpec);
        int heightMode = View.MeasureSpec.getMode(heightSpec);

        int parentHeight = View.MeasureSpec.getSize(heightSpec) - getParent().getPaddingTop() - getParent().getPaddingBottom();
        int parentWidth = View.MeasureSpec.getSize(widthSpec) - getParent().getPaddingLeft() - getParent().getPaddingRight();

        final int itemCount = state.getItemCount();

        int rowCount = itemCount % 3 == 0 ? itemCount / 3 : itemCount / 3 + 1;
        int columnCount = itemCount >= 3 ? 3 : itemCount;

        if (itemCount == 4) {
            rowCount = 2;
            columnCount = 2;
        }

        if (widthMode == View.MeasureSpec.EXACTLY) {
            int size = (parentWidth - (itemSpace << 1)) / 3;

            for (int i = 0; i < itemCount; i++) {
                measureChild(getChildAt(i), i, View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY));
            }
            parentHeight = size * rowCount + Math.max(0, rowCount - 1) * itemSpace;
            heightSpec = View.MeasureSpec.makeMeasureSpec(parentHeight, heightMode);
        } else if (heightMode == View.MeasureSpec.EXACTLY) {
            int size = (parentHeight - (itemSpace << 1)) / 3;
            for (int i = 0; i < itemCount; i++) {
                measureChild(getChildAt(i), i, View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY));
            }
            parentWidth = size * columnCount + Math.max(0, columnCount - 1) * itemSpace;
            widthSpec = View.MeasureSpec.makeMeasureSpec(parentWidth, widthMode);
        }
        mLayoutState.widthMeasureSpec = widthSpec;
        mLayoutState.heightMeasureSpec = heightSpec;
    }


    private View measureChild(View v, int position, int widthSpec, int heightSpec) {
        if (v == null) {
            v = obtainViewHolder(position).rootView;
        }
        if (v.getVisibility() == View.GONE) return v;
        v.measure(widthSpec, heightSpec);
        if (v.getParent() == null) {
            addViewInternal(v, position);
        }
        return v;
    }

    private void addViewInternal(View v, int pos) {
        if (v == null) return;
        PhotoContents.ViewHolder holder = findViewHolderForView(v);
        if (holder == null) {
            throw new NullPointerException("ViewHolder为空");
        }

        getParent().getAdapter().bindViewHolder(holder, pos);
        addView(v, -1, (PhotoContents.LayoutParams) v.getLayoutParams(), true);
    }

    @Override
    public void onLayoutChildren(@NonNull SimplePool<PhotoContents.ViewHolder> pool, @NonNull PhotoContents.State state) {
        Rect bounds = state.getBounds();

        final int childCount = getChildCount();
        if (childCount == 1) {
            View child = getChildAt(0);
            child.layout(bounds.left, bounds.top, bounds.left + child.getMeasuredWidth(), bounds.top + child.getMeasuredHeight());
        } else {
            int left = bounds.left;
            int top = bounds.top;
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == View.GONE) continue;

                int right = left + child.getMeasuredWidth();
                int bottom = top + child.getMeasuredHeight();

                child.layout(left, top, right, bottom);

                left += child.getMeasuredWidth() + itemSpace;

                if (childCount == 4) {
                    if (i == 1) {
                        //换行
                        left = bounds.left;
                        top = bottom + itemSpace;
                    }
                } else {
                    if ((i + 1) % 3 == 0) {
                        left = bounds.left;
                        top = bottom + itemSpace;
                    }
                }
            }
        }
    }

    @Override
    protected PhotoContents.ViewHolder obtainViewHolder(int position) {
        if (mLayoutState.itemCount == 1) {
            PhotoContents.ViewHolder result = mSinglePool.acquire();
            if (result == null) {
                result = createFromAdapter(position);
            }
            onPrepareViewHolder(result, position);
            return result;
        }
        return super.obtainViewHolder(position);
    }

    @Override
    public boolean onInterceptRecycle(int childCount, int position, @NonNull PhotoContents.ViewHolder holder) {
        if (childCount == 1) {
            mSinglePool.release(holder);
            return true;
        }
        return super.onInterceptRecycle(childCount, position, holder);
    }

    private static class LayoutState {
        int widthMeasureSpec;
        int heightMeasureSpec;
        int itemCount;
    }
}
