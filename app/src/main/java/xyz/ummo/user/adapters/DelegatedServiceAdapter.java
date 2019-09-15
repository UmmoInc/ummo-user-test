package xyz.ummo.user.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.ummo.user.DelegatedService;
import xyz.ummo.user.DelegationChat;
import xyz.ummo.user.R;

public class DelegatedServiceAdapter extends RecyclerView.Adapter<DelegatedServiceAdapter.MyViewHolder>{

    private List<DelegatedService> delegatedServicesList;
    String agentName, serviceName;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView serviceName, agentName;

        public MyViewHolder(View view) {
            super(view);

            serviceName = view.findViewById(R.id.service_name);
            agentName = view.findViewById(R.id.agent_name);

        }
    }

    public DelegatedServiceAdapter(List<DelegatedService> delegatedServicesList){
        this.delegatedServicesList= delegatedServicesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.delegated_services_list, parent, false);

        RelativeLayout delegatedSevice = itemView.findViewById(R.id.delegated_service);
        delegatedSevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(itemView.getContext(), DelegationChat.class);
                intent.putExtra("agentName", agentName);
                intent.putExtra("serviceName", serviceName);
                itemView.getContext().startActivity(intent);
            }
        });

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DelegatedService delegatedService= delegatedServicesList.get(position);

        agentName = delegatedService.getAgentName();
        serviceName = delegatedService.getServiceName();

        holder.serviceName.setText(delegatedService.getServiceName());
        holder.agentName.setText(delegatedService.getAgentName());


    }

    @Override
    public int getItemCount() {
        return delegatedServicesList.size();

    }

}
