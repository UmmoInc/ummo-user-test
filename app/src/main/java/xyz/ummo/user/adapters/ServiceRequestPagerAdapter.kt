package xyz.ummo.user.adapters

import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.viewpager.widget.PagerAdapter
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.models.ServiceObject

class ServiceRequestPagerAdapter(private val views: ArrayList<View>, private val context: Context, private var serviceObject: ServiceObject) : PagerAdapter() {
//    private var layouts: IntArray? = null
    private var layoutInflater: LayoutInflater? = null
    private lateinit var serviceCentreRadioGroup: RadioGroup
    var serviceCentreRadioButton: RadioButton? = null
    var serviceCentresList: ArrayList<String>? = null
    private var serviceSpec = ""
    private var specCost = ""
    private var chosenServiceCentre = ""

    override fun getCount(): Int {
        return views.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return super.getItemPosition(`object`)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val view = views[position]
        container.addView(view)
        return view
        /*layoutInflater = LayoutInflater.from(context)
                .context.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        var resourceId: Int
        var viewItem: View

        when (position) {
            0 -> {
                resourceId = R.layout.service_centre_picker_slide
                viewItem = layoutInflater!!.inflate(resourceId, null)
                serviceCentreRadioGroup = viewItem.findViewById(R.id.service_centre_radio_group_dialog_fragment)

            }
            1 -> {
                resourceId = R.layout.service_cost_picker_slide
            }
            2 -> {
                resourceId = R.layout.confirm_delegation_slide
            }
        }

//        val view = layoutInflater!!.inflate(layouts!![position], container, false)

        container.addView(view)
        return view*/
    }

    private fun initServiceCentres(): View? {
        /** Parsing Service Centres into Radio-Group **/
        serviceCentresList = ArrayList(serviceObject.serviceCentres)

        if (serviceCentresList!!.isNotEmpty()) {

            Timber.e("SERVICE CENTRES-LIST -> $serviceCentresList")
//            serviceCentreRadioGroup.removeAllViews()
            for (i in serviceCentresList!!.indices) {
                serviceCentreRadioButton = RadioButton(context)
                serviceCentreRadioButton!!.id = i
                serviceCentreRadioButton!!.text = serviceCentresList!![i].replace("\"\"", "")
                serviceCentreRadioButton!!.textSize = 14F
                Timber.e("SERVICE CENTRES-LIST [$i] -> ${serviceCentresList!![i]}")
                Timber.e("SERVICE CENTRES-RADIO [$i] -> ${serviceCentreRadioButton!!.text}")

                /** Setting RadioButton color state-list **/
                if (Build.VERSION.SDK_INT >= 21) {
                    val colorStateList = ColorStateList(arrayOf(intArrayOf(-android.R.attr.state_enabled),
                            intArrayOf(android.R.attr.state_enabled)), intArrayOf(
                            Color.GRAY//disabled
                            , context.resources.getColor(R.color.ummo_1) //enabled
                    ))
                    serviceCentreRadioButton!!.buttonTintList = colorStateList
                }
                serviceCentreRadioGroup.addView(serviceCentreRadioButton)
                serviceCentreRadioGroup.setOnCheckedChangeListener { radioGroup, checkedId ->

                    val checkedBox = radioGroup.findViewById<RadioButton>(checkedId)
                    chosenServiceCentre = checkedBox.text.toString()
                    Timber.e("CHECKED BOX -> $chosenServiceCentre")
                }

//                container.addView(serviceCentreRadioGroup)
            }
        } else {
            Timber.e("onCreate: docsList is EMPTY!")
            return null
        }
        return serviceCentreRadioGroup
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }

    /*init {
        layouts = intArrayOf(R.layout.service_centre_picker_slide,
                R.layout.service_cost_picker_slide,
                R.layout.confirm_delegation_slide)
    }*/
}