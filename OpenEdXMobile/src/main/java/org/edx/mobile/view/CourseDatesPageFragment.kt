package org.edx.mobile.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import org.edx.mobile.R
import org.edx.mobile.core.EdxEnvironment
import org.edx.mobile.databinding.FragmentCourseDatesPageBinding
import org.edx.mobile.http.HttpStatus
import org.edx.mobile.http.HttpStatusException
import org.edx.mobile.http.notifications.FullScreenErrorNotification
import org.edx.mobile.interfaces.OnDateBlockListener
import org.edx.mobile.util.BrowserUtil
import org.edx.mobile.util.UiUtil
import org.edx.mobile.view.adapters.CourseDatesAdapter
import org.edx.mobile.viewModel.CourseDateViewModel
import org.edx.mobile.viewModel.ViewModelFactory
import javax.inject.Inject

class CourseDatesPageFragment : OfflineSupportBaseFragment() {

    @Inject
    private lateinit var environment: EdxEnvironment
    private lateinit var errorNotification: FullScreenErrorNotification

    private lateinit var binding: FragmentCourseDatesPageBinding
    private lateinit var viewModel: CourseDateViewModel
    private var onDateItemClick: OnDateBlockListener = object : OnDateBlockListener {
        override fun onClick(link: String) {
            BrowserUtil.open(activity, link)
        }
    }

    companion object {
        @JvmStatic
        fun makeArguments(courseId: String): Bundle {
            val courseBundle = Bundle()
            courseBundle.putString(Router.EXTRA_COURSE_ID, courseId)
            return courseBundle
        }
    }

    override fun isShowingFullScreenError(): Boolean {
        return errorNotification.isShowing
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_course_dates_page, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelFactory()).get(CourseDateViewModel::class.java)

        errorNotification = FullScreenErrorNotification(binding.swipeContainer)

        binding.swipeContainer.setOnRefreshListener {
            // Hide the progress bar as swipe layout has its own progress indicator
            binding.loadingIndicator.loadingIndicator.visibility = View.GONE
            errorNotification.hideError()
            viewModel.fetchCourseDates(courseID = getStringArgument(Router.EXTRA_COURSE_ID), isSwipeRefresh = true)
        }
        UiUtil.setSwipeRefreshLayoutColors(binding.swipeContainer)
        initObserver()
        viewModel.fetchCourseDates(courseID = getStringArgument(Router.EXTRA_COURSE_ID), isSwipeRefresh = false)
    }

    private fun initObserver() {
        viewModel.showLoader.observe(this, Observer { showLoader ->
            binding.loadingIndicator.loadingIndicator.visibility = if (showLoader) View.VISIBLE else View.GONE
        })

        viewModel.courseDates.observe(this, Observer { dates ->
            if (dates.courseDateBlocks.isNullOrEmpty()) {
                viewModel.setError(HttpStatus.NO_CONTENT, getString(R.string.course_dates_unavailable_message))
            } else {
                dates.organiseCourseDates()
                binding.rvDates.apply {
                    layoutManager = LinearLayoutManager(context)
                    adapter = CourseDatesAdapter(dates.courseDatesMap, onDateItemClick)
                }
            }
        })

        viewModel.errorMessage.observe(this, Observer { throwable ->
            if (throwable != null) {
                if (throwable is HttpStatusException) {
                    when (throwable.statusCode) {
                        HttpStatus.UNAUTHORIZED -> {
                            environment.router?.forceLogout(contextOrThrow,
                                    environment.analyticsRegistry,
                                    environment.notificationDelegate)
                            return@Observer
                        }
                    }
                }
                errorNotification.showError(contextOrThrow, throwable, -1, null)
            }
        })

        viewModel.swipeRefresh.observe(this, Observer { enableSwipeListener ->
            binding.swipeContainer.isRefreshing = enableSwipeListener
        })
    }
}
