package xyz.ummo.user

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import xyz.ummo.user.adapters.ProvidersAdapter
import xyz.ummo.user.delegate.Provider
import xyz.ummo.user.delegate.PublicService
import xyz.ummo.user.delegate.PublicServiceData

class Home : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private  var providers: ArrayList<PublicServiceData> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        viewManager = LinearLayoutManager(this)
        viewAdapter = ProvidersAdapter(providers)

        recyclerView = findViewById<RecyclerView>(R.id.my_recycler_view).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }

        getProviders()
    }

    fun getProviders() {
        object : PublicService() {
            override fun done(data: List<PublicServiceData>, code: Number) {
                Log.e("DONEWITH PROVIDES","$code")
                data.forEach {
                    providers.add(it)
                }
                Log.e("DATA Length","${providers.size}")
                viewAdapter.notifyDataSetChanged()
            }
        }

    }
}
