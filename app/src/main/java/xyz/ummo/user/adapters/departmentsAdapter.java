package xyz.ummo.user.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import xyz.ummo.user.Department;
import xyz.ummo.user.R;

public class departmentsAdapter extends RecyclerView.Adapter<departmentsAdapter.MyViewHolder>  {

    private List<Department> departments;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView departmentTitle;
        public RelativeLayout departmentGround;


        public MyViewHolder(View view) {
            super(view);
            //departmentGround = view.findViewById(R.id.department_ground);
            departmentTitle = view.findViewById(R.id.department_title);
        }
    }


    public departmentsAdapter(Context context, List<Department>  departments) {
        this.departments = departments;
        this.context = context;
    }

    @Override
    public departmentsAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.departments_list, parent, false);



        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Department department= departments.get(position);
        final TextView departmentTile = holder.departmentTitle;


        holder.departmentTitle.setText("Browse " + department.getDepartmentName() + " Services");

        /*holder.contactsForeground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                call.setVisibility(View.VISIBLE);
                profile.setVisibility(View.VISIBLE);

            }
        });*/
    }

    @Override
    public int getItemCount() {
        return departments.size();

    }


    public void removeItem(int position) {
        departments.remove(position);

        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
    }
}
