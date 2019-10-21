package xyz.ummo.user.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import xyz.ummo.user.R;
import xyz.ummo.user.Services;
import xyz.ummo.user.delegate.PublicServiceData;

public class servicesCarouselAdapter extends BaseAdapter {

    private List<PublicServiceData> departments;
    Context context;
    String departmentName;

    public class ViewHolder {


        public TextView departmentTitle;
        public RelativeLayout serviceGround;


        public ViewHolder(View view) {

            //departmentGround = view.findViewById(R.id.department_ground);
            departmentTitle = view.findViewById(R.id.department_title);
           serviceGround = view.findViewById(R.id.department_ground);
        }
    }


    public servicesCarouselAdapter(Context context, List<PublicServiceData>  departments) {
        this.departments = departments;
        this.context = context;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.departments_list, null, false);

            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PublicServiceData item = departments.get(position);


        viewHolder.serviceGround.isClickable();

        viewHolder.serviceGround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(context, Services.class);
                intent.putExtra("departmentName", departments.get(position).getServiceName());
                intent.putExtra("public_service",departments.get(position).getServiceCode());

                ((Activity)context).finish();
                context.startActivity(intent);
            }
        });

        viewHolder.departmentTitle.setText("Browse " + item.getServiceName() + " Services");
        //departmentName = department.getDepartmentName();

        convertView.setOnClickListener(onClickListener(position));

        return convertView;
    }

    private View.OnClickListener onClickListener(final int position) {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Intent intent= new Intent(context, Services.class);
                intent.putExtra("departmentName", departments.get(position).getServiceName());
                intent.putExtra("public_service",departments.get(position).getServiceCode());
                ((Activity)context).finish();
                context.startActivity(intent);
            }
        };
    }



    @Override
    public int getCount() {
        return departments.size();

    }

    @Override
    public PublicServiceData getItem(int position) {
        return departments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
