package xyz.ummo.user.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import xyz.ummo.user.AgentRequest;
import xyz.ummo.user.DetailedService;
import xyz.ummo.user.R;
import xyz.ummo.user.Service;

public class servicesAdapter extends RecyclerView.Adapter<servicesAdapter.MyViewHolder>  {

    private List<Service> services;
    Context context;
    private String departmentName;


    public class MyViewHolder extends RecyclerView.ViewHolder {


        public TextView serviceTitle, serviceDescription, serviceForm, servicePersonalDocs,
                serviceCost, serviceDuration, moreButton;
        String steps = "";

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

            moreButton = view.findViewById(R.id.more_on_the_service_text);
        }
    }


    public servicesAdapter(Context context, List<Service>  services, String departmentName) {
        this.services = services;
        this.context = context;
        this.departmentName = departmentName;
    }

    @Override
    public servicesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.service_content, parent, false);

        Log.e("Adapter","create vh");
        return new servicesAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(servicesAdapter.MyViewHolder holder, int position) {
        Service service = services.get(position);

        String steps ="";

        for (int i = 0; i< service.getSteps().size(); i++){
            steps += service.getSteps().get(i)+" ";
        }
        holder.steps = steps;

        holder.serviceTitle.setText(service.getServiceName());
        holder.serviceDescription.setText(service.getServiceDescription());
        holder.serviceForm.setText(" " + steps);
        holder.servicePersonalDocs.setText(" " + service.getPersonalDocs());
        holder.serviceCost.setText(" " + service.getCost());
        holder.serviceDuration.setText( " " + service.getDuraion());


        final String serviceName, description, form, personalDocs, cost, duration;

        serviceName = holder.serviceTitle.getText().toString();
        description = holder.serviceDescription.getText().toString();
        form = holder.serviceForm.getText().toString();
        personalDocs = holder.servicePersonalDocs.getText().toString();
        cost = holder.serviceCost.getText().toString();
        duration = holder.serviceDuration.getText().toString();

        holder.requestAgentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i= new Intent(context, AgentRequest.class);
                i.putExtra("departmentName", departmentName);
              //  i.putExtra("serviceId",service)
                i.putExtra("serviceName", serviceName);
                i.putExtra("form", form);
                i.putExtra("id",service.getId());
                i.putExtra("docs", personalDocs);
                i.putExtra("cost", cost);
                i.putExtra("steps",holder.steps);
                i.putExtra("duration", duration);
                context.startActivity(i);
            }
        });

        holder.moreButton.setOnClickListener(new View.OnClickListener(){


            @Override
            public void onClick(View v) {

                Intent intent = new Intent(context, DetailedService.class);
                intent.putExtra("serviceName", serviceName);
                intent.putExtra("description", description);
                intent.putExtra("cost", cost);
                intent.putExtra("steps",holder.steps);
                intent.putExtra("duration", duration);
                intent.putExtra("docs",personalDocs);
                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return services.size();

    }
}
