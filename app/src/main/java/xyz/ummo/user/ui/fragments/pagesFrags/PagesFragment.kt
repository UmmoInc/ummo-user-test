package xyz.ummo.user.ui.fragments.pagesFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import xyz.ummo.user.R
import xyz.ummo.user.adapters.PagesViewPagerAdapter
import xyz.ummo.user.databinding.FragmentPagesBinding

class PagesFragment : Fragment() {

    private lateinit var pagesFragmentBinding: FragmentPagesBinding
    companion object {
        fun newInstance() = PagesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        pagesFragmentBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_pages,
                container, false)

        val view = pagesFragmentBinding.root
        setupPagesTabs()

        return view
    }

    private fun setupPagesTabs() {
        val pagesAdapter = PagesViewPagerAdapter(childFragmentManager)
        pagesAdapter.addFragment(HomeAffairsFragment(), "Home Affairs")
        pagesAdapter.addFragment(TreasuryFragment(), "Revenue")
        pagesAdapter.addFragment(CommerceFragment(), "Commerce")

        pagesFragmentBinding.pagesViewPager.adapter = pagesAdapter
        pagesFragmentBinding.pagesTabLayout.setupWithViewPager(pagesFragmentBinding.pagesViewPager)


        pagesFragmentBinding.pagesTabLayout.getTabAt(0)!!
                .setIcon(R.drawable.ic_home_affairs_24)
        pagesFragmentBinding.pagesTabLayout.getTabAt(1)!!
                .setIcon(R.drawable.ic_treasury_24)
        pagesFragmentBinding.pagesTabLayout.getTabAt(2)!!
                .setIcon(R.drawable.ic_commerce_24)
    }
}