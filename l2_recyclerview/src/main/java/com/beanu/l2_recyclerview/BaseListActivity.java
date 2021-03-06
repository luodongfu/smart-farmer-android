package com.beanu.l2_recyclerview;

import android.os.Bundle;

import com.beanu.arad.base.ToolBarActivity;
import com.beanu.arad.support.recyclerview.adapter.EndlessRecyclerOnScrollListener;
import com.beanu.arad.support.recyclerview.adapter.LoadMoreAdapterWrapper;
import com.beanu.arad.support.recyclerview.loadmore.ILoadMoreModel;
import com.beanu.arad.support.recyclerview.loadmore.LoadMorePresenterImpl;
import com.beanu.l2_recycleview.R;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.srain.cube.views.ptr.PtrClassicDefaultHeader;
import in.srain.cube.views.ptr.PtrFrameLayout;

/**
 * @author lizhi
 * @date 2017/11/1.
 */

public abstract class BaseListActivity<P extends LoadMorePresenterImpl, M extends ILoadMoreModel> extends ToolBarActivity<P, M> implements LoadMoreAdapterWrapper.OnClickRetryListener {
    private RecyclerView.Adapter<?> adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayMap<String, Object> params;
    private RecyclerView recyclerView;
    private PtrFrameLayout refreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        initPtr();
        initList();
        mPresenter.initLoadDataParams(getParams());

        getRefreshLayout().post(new Runnable() {
            @Override
            public void run() {
                getRefreshLayout().autoRefresh();
            }
        });
    }

    protected int getLayoutId() {
        return R.layout.activity_recycleview_simplest;
    }

    public RecyclerView.Adapter<?> getAdapter() {
        if (adapter == null) {
            adapter = provideAdapter();
        }
        return adapter;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        if (layoutManager == null) {
            layoutManager = provideLayoutManager();
        }
        return layoutManager;
    }

    public ArrayMap<String, Object> getParams() {
        if (params == null) {
            params = provideParams();
        }
        return params;
    }

    public RecyclerView getRecyclerView() {
        if (recyclerView == null) {
            recyclerView = findRecyclerView();
        }
        return recyclerView;
    }

    public PtrFrameLayout getRefreshLayout() {
        if (refreshLayout == null) {
            refreshLayout = findRefreshLayout();
        }
        return refreshLayout;
    }

    protected RecyclerView findRecyclerView() {
        return findViewById(R.id.recycle_view);
    }

    protected PtrFrameLayout findRefreshLayout() {
        return findViewById(R.id.arad_content);
    }

    protected void initPtr() {
        PtrClassicDefaultHeader header = new PtrClassicDefaultHeader(this);
        getRefreshLayout().setHeaderView(header);
        getRefreshLayout().addPtrUIHandler(header);
        getRefreshLayout().setPtrHandler(new RecyclerViewPtrHandler(getRecyclerView()) {
            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                mPresenter.loadDataFirst();
            }
        });
    }

    protected void initList() {
        getRecyclerView().setLayoutManager(getLayoutManager());
        getRecyclerView().setAdapter(new LoadMoreAdapterWrapper(this, getAdapter(), mPresenter, this));
        getRecyclerView().addOnScrollListener(new EndlessRecyclerOnScrollListener(getLayoutManager(), mPresenter) {
            @Override
            public void onLoadMore() {
                mPresenter.loadDataNext();
            }
        });
    }

    protected abstract RecyclerView.Adapter<?> provideAdapter();

    protected RecyclerView.LayoutManager provideLayoutManager() {
        return new LinearLayoutManager(this);
    }

    protected ArrayMap<String, Object> provideParams() {
        return new ArrayMap<>();
    }


    @Override
    public void initTopBar(QMUITopBarLayout topBarLayout) {

        topBarLayout.setTitle("列表");
        topBarLayout.addLeftBackImageButton().setOnClickListener(view -> {
            onBackPressed();
        });
    }

    @Override
    public void contentLoadingComplete() {
        super.contentLoadingComplete();
        getRefreshLayout().refreshComplete();
    }

    @Override
    public void contentLoadingError() {
        super.contentLoadingError();
        getRefreshLayout().refreshComplete();
    }

    @Override
    public void contentLoadingEmpty() {
        super.contentLoadingEmpty();
        getRefreshLayout().refreshComplete();
    }

    @Override
    public void onClickRetry() {
        if (mPresenter != null) {
            mPresenter.loadDataNext();
            RecyclerView.Adapter adapter = getRecyclerView().getAdapter();
            int count = adapter.getItemCount();
            if (count != 0) {
                adapter.notifyItemChanged(count - 1);
            }
        }
    }
}
