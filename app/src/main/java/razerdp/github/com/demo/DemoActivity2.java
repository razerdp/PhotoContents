package razerdp.github.com.demo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import razerdp.github.com.demo.utils.BmobUrlUtil;
import razerdp.github.com.widget.PhotoContents;
import razerdp.github.com.widget.adapter.PhotoContentsBaseAdapter;

public class DemoActivity2 extends AppCompatActivity {

    private List<String> data1;
    private List<String> data2;


    private PhotoContents contents1;
    private PhotoContentsAdapter adapter1;
    private PhotoContents contents2;
    private PhotoContentsAdapter adapter2;

    int test = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        data1 = new ArrayList<>();
        data1.add("http://bmob-cdn-14711.b0.upaiyun.com/2018/01/30/bc6e5d2a8498419db697ce222ee3b237.jpg");
        data1.add("http://bmob-cdn-14711.b0.upaiyun.com/2018/01/30/7a42e6e2be6f40be99c87c4617d99d92.jpg");
        data1.add("http://bmob-cdn-14711.b0.upaiyun.com/2018/01/30/83ecfa31bb7145e1b414fc5b1bac8e75.jpg");
        data1.add("http://bmob-cdn-14711.b0.upaiyun.com/2018/01/29/3d584428d0d14c5db6be674715d7747b.jpg");

        data2 = new ArrayList<>();
        data2.add("http://bmob-cdn-14711.b0.upaiyun.com/2018/01/19/e139783ca91944d7aa0db3382451c86b.jpg");
        data2.add("http://bmob-cdn-14711.b0.upaiyun.com/2018/01/19/5ff8baa590fc49dc8744d4e994e14a08.jpg");
        data2.add("http://bmob-cdn-14711.b0.upaiyun.com/2018/01/19/ec689fe60b6d4b54977176154d78d9e9.jpg");
//        data2.add("http://bmob-cdn-14711.b0.upaiyun.com/2018/01/19/3fb259609fe14fb284957146be3bd61c.jpg");
        initView();

    }


    private void initView() {
        contents1 = (PhotoContents) findViewById(R.id.contents1);
        contents2 = (PhotoContents) findViewById(R.id.contents2);

        adapter1 = new PhotoContentsAdapter(this, data1);
        adapter2 = new PhotoContentsAdapter(this, data2);

        contents1.setAdapter(adapter1);

        findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test++;
                if (test % 2 == 0) {
                    adapter1.updateData(data2);
                    adapter2.updateData(data1);
                } else {
                    adapter1.updateData(data1);
                    adapter2.updateData(data2);
                }

            }
        });
    }

    //=============================================================InnerAdapter
    private class PhotoContentsAdapter extends PhotoContentsBaseAdapter {


        private Context context;
        private List<String> datas;

        PhotoContentsAdapter(Context context, List<String> datas) {
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
            ImageLoadMnanger.INSTANCE.loadImage(convertView, BmobUrlUtil.getThumbImageUrl(datas.get(position), 50));
        }

        @Override
        public int getCount() {
            return datas.size();
        }

        public void updateData(List<String> datas) {
            this.datas.clear();
            this.datas.addAll(datas);
            notifyDataChanged();
        }
    }

}
