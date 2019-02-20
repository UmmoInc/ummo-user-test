package xyz.ummo.user.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import javax.xml.datatype.Duration;

import androidx.recyclerview.widget.RecyclerView;
import xyz.ummo.user.AgentRequest;
import xyz.ummo.user.Department;
import xyz.ummo.user.R;
import xyz.ummo.user.Service;
import xyz.ummo.user.Services;

public class servicesAdapter extends RecyclerView.Adapter<servicesAdapter.MyViewHolder>  {

    private List<Service> services;
    Context context;

    public class MyViewHolder extends RecyclerView.ViewHolder {


        public TextView serviceTitle, serviceDescription, serviceForm, servicePersonalDocs,
                serviceCost, serviceDuration;

        Button requestAgentButton;

        public RelativeLayout departmentGround;


        public MyViewHolder(View view) {
            super(view);
            //departmentGround = view.findViewById(R.id.department_ground);
            serviceTitle= view.findViewById(R.id.service_title);
            serviceDescription = view.findViewById(R.id.service_description);
            serviceForm = view.findViewById(R.id.service_form);
            servicePersonalDocs = view.findViewById(R.id.service_personal_docs);
            serviceCost = view.findViewById(R.id.service_cost);
            serviceDuration = view.findViewById(R.id.service_duration);

            requestAgentButton = view.findViewById(R.id.request_agent_btn);
        }
    }


    public servicesAdapter(Context context, List<Service>  services) {
        this.services = services;
        this.context = context;
    }

    @Override
    public servicesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.services_list, parent, false);


        return new servicesAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(servicesAdapter.MyViewHolder holder, int position) {
        Service service = services.get(position);
        holder.serviceTitle.setText(service.getServiceName());
        holder.serviceDescription.setText(service.getServiceDescription());
        holder.serviceForm.setText(service.getForm());
        holder.servicePersonalDocs.setText(service.getPersonalDocs());
        holder.serviceCost.setText(service.getCost());
        holder.serviceDuration.setText(service.getDuraion());

        final String serviceName, form, personalDocs, cost, duration;

        serviceName = holder.serviceTitle.getText().toString();
        form = holder.serviceForm.getText().toString();
        personalDocs = holder.servicePersonalDocs.getText().toString();
        cost = holder.serviceCost.getText().toString();
        duration = holder.serviceDuration.getText().toString();

        holder.requestAgentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i= new Intent(context, AgentRequest.class);
                i.putExtra("serviceName", serviceName);
                i.putExtra("form", form);
                i.putExtra("personalDocs", personalDocs);
                i.putExtra("cost", cost);
                i.putExtra("duration", duration);
                context.startActivity(i);

            }
        });
    }

    @Override
    public int getItemCount() {
        return services.size();

    }
}
