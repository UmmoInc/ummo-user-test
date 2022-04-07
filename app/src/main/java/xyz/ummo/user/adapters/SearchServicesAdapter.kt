package xyz.ummo.user.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import xyz.ummo.user.databinding.ServiceSliceBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity
import xyz.ummo.user.utilities.SERVICE_OBJECT
import xyz.ummo.user.utilities.eventBusEvents.SearchResultsEvent
import java.io.Serializable
import java.util.*

class SearchServicesAdapter(private var allServicesArrayList: ArrayList<ServiceObject>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var fullServiceList = ArrayList<ServiceObject>()
    var servicesList = ArrayList<ServiceObject>()
    val searchResultsEvent = SearchResultsEvent()

    private lateinit var mContext: Context

    class ServiceHolder(var viewBinding: ServiceSliceBinding) :
        RecyclerView.ViewHolder(viewBinding.root)

    init {
        fullServiceList = allServicesArrayList!!
        servicesList = fullServiceList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ServiceSliceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val serviceHolder = ServiceHolder(binding)
        mContext = parent.context
        return serviceHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val serviceViewHolder = holder as ServiceHolder
        serviceViewHolder.viewBinding.serviceTitleTextViewSlice.text =
            fullServiceList[position].serviceName
        serviceViewHolder.viewBinding.serviceDescriptionTextViewSlice.text =
            fullServiceList[position].serviceDescription

        holder.itemView.setOnClickListener {
            showServiceDetails(position)
        }

        serviceViewHolder.viewBinding.openServiceImageSlice.setOnClickListener {
            showServiceDetails(position)
        }
    }

    override fun getItemCount(): Int {
        return fullServiceList.size
    }

    fun clearAdapter() {
        fullServiceList.clear()
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                val filteredServiceObjectsList = ArrayList<ServiceObject>()
                val charSearch = constraint.toString()

                if (charSearch.isEmpty()) {
//                    suggestions.addAll(fullServiceList)
                    filteredServiceObjectsList.addAll(servicesList)
                } else {

                    val filterPattern = charSearch.toLowerCase(Locale.ROOT).trim()
                    Timber.e("SERVICE FILTER LIST 0 -> $fullServiceList")

                    for (service in servicesList) {
//                        if (service.serviceName.toLowerCase(Locale.ROOT).contains(filterPattern))
                        if (service.serviceName.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(
                                    Locale.ROOT
                                )
                            )
                        ) {
                            filteredServiceObjectsList.add(service)
                            Timber.e("SERVICE FILTER LIST 1 -> $servicesList")
                        } else {
                            Timber.e("SERVICE FILTER LIST 1 -> NOT FOUND")
                        }
                    }
                    /** Publishing the searched service name to AllServices **/
                    searchResultsEvent.searchedService = filterPattern
                    EventBus.getDefault().post(searchResultsEvent)
                }
                filterResults.values = filteredServiceObjectsList
                filterResults.count = filteredServiceObjectsList.size
                Timber.e("SERVICE FILTER LIST 2 -> ${filterResults.values}")

                val filterResultsValues = filterResults.values as ArrayList<ServiceObject>

                for (i in 0 until filterResultsValues.size) {
                    Timber.e("SERVICE FILTER VALUE 1 -> $filterResultsValues")

                    if (filterResultsValues[i].serviceName == filterResultsValues[i + 1].serviceName) {
                        filterResultsValues.removeAt(i)
                    }
                    Timber.e("SERVICE FILTER VALUE 2 -> $filterResultsValues")
                }

                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                fullServiceList.clear()

                if (filterResults!!.values != null) {
                    fullServiceList.addAll(filterResults.values as ArrayList<ServiceObject>)
                    Timber.e("SERVICE FILTER LIST 3 -> $fullServiceList")

                    if (fullServiceList.size != 0) {
                        notifyDataSetChanged()
                        searchResultsEvent.searchResultsFound = true
                        EventBus.getDefault().post(searchResultsEvent)
                    } else {
                        searchResultsEvent.searchResultsFound = false
                        EventBus.getDefault().post(searchResultsEvent)
                    }
                } else {
                    Timber.e("SERVICE FILTER 3 NULL")
                }
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return super.convertResultToString(resultValue)
            }
        }
    }

    /** When a service is tapped on, it should expand to a detailed view with more info. **/
    private fun showServiceDetails(pos: Int) {
        val intent = Intent(mContext, DetailedServiceActivity::class.java)

        /** Passing Service to [DetailedServiceActivity] via [Serializable] object **/
        intent.putExtra(SERVICE_OBJECT, allServicesArrayList?.get(pos) as Serializable)
//        intent.putExtra(SERVICE_IMAGE, )
        Timber.e("SHOWING SERVICE DETAILS -> ${allServicesArrayList!![pos]}")
//        serviceViewModel.addService(serviceEntity)
        mContext.startActivity(intent)
    }
}