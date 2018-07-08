package com.bluebirdaward.joinin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.pojo.Action;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by duyvu on 4/26/16.
 */
public class ActionAdapter extends RecyclerView.Adapter<ActionAdapter.ViewHolder> {

    private ArrayList<Action> actions = new ArrayList<>();
    private Context context;

    public ActionAdapter(ArrayList<Action> actions) {
        this.actions = actions;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tvActionName)
        TextView tvActionName;
        @Bind(R.id.tvActionEmoticon)
        TextView tvActionEmoticon;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public ActionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_action, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(ActionAdapter.ViewHolder holder, final int position) {
        holder.tvActionName.setText(actions.get(position).getName());
        holder.tvActionEmoticon.setText(actions.get(position).getEmoticon());
    }

    @Override
    public int getItemCount() {
        if (actions != null)
            return actions.size();
        return 0;
    }
}
