package xyz.ummo.user.rvItems

import android.annotation.SuppressLint
import android.content.Context
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.service_comment.view.*
import timber.log.Timber
import xyz.ummo.user.R
import xyz.ummo.user.models.ServiceCommentObject
import java.text.SimpleDateFormat
import java.util.*

class ServiceCommentItem(
    private val serviceCommentObject: ServiceCommentObject,
    val context: Context?
) : Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.service_comment
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val commentDate = convertDate()
        val formattedDate = commentDate.toString().subSequence(0, 16)
        val date = formattedDate.subSequence(0, 10)
        val time = formattedDate.subSequence(11, 16)

        viewHolder.itemView.user_name_text_view.text = serviceCommentObject.userName
        viewHolder.itemView.comment_datetime_text_view.text = "$date \u2022 $time"
        viewHolder.itemView.service_comment_text_view.text = serviceCommentObject.serviceComment
    }

    @SuppressLint("SimpleDateFormat")
    private fun convertDate(): Date {
        val date = serviceCommentObject.commentDateTime
        val pattern = "dd/M/yyy hh:mm:ss"
        val dateVal = SimpleDateFormat(pattern).parse(date)
        val calendar = Calendar.getInstance()
        calendar.time = dateVal!!

        Timber.e("DATE TIME CONVERT -> ${calendar.time}")

        return calendar.time
    }
}