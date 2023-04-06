package ru.maltsev.dogs.view

import android.os.Bundle
import android.view.*
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuItemCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import ru.maltsev.dogs.R
import ru.maltsev.dogs.viewmodel.ListViewModel

class ListFragment : Fragment() {

    private lateinit var viewModel: ListViewModel
    private val dogsListAdapter = DogListAdapter(arrayListOf())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createMenu(view)

        viewModel = ViewModelProviders.of(this)[ListViewModel::class.java]
        viewModel.refresh()

        val dogsList = view.findViewById<RecyclerView>(R.id.dogsList)
        val listError = view.findViewById<TextView>(R.id.listError)
        val loading = view.findViewById<ProgressBar>(R.id.loadingView)
        dogsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = dogsListAdapter
        }

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.refreshLayout)
        refreshLayout.setOnRefreshListener {
            dogsList.visibility = View.GONE
            listError.visibility = View.GONE
            loading.visibility = View.VISIBLE
            viewModel.refreshBypassCache()
            refreshLayout.isRefreshing = false
        }

        observeViewModel(view)
    }

    private fun createMenu(view: View) {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.list_menu, menu)
                val searchView = MenuItemCompat.getActionView(menu.findItem(R.id.action_search)) as SearchView
                searchView.queryHint = "Filter"
                searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (!newText.isNullOrEmpty()) {
                            viewModel.filterByDatabase("%$newText%")
                        } else {
                            viewModel.refresh()
                        }
                        return true
                    }

                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.actionSettings -> {
                        Navigation.findNavController(view)
                            .navigate(ListFragmentDirections.actionSettingsFragment())
                        true
                    }
                    R.id.action_search -> {
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModel(view: View) {
        val dogsList = view.findViewById<RecyclerView>(R.id.dogsList)
        val listError = view.findViewById<TextView>(R.id.listError)
        val loading = view.findViewById<ProgressBar>(R.id.loadingView)
        viewModel.dogs.observe(viewLifecycleOwner) { dogs ->
            dogs?.let {
                dogsList.visibility = View.VISIBLE
                dogsListAdapter.updateDogsList(dogs)
            }
        }

        viewModel.dogsLoadError.observe(viewLifecycleOwner) { isError ->
            isError?.let {
                if (it) {
                    listError.visibility = View.VISIBLE
                } else {
                    listError.visibility = View.GONE
                }
            }
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            isLoading?.let {
                if (it) {
                    loading.visibility = View.VISIBLE
                    listError.visibility = View.GONE
                    dogsList.visibility = View.GONE
                } else {
                    loading.visibility = View.GONE
                    dogsList.visibility = View.VISIBLE
                }
            }
        }
    }
}