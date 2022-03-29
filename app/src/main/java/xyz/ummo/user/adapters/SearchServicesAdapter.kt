package xyz.ummo.user.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber
import xyz.ummo.user.databinding.ServiceSliceBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.ui.detailedService.DetailedServiceActivity
import xyz.ummo.user.utilities.SERVICE_OBJECT
import java.io.Serializable
import java.util.*

class SearchServicesAdapter(private var allServicesArrayList: ArrayList<ServiceObject>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var fullServiceList = ArrayList<ServiceObject>()
    var servicesList = ArrayList<ServiceObject>()

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
                }
                filterResults.values = filteredServiceObjectsList
                filterResults.count = filteredServiceObjectsList.size
                Timber.e("SERVICE FILTER LIST 2 -> ${filterResults.values}")
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, filterResults: FilterResults?) {
                fullServiceList.clear()

                if (filterResults!!.values != null) {
                    fullServiceList.addAll(filterResults.values as ArrayList<ServiceObject>)
                    notifyDataSetChanged()
                } else {
                    Timber.e("SERVICE FILTER 3 NULL")
                }
            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return super.convertResultToString(resultValue)
            }
        }
    }

    /*override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                val charSearch = constraint.toString()

                if (charSearch.isEmpty()) {
                    fullServiceList = allServicesArrayList!!
                    Timber.e("EMPTY SEARCH -> $fullServiceList")
                } else {
                    for (service in allServicesArrayList!!) {
                        if (service.serviceName.toLowerCase(Locale.ROOT).contains(
                                charSearch.toLowerCase(
                                    Locale.ROOT
                                )
                            )
                        ) {
                            Timber.e("FOUND SERVICE OBJECT -> $service")
                            resultsList.add(service)
                            Timber.e("FOUND SERVICE LIST -> $resultsList")
//                            Timber.e("FOUND SERVICE (Char Search) -> $charSearch")
//                            Timber.e("FOUND SERVICE (Name) -> ${service.serviceName}")
                        }
                    }
                    *//*fullServiceList.clear()
                    fullServiceList.addAll(resultsList)*//*
                    Timber.e("SERVICE FILTER LIST 1 -> $resultsList")
                }
                filterResults.values = resultsList
                filterResults.count = resultsList.size
                Timber.e("SERVICE FILTER LIST 2 -> ${filterResults.values}")

                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
//                fullServiceList.clear()
//                fullServiceList = results?.values as ArrayList<ServiceObject>
                fullServiceList.clear()

                if (results!!.values != null) {
                    fullServiceList.addAll(results.values as ArrayList<ServiceObject>)
                    Timber.e("SERVICE FILTER LIST 3 -> $fullServiceList")
                    notifyDataSetChanged()
                } else {
                    Timber.e("SERVICE FILTER LIST 3 NULL")
                }

            }

            override fun convertResultToString(resultValue: Any?): CharSequence {
                return super.convertResultToString(resultValue)
            }
        }
    }*/

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