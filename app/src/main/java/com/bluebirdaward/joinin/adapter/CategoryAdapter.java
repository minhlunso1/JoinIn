package com.bluebirdaward.joinin.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import com.bluebirdaward.joinin.R;
import com.bluebirdaward.joinin.pojo.Category;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by duyvu on 4/26/16.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private ArrayList<Category> categories = new ArrayList<>();
    private Context context;

    public CategoryAdapter(ArrayList<Category> categories) {
        this.categories = categories;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Bind(R.id.tvCategoryName)
        TextView tvCategoryName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onClick(View v) {

        }
    }

    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_category, parent, false);

        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(CategoryAdapter.ViewHolder holder, final int position) {
        holder.tvCategoryName.setText(categories.get(position).getName());
    }

    @Override
    public int getItemCount() {
        if (categories != null)
            return categories.size();
        return 0;
    }
}
