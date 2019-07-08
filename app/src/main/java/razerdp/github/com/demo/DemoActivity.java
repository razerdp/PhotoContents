package razerdp.github.com.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcodecraeer.xrecyclerview.XRecyclerView;

import java.util.ArrayList;
import java.util.List;

import razerdp.github.com.demo.baseadapter.BaseRecyclerViewAdapter;
import razerdp.github.com.demo.baseadapter.BaseRecyclerViewHolder;
import razerdp.github.com.demo.utils.RandomUtil;
import razerdp.github.com.demo.utils.ToolUtil;
import razerdp.github.com.demo.utils.rx.RxCall;
import razerdp.github.com.demo.utils.rx.RxHelper;
import razerdp.github.com.widget.PhotoContents;
import razerdp.github.com.widget.layoutmanager.NineGridLayoutManager;

public class DemoActivity extends AppCompatActivity implements XRecyclerView.LoadingListener {

    private XRecyclerView xrecyclerview;
    private InnerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        xrecyclerview = findViewById(R.id.xrecyclerview);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        xrecyclerview.setLayoutManager(layoutManager);
        xrecyclerview.setLoadingListener(this);
        xrecyclerview.setItemAnimator(null);
        adapter = new InnerAdapter(this, new ArrayList<List<String>>());
        xrecyclerview.setAdapter(adapter);
        xrecyclerview.refresh();
    }

    @Override
    public void onRefresh() {
        RxHelper.delay(RandomUtil.randomInt(200, 1500), new RxCall<Long>() {
            @Override
            public void onCall(Long data) {
                adapter.updateData(randomPhoto(RandomUtil.randomInt(15, 20)));
                xrecyclerview.refreshComplete();
            }
        });
    }

    @Override
    public void onLoadMore() {
        RxHelper.delay(RandomUtil.randomInt(200, 1500), new RxCall<Long>() {
            @Override
            public void onCall(Long data) {
                adapter.addMore(randomPhoto(RandomUtil.randomInt(15, 20)));
                xrecyclerview.refreshComplete();
            }
        });
    }

    public List<List<String>> randomPhoto(int count) {
        List<List<String>> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int photoCount = RandomUtil.randomInt(0, 9);
            List<String> content = new ArrayList<>();
            if (photoCount <= 0) {
                result.add(content);
            } else {
                for (int j = 0; j < photoCount; j++) {
                    content.add(TestServerData.getPicUrl());
                }
                result.add(content);
            }
        }
        return result;
    }

    //=============================================================InnerAdapter

    private static class InnerAdapter extends BaseRecyclerViewAdapter<List<String>> {


        public InnerAdapter(@NonNull Context context, @NonNull List<List<String>> datas) {
            super(context, datas);
        }

        @Override
        protected int getViewType(int position, @NonNull List<String> data) {
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

        private class InnerViewHolder extends BaseRecyclerViewHolder<List<String>> {

            private PhotoContents imageContainer;
            private InnerContainerAdapter adapter;
            private TextView mNoPhoto;

            public InnerViewHolder(View itemView, int viewType) {
                super(itemView, viewType);
                imageContainer = itemView.findViewById(R.id.photocontents);
                imageContainer.setLayoutManager(new NineGridLayoutManager(8));
                mNoPhoto = itemView.findViewById(R.id.tv_no_photo);
            }

            @Override
            public void onBindData(List<String> data, int position) {
                mNoPhoto.setVisibility(ToolUtil.isListEmpty(data) ? View.VISIBLE : View.GONE);
                if (adapter == null) {
                    adapter = new InnerContainerAdapter(getContext(), data);
                    imageContainer.setAdapter(adapter);
                } else {
                    adapter.updateData(data);
                }
            }

            private class InnerContainerAdapter extends PhotoContents.Adapter<ViewHolder> {

                private List<String> datas;

                InnerContainerAdapter(Context context, List<String> datas) {
                    this.datas = new ArrayList<>();
                    this.datas.addAll(datas);
                }


                public void updateData(List<String> datas) {
                    this.datas.clear();
                    this.datas.addAll(datas);
                    notifyDataSetChanged();
                }

                public void addMore(List<String> datas) {
                    this.datas.addAll(datas);
                    notifyDataSetChanged();
                }

                @Override
                public int getItemCount() {
                    return datas.size();
                }

                @Override
                public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_img, parent, false));
                }

                @Override
                public void onBindViewHolder(ViewHolder viewHolder, int position) {
                    if (getItemCount() == 1) {
                        viewHolder.iv.setAdjustViewBounds(true);
                        viewHolder.iv.setScaleType(ImageView.ScaleType.FIT_START);
                    } else {
                        viewHolder.iv.setAdjustViewBounds(false);
                        viewHolder.iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                    ImageLoadManager.INSTANCE.loadImage(viewHolder.iv, datas.get(position));
                }
            }


            class ViewHolder extends PhotoContents.ViewHolder {
                ImageView iv;

                ViewHolder(View rootView) {
                    super(rootView);
                    iv = findViewById(R.id.iv_img);
                }
            }
        }
    }
}
