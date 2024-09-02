package com.example.unsplashgallery.ui.gallery

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import com.example.unsplashgallery.R
import com.example.unsplashgallery.data.UnsplashPhotoModel
import com.example.unsplashgallery.databinding.FragmentGalleryBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager

@AndroidEntryPoint
class GalleryFragment : Fragment(R.layout.fragment_gallery),
    GalleryAdapter.OnItemClickListener {

    private val viewModel by viewModels<GalleryViewModel>()

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private var isLinearLayoutManager = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentGalleryBinding.bind(view)

        val adapter = GalleryAdapter(this)

        binding.apply {
            recyclerView.setHasFixedSize(true)
            recyclerView.itemAnimator = null
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            buttonRetry.setOnClickListener { adapter.retry() }
            toggleButton.setOnClickListener { toggleLayout() }
        }

        viewModel.photos.observe(viewLifecycleOwner) {
            adapter.submitData(viewLifecycleOwner.lifecycle, it)
        }

        adapter.addLoadStateListener { loadState ->
            binding.apply {
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                textViewEmpty.isVisible = loadState.source.refresh is LoadState.Error
                buttonRetry.isVisible = loadState.source.refresh is LoadState.Error

                // empty view
                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    recyclerView.isVisible = false
                    textViewEmpty.isVisible = true
                } else {
                    textViewEmpty.isVisible = false
                }
            }
        }

        setHasOptionsMenu(true)
    }

    override fun onItemClick(photo: UnsplashPhotoModel) {
        val action = GalleryFragmentDirections.actionGalleryFragmentToDetailsFragment(photo)
        findNavController().navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.menu_gallery, menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query != null) {
                    binding.recyclerView.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun toggleLayout() {
        if (isLinearLayoutManager) {
            // Switch to GridLayoutManager
            binding.toggleButton.setImageResource(R.drawable.ic_baseline_list_24)
            binding.recyclerView.layoutManager =
                GridLayoutManager(context, 2) // Adjust span count as needed
        } else {
            // Switch to LinearLayoutManager
            binding.toggleButton.setImageResource(R.drawable.ic_baseline_grid_view_24)
            binding.recyclerView.layoutManager = LinearLayoutManager(context)
        }
        isLinearLayoutManager = !isLinearLayoutManager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}