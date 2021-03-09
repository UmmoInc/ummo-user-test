package xyz.ummo.user.ui.fragments.pagesFrags

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.fragment_home_affairs.view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONObject
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.data.entity.ProfileEntity
import xyz.ummo.user.data.entity.ServiceEntity
import xyz.ummo.user.databinding.FragmentSavedServicesBinding
import xyz.ummo.user.models.ServiceObject
import xyz.ummo.user.rvItems.ServiceItem
import xyz.ummo.user.ui.fragments.profile.ProfileViewModel
import xyz.ummo.user.ui.viewmodels.ServiceProviderViewModel
import xyz.ummo.user.ui.viewmodels.ServiceViewModel
import xyz.ummo.user.utilities.eventBusEvents.ServiceBookmarkedEvent

/**
 * A simple [Fragment] subclass.
 * Use the [SavedServicesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SavedServicesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var savedServicesBinding: FragmentSavedServicesBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter<GroupieViewHolder>

    /** ServiceProvider ViewModel && Entity Declarations **/
    private var serviceProviderViewModel: ServiceProviderViewModel? = null

    /** Service ViewModel && Entity Declarations **/
    private var serviceViewModel: ServiceViewModel? = null

    /** Shared Preferences for storing user actions **/
    private lateinit var bookmarkedPrefs: SharedPreferences
    private val mode = Activity.MODE_PRIVATE
    private val ummoUserPreferences: String = "UMMO_USER_PREFERENCES"
    private var serviceUpVoteBoolean: Boolean = false
    private var serviceDownVoteBoolean: Boolean = false
    private var serviceCommentBoolean: Boolean = false
    private var serviceBookmarked: Boolean = false
    private var savedUserActions = JSONObject()
    private lateinit var profileViewModel: ProfileViewModel
    private var profileEntity = ProfileEntity()

    private lateinit var bookmarkedServicesList: List<ServiceEntity>
    private lateinit var bookmarkedService: ServiceObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bookmarkedPrefs = this.requireActivity().getSharedPreferences(ummoUserPreferences, mode)

        groupAdapter = GroupAdapter()

        /** Initializing ViewModels: ServiceProvider && Services **/
        serviceProviderViewModel = ViewModelProvider(this)
                .get(ServiceProviderViewModel::class.java)

        serviceViewModel = ViewModelProvider(this)
                .get(ServiceViewModel::class.java)

        profileViewModel = ViewModelProvider(context as FragmentActivity)
                .get(ProfileViewModel::class.java)

        /** [BookmarkEvent-1] Register for EventBus events **/
        EventBus.getDefault().register(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        savedServicesBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_saved_services,
                container, false)

        val view = savedServicesBinding.root
        val layoutManager = view.services_recycler_view.layoutManager

        recyclerView = view.services_recycler_view
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = groupAdapter

        getBookmarkedServicesFromSharedPrefs()

//        getBookmarkedServices()

        return view
    }

    private fun getBookmarkedServices() {
        var serviceId: String
        var serviceName: String
        var serviceDescription: String
        var serviceEligibility: String
        var serviceCentres: ArrayList<String>
        var delegatable: Boolean
        var serviceCost: String
        var serviceDocuments: ArrayList<String>
        var serviceDuration: String
        var approvalCount: Int
        var disapprovalCount: Int
        var serviceComments: ArrayList<String>
        var commentCount: Int
        var shareCount: Int
        var viewCount: Int
        var serviceProvider: String

        bookmarkedServicesList = serviceViewModel?.getBookmarkedServiceList()!!

        Timber.e("BOOKMARKED LIST SIZE [0]-> ${bookmarkedServicesList.size}")

        if (bookmarkedServicesList.isEmpty()) {
            savedServicesBinding.bookmarkedServicesRelativeLayout.visibility = View.GONE
            savedServicesBinding.noBookmarkedServicesRelativeLayout.visibility = View.VISIBLE
        } else {
            savedServicesBinding.noBookmarkedServicesRelativeLayout.visibility = View.GONE
            savedServicesBinding.bookmarkedServicesRelativeLayout.visibility = View.VISIBLE
        }

        for (i in bookmarkedServicesList.indices) {
            Timber.e("BOOKMARKED SERVICES -> ${bookmarkedServicesList[i].serviceName}")
            Timber.e("BOOKMARKED LIST SIZE [1]-> ${bookmarkedServicesList.size}")

            serviceId = bookmarkedServicesList[i].serviceId!! //0
            serviceName = bookmarkedServicesList[i].serviceName!! //1
            serviceDescription = bookmarkedServicesList[i].serviceDescription!! //2
            serviceEligibility = bookmarkedServicesList[i].serviceEligibility!! //3
            serviceCentres = bookmarkedServicesList[i].serviceCentres!! //4
            delegatable = bookmarkedServicesList[i].delegatable!! //5
//            serviceCost = bookmarkedServicesList[i].serviceCost!! //6
            serviceDocuments = bookmarkedServicesList[i].serviceDocuments!! //7
            serviceDuration = bookmarkedServicesList[i].serviceDuration!! //8
            approvalCount = bookmarkedServicesList[i].usefulCount!! //9
            disapprovalCount = bookmarkedServicesList[i].notUsefulCount!! //10
            serviceComments = bookmarkedServicesList[i].serviceComments!!
            commentCount = bookmarkedServicesList[i].commentCount!! //11
            shareCount = bookmarkedServicesList[i].serviceShares!! //12
            viewCount = bookmarkedServicesList[i].serviceViews!! //13
            serviceProvider = bookmarkedServicesList[i].serviceProvider!! //14

            /*bookmarkedService = ServiceObject(serviceId, serviceName, serviceDescription,
                    serviceEligibility, serviceCentres, delegatable, serviceCost,
                    serviceDocuments, serviceDuration, approvalCount, disapprovalCount,
                    serviceComments, commentCount, shareCount, viewCount, serviceProvider)*/

            /**1. capturing $UP-VOTE, $DOWN-VOTE && $COMMENTED-ON values from RoomDB,
             * using the $serviceId
             * 2. wrapping those values in a JSON Object
             * 3. pushing that $savedUserActions JSON Object to $ServiceItem, via gAdapter **/
            serviceUpVoteBoolean = bookmarkedPrefs
                    .getBoolean("UP-VOTE-${bookmarkedServicesList[i].serviceId}", false)

            serviceDownVoteBoolean = bookmarkedPrefs
                    .getBoolean("DOWN-VOTE-${bookmarkedServicesList[i].serviceId}", false)

            serviceCommentBoolean = bookmarkedPrefs
                    .getBoolean("COMMENTED-ON-${bookmarkedServicesList[i].serviceId}", false)

            serviceBookmarked = bookmarkedPrefs
                    .getBoolean("BOOKMARKED-${bookmarkedServicesList[i].serviceId}", false)

            savedUserActions
                    .put("UP-VOTE", serviceUpVoteBoolean)
                    .put("DOWN-VOTE", serviceDownVoteBoolean)
                    .put("COMMENTED-ON", serviceCommentBoolean)
                    .put("BOOKMARKED", serviceBookmarked)

            Timber.e("SAVED USER ACTIONS -> ${savedUserActions.getString("BOOKMARKED")}")

            groupAdapter.add(ServiceItem(bookmarkedService, context, savedUserActions))
        }

        //TODO: stashing this method for now. Let's try retrieve bookmarked services via sharedPrefs
        /*object : GetBookmarks(context!!, profileEntity.profileContact.toString()) {
            override fun done(data: ByteArray, code: Number) {
                if (code == 200) {
                    Timber.e("BOOKMARKED SERVICES -> ${String(data)}")
                } else {
                    Timber.e("SERVICE-BOOKMARK-ERROR-> $code")
                }
            }
        }*/
    }

    private fun getBookmarkedServicesFromSharedPrefs() {

        val services = serviceViewModel?.getServicesList()
        val serviceEntity = ServiceEntity()
        var isBookmarkedInPrefs: Boolean
        var isBookmarkedInRoom: Boolean

        for (i in services?.indices!!) {
            //Timber.e("SERVICE-IDs -> ${services[i].serviceId}")
            serviceEntity.serviceId = services[i].serviceId //0
            serviceEntity.serviceName = services[i].serviceName //1
            serviceEntity.serviceDescription = services[i].serviceDescription //2
            serviceEntity.serviceEligibility = services[i].serviceEligibility //3
            serviceEntity.serviceCentres = services[i].serviceCentres //4
            serviceEntity.delegatable = services[i].delegatable //5
//            serviceEntity.serviceCost = services[i].serviceCost //6
            serviceEntity.serviceDocuments = services[i].serviceDocuments //7
            serviceEntity.serviceDuration = services[i].serviceDuration //8
            serviceEntity.notUsefulCount = services[i].notUsefulCount //9
            serviceEntity.usefulCount = services[i].usefulCount //10
            serviceEntity.serviceComments = services[i].serviceComments //11
            serviceEntity.commentCount = services[i].commentCount //12
            serviceEntity.serviceShares = services[i].serviceShares //13
            serviceEntity.serviceViews = services[i].serviceViews //14
            serviceEntity.serviceProvider = services[i].serviceProvider //15
            isBookmarkedInRoom = services[i].bookmarked!!

            isBookmarkedInPrefs = bookmarkedPrefs.getBoolean("BOOKMARKED-${services[i].serviceId}", false)

            if (isBookmarkedInPrefs && !isBookmarkedInRoom) {

                serviceEntity.bookmarked = true //16
                Timber.e("UPDATING SERVICE (VIA PREF) -> ${serviceEntity.serviceId}")

               serviceViewModel?.updateService(serviceEntity)

            }
        }
    }

    @Subscribe
    fun onServiceBookmarkedEvent(serviceBookmarkedEvent: ServiceBookmarkedEvent) {
        Timber.e("SERVICE-BOOK-MARKED-EVENT -> ${serviceBookmarkedEvent.serviceName}")
        Timber.e("SERVICE-BOOK-MARKED-EVENT -> ${serviceBookmarkedEvent.serviceBookmarked}")

//        getBookmarkedServices()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SavedServicesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                SavedServicesFragment().apply {

                }
    }
}