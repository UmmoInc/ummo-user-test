package xyz.ummo.user.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import xyz.ummo.user.ui.ChatBubble
import xyz.ummo.user.R

class MessageAdapter(private val activity: Activity, resource: Int, private val messages: List<ChatBubble>) : ArrayAdapter<ChatBubble?>(activity, resource, messages) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        val inflater = activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var layoutResource = 0 // determined by view type
        val ChatBubble = getItem(position)
        val viewType = getItemViewType(position)
        layoutResource = if (ChatBubble!!.myMessage()) {
            R.layout.left_chat_bubble
        } else {
            R.layout.right_chat_bubble
        }

        /*if (convertView != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {*/convertView = inflater.inflate(layoutResource, parent, false)
        holder = ViewHolder(convertView)
        convertView.tag = holder
        // }

        //set message content
        holder.msg.text = ChatBubble.content
        return convertView
    }

    override fun getViewTypeCount(): Int {
        // return the total number of view types. this value should never change
        // at runtime. Value 2 is returned because of left and right views.
        return 2
    }

    override fun getItemViewType(position: Int): Int {
        // return a value between 0 and (getViewTypeCount - 1)
        return position % 2
    }

    private inner class ViewHolder(v: View) {
        val msg: TextView = v.findViewById<View>(R.id.txt_msg) as TextView

    }
}