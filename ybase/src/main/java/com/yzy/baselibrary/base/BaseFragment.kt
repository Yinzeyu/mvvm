package com.yzy.baselibrary.base

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.yzy.baselibrary.R
import com.yzy.baselibrary.extention.StatusBarHelper
import com.yzy.baselibrary.extention.inflate
import com.yzy.baselibrary.extention.removeParent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.lang.reflect.ParameterizedType

/**
 *description: BaseFragment.
 *@date 2019/7/15
 *@author: yzy.
 */
abstract class BaseFragment<VM : BaseViewModel<*>> : Fragment(),
    CoroutineScope by MainScope() {
    lateinit var viewModel: VM

    //是否第一次加载
    private var isFirst: Boolean = true
    //页面基础信息
    lateinit var mActivity: BaseActivity
    lateinit var mContext: Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = requireActivity() as BaseActivity
        mContext = context
    }

    /**
     * 内容布局的ResId
     */
    protected abstract val contentLayout: Int
    lateinit var rootView: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.e("fragment", this.javaClass.name)
        val contentView = inflater.inflate(R.layout.base_fragment, container,false)
        val noteView = mContext.inflate(contentLayout)
        rootView = contentView.findViewById(R.id.contentView)
        if (contentLayout > 0) {
            rootView.addView(noteView)
        } else {
            rootView.removeParent()
        }
//        val baseStatusView = contentView.findViewById<View>(R.id.baseStatusView)
//        baseStatusView?.let {
//            it.layoutParams.height =
//                if (fillStatus()) StatusBarHelper.getStatusBarHeight(mContext) else 0
//            it.backgroundColor = statusColor()
//        }
        return contentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isBack()) {
            StatusBarHelper.setStatusBarLightMode(mActivity)
        } else {
            StatusBarHelper.setStatusBarDarkMode(mActivity)
        }
        onVisible()
        createViewModel()
        lifecycle.addObserver(viewModel)
        initView(view)
        initData()
    }

    override fun onResume() {
        super.onResume()
        onVisible()
    }

    /**
     * 是否需要懒加载
     */
    private fun onVisible() {
        if (lifecycle.currentState == Lifecycle.State.STARTED && isFirst) {
            lazyLoadData()
            isFirst = false
        }
    }

    /**
     * 懒加载
     */
    open fun lazyLoadData() {}

    /**
     * 创建 ViewModel
     */
    @Suppress("UNCHECKED_CAST")
    private fun createViewModel() {
        val type = javaClass.genericSuperclass
        if (type is ParameterizedType) {
            val tp = type.actualTypeArguments[0]
            val tClass = tp as? Class<VM> ?: BaseViewModel::class.java
            viewModel = ViewModelProvider(this, ViewModelFactory()).get(tClass) as VM
        }
    }

    /**
     * 是否屏蔽返回键
     */
    fun onBack(enabled:Boolean,onBackPressed:()->Unit){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(enabled) {
            override fun handleOnBackPressed() {
                onBackPressed.invoke()
            }
        })
    }
    /**
     * 初始化View
     */
    protected abstract fun initView(root: View?)

    /**
     * 初始化数据
     */
    protected abstract fun initData()

    open fun onFragmentResult(requestCode: Int, resultCode: Int, data: Intent?) {

    }


    open fun onRestartNavigate() {

    }

    //是否需要默认填充状态栏,默认填充为白色view
    protected open fun fillStatus(): Boolean {
        return true
    }

    protected open fun statusColor(): Int {
        return Color.TRANSPARENT
    }

    protected open fun isBack(): Boolean {
        return true
    }

    override fun onDestroyView() {
        cancel()
        super.onDestroyView()
    }


    open fun onBackPressed() {
        (mContext as BaseActivity).onBackPressed()
    }
}