package com.androiddevs.mvvmnewsapp.ui.fragments
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.mvvmnewsapp.R
import com.androiddevs.mvvmnewsapp.adapters.NewsAdapter
import com.androiddevs.mvvmnewsapp.util.Constants.Companion.SEARCH_NEWS_DELAY
import com.androiddevs.mvvmnewsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment: FragmentWithNewsViewModel(R.layout.fragment_search_news) {

    lateinit var newsAdapter: NewsAdapter
    private val TAG = "SearchNewsFragment"


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }

            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }

        var job: Job? = null

        etSearch.addTextChangedListener {
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_NEWS_DELAY)
                it?.let {
                    if (it.toString().isNotEmpty()) {
                        viewModel.searchNews(it.toString())
                    }
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is Resource.Success ->  {
                    hideProgressBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles)
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    Log.d(TAG, response.message!!)
                }
                is Resource.Loading -> showProgressBar()
            }
        })
    }



    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun hideProgressBar() {
        paginationProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        paginationProgressBar.visibility = View.VISIBLE
    }
}