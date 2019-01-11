package razerdp.github.com.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import razerdp.github.com.demo.baseadapter.BaseRecyclerViewAdapter;
import razerdp.github.com.demo.baseadapter.BaseRecyclerViewHolder;
import razerdp.github.com.demo.baseadapter.OnRecyclerViewItemClickListener;
import razerdp.github.com.demo.model.entity.MomentsInfo;
import razerdp.github.com.demo.model.entity.PhotoInfo;
import razerdp.github.com.demo.net.MomentsRequest;
import razerdp.github.com.demo.net.base.SimpleResponseListener;
import razerdp.github.com.demo.utils.BmobUrlUtil;
import razerdp.github.com.demo.utils.ToolUtil;
import razerdp.github.com.demo.utils.UIHelper;
import razerdp.github.com.widget.PhotoContents;
import razerdp.github.com.widget.adapter.PhotoContentsBaseAdapter;

public class DemoActivity extends AppCompatActivity implements XRecyclerView.LoadingListener {

    private XRecyclerView xrecyclerview;
    private InnerAdapter adapter;

    private static final int REQUEST_REFRESH = 0x10;
    private static final int REQUEST_LOADMORE = 0x11;

    //request
    private MomentsRequest momentsRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        momentsRequest = new MomentsRequest();
        initView();
    }

    private void initView() {
        xrecyclerview = (XRecyclerView) findViewById(R.id.xrecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xrecyclerview.setLayoutManager(layoutManager);
        xrecyclerview.setLoadingListener(this);
        adapter = new InnerAdapter(this, new ArrayList<MomentsInfo>());
        adapter.setOnRecyclerViewItemClickListener(new OnRecyclerViewItemClickListener<MomentsInfo>() {
            @Override
            public void onItemClick(View v, int position, MomentsInfo data) {
                UIHelper.ToastMessage("click item");
            }
        });
        xrecyclerview.setAdapter(adapter);
        xrecyclerview.refresh();
    }

    @Override
    public void onRefresh() {
        momentsRequest.setOnResponseListener(momentsRequestCallBack);
        momentsRequest.setRequestType(REQUEST_REFRESH);
        momentsRequest.setCurPage(0);
        momentsRequest.execute();

    }

    @Override
    public void onLoadMore() {
        momentsRequest.setOnResponseListener(momentsRequestCallBack);
        momentsRequest.setRequestType(REQUEST_LOADMORE);
        momentsRequest.execute();
    }

    private SimpleResponseListener<List<MomentsInfo>> momentsRequestCallBack = new SimpleResponseListener<List<MomentsInfo>>() {
        @Override
        public void onSuccess(List<MomentsInfo> response, int requestType) {
            switch (requestType) {
                case REQUEST_REFRESH:
                    xrecyclerview.refreshComplete();
                    if (!ToolUtil.isListEmpty(response)) {
                        adapter.updateData(response);
                    }
                    break;
                case REQUEST_LOADMORE:
                    xrecyclerview.loadMoreComplete();
                    adapter.addMore(response);
                    break;
            }
        }

        @Override
        public void onError(BmobException e, int requestType) {
            super.onError(e, requestType);
            xrecyclerview.refreshComplete();
            xrecyclerview.loadMoreComplete();
        }
    };

    //=============================================================InnerAdapter

    private static class InnerAdapter extends BaseRecyclerViewAdapter<MomentsInfo> {


        public InnerAdapter(@NonNull Context context, @NonNull List<MomentsInfo> datas) {
            super(context, datas);
        }

        @Override
        protected int getViewType(int position, @NonNull MomentsInfo data) {
            return 0;
        }

        @Override
        protected int getLayoutResId(int viewType) {
            return R.layout.item_multi_image;
        }

        @Override
        protected BaseRecyclerViewHolder getViewHolder(ViewGroup parent, View inflatedView, int viewType) {
            return new InnerViewHolder(inflatedView, viewType);
        }

        private class InnerViewHolder extends BaseRecyclerViewHolder<MomentsInfo> implements PhotoContents.OnItemClickListener {

            private PhotoContents imageContainer;
            private InnerContainerAdapter adapter;

            public InnerViewHolder(View itemView, int viewType) {
                super(itemView, viewType);
                imageContainer = (PhotoContents) itemView.findViewById(R.id.photocontents);
                imageContainer.setmOnItemClickListener(this);
            }

            @Override
            public void onBindData(MomentsInfo data, int position) {
                //因为使用的是朋友圈的模拟数据，所以也会存在图片为空的情况，所以这里判空，实际上因为是多type，所以实际应用中并不需要这样处理
                List<PhotoInfo> pics = data.getContent().getPics();
                if (ToolUtil.isListEmpty(pics)) return;
                if (adapter == null) {
                    adapter = new InnerContainerAdapter(getContext(), data.getContent().getPics());
                    imageContainer.setAdapter(adapter);
                } else {
                    adapter.updateData(data.getContent().getPics());
                }
            }

            @Override
            public void onItemClick(ImageView view, int position) {
                Log.d("onItemClick", "position  >>>  " + position);

            }

            private class InnerContainerAdapter extends PhotoContentsBaseAdapter {


                private Context context;
                private List<PhotoInfo> datas;

                InnerContainerAdapter(Context context, List<PhotoInfo> datas) {
                    this.context = context;
                    this.datas = new ArrayList<>();
                    this.datas.addAll(datas);
                }

                @Override
                public ImageView onCreateView(ImageView convertView, ViewGroup parent, int position) {
                    if (convertView == null) {
                        convertView = new ForceClickImageView(context);
                        convertView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                    return convertView;
                }

                @Override
                public void onBindData(int position, @NonNull ImageView convertView) {
                    ImageLoadMnanger.INSTANCE.loadImage(convertView, BmobUrlUtil.getThumbImageUrl(datas.get(position).getUrl(), 50));
                }

                @Override
                public int getCount() {
                    return datas.size();
                }

                public void updateData(List<PhotoInfo> datas) {
                    this.datas.clear();
                    this.datas.addAll(datas);
                    notifyDataChanged();
                }
            }
        }
    }
}
