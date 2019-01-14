package razerdp.github.com.widget.adapter;


import android.database.Observable;
import android.view.View;
import android.view.ViewGroup;

import razerdp.github.com.widget.adapter.observer.TypeAdapterDataObserver;

/**
 * Created by 大灯泡 on 2019/1/14.
 */
public abstract class TypeLayoutAdapter<VH extends TypeLayoutAdapter.ViewHolder> {

    private final AdapterDataObservable mObservable = new AdapterDataObservable();

    public final VH createViewHolder(ViewGroup parent, int viewType, int position) {
        VH holder = onCreateViewHolder(parent, viewType, position);
        holder.viewType = viewType;
        if (holder.viewType == 0 && getItemCount() == 1) {
            holder.viewType = ViewHolder.TYPE_SINGLE;
        }
        return holder;
    }

    public final void bindViewHolder(VH holder, int position) {
        onBindViewHolder(holder, holder.getViewType(), position);
    }

    public abstract int getItemCount();

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType, int position);

    public abstract void onBindViewHolder(VH holder, int viewType, int position);

    public int getItemViewType(int position) {
        return 0;
    }

    public int getMaxColumns() {
        return 3;
    }

    public final void cacheViewHolder(VH holder) {
        holder.isCached = true;
        onViewHolderCached(holder);
    }

    public void onViewHolderCached(VH holder) {

    }

    public final void resumeViewHolder(VH holder) {
        holder.isCached = false;
        onViewHolderResume(holder);
    }

    public void onViewHolderResume(VH holder) {

    }

    public void registerAdapterDataObserver(TypeAdapterDataObserver observer) {
        mObservable.registerObserver(observer);
    }

    public void unregisterAdapterDataObserver(TypeAdapterDataObserver observer) {
        mObservable.unregisterObserver(observer);
    }

    public final void notifyDataSetChanged() {
        mObservable.notifyChanged();
    }

    public final void notifyItemChange(int position) {
        mObservable.notifyItemChange(position);
    }

    public void notifyItemRangeChange(int positionStart, int itemCount) {
        mObservable.notifyItemRangeChange(positionStart, itemCount);
    }


    public static abstract class ViewHolder {
        public static final int TYPE_SINGLE = -1;
        public final View itemView;
        boolean isCached;
        int viewType;
        int mPosition = -1;

        public ViewHolder(View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }

        public boolean isCached() {
            return isCached;
        }

        public final int getViewType() {
            return viewType;
        }

    }


    static final class AdapterDataObservable extends Observable<TypeAdapterDataObserver> {
        public boolean hasObservers() {
            return !mObservers.isEmpty();
        }

        public void notifyChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }

        public void notifyItemChange(int position) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemChanged(position);
            }
        }

        public void notifyItemRangeChange(int positionStart, int itemCount) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeChanged(positionStart, itemCount);
            }
        }
    }
}
