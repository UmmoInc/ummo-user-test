package xyz.ummo.user.adapters;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import xyz.ummo.user.DelegatedService;
import xyz.ummo.user.DelegationChat;
import xyz.ummo.user.R;
import xyz.ummo.user.ui.MainScreen;
import xyz.ummo.user.ui.fragments.delegatedService.DelegatedServiceFragment;

public class DelegatedServiceAdapter extends RecyclerView.Adapter<DelegatedServiceAdapter.MyViewHolder>{

    private List<DelegatedService> delegatedServicesList;
    String agentName, serviceName;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView serviceName, agentName;
        public DelegatedService service;

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

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DelegatedService delegatedService= delegatedServicesList.get(position);

        agentName = delegatedService.getAgentName();
        serviceName = delegatedService.getServiceName();
        holder.serviceName.setText(delegatedService.getServiceName());
        holder.agentName.setText(delegatedService.getAgentName());

        holder.serviceName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DelegatedServiceFragment delegatedServiceFragment = new DelegatedServiceFragment();
                Bundle bundle = new Bundle();
                bundle.putString("SERVICE_ID", delegatedService.getServiceId());
                bundle.putString("SERVICE_AGENT_ID", delegatedService.getAgentId());
                bundle.putString("DELEGATED_PRODUCT_ID", delegatedService.getProductId());
//            bundle.putString("DELEGATED_PRODUCT_ID", intent.extras!!.getString("DELEGATED_PRODUCT_ID"))
                delegatedServiceFragment.setArguments(bundle);

                Log.e("DelegatedServiceAdapter", "onClick: "+ delegatedService.getAgentId()+" "+delegatedService.getProductId());

                FragmentTransaction fragmentTransaction = MainScreen.Companion.getSupportFM().beginTransaction();
                fragmentTransaction.replace(R.id.frame, delegatedServiceFragment);
                fragmentTransaction.commit();
            }
        });


    }

    @Override
    public int getItemCount() {
        return delegatedServicesList.size();

    }

}
