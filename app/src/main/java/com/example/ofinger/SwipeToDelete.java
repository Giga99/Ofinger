package com.example.ofinger;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ofinger.adapters.ChatAdapter;

public class SwipeToDelete extends ItemTouchHelper.SimpleCallback {
    Context context;
    ChatAdapter adapter;
    Drawable trashIcon;
    ColorDrawable backgroundColor;

    public SwipeToDelete(Context context, ChatAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.context = context;
        trashIcon = ContextCompat.getDrawable(context, R.drawable.delete);
        backgroundColor = new ColorDrawable(Color.RED);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.deleteItem(viewHolder.getAdapterPosition());
    }

    public void swipeRight(View view, int margin, int top, int bottom, float dX){
        int left = view.getLeft() + margin;
        int right = view.getLeft() + margin + trashIcon.getIntrinsicWidth();

        int iconConstraint = (view.getLeft() + ((int) dX) < right + margin) ? (int)dX - trashIcon.getIntrinsicWidth() - (margin * 2) : 0;
        left += iconConstraint;
        right += iconConstraint;

        trashIcon.setBounds(left, top, right, bottom);
        backgroundColor.setBounds(view.getLeft(), view.getTop(), view.getLeft() + (int)dX, view.getBottom());
    }

    public void swipeLeft(View view, int margin, int top, int bottom, float dX){
        int right = view.getRight() - margin;
        int left = view.getRight() - margin - trashIcon.getIntrinsicWidth();

        int iconConstraint = (view.getRight() + ((int) dX) > left - margin) ? (int)dX + trashIcon.getIntrinsicWidth() + (margin * 2) : 0;
        left += iconConstraint;
        right += iconConstraint;

        trashIcon.setBounds(left, top, right, bottom);
        backgroundColor.setBounds(view.getRight() + (int)dX, view.getTop(), view.getRight(), view.getBottom());
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View view = viewHolder.itemView;

        int margin = (view.getHeight() - trashIcon.getIntrinsicHeight()) / 2;
        int top = view.getTop() + (view.getHeight() - trashIcon.getIntrinsicHeight()) / 2;
        int bottom = top + trashIcon.getIntrinsicHeight();

        if(dX > 0){
            swipeRight(view, margin, top, bottom, dX);
        } else if(dX < 0){
            swipeLeft(view, margin, top, bottom, dX);
        } else {
            backgroundColor.setBounds(0, 0, 0, 0);
        }

        backgroundColor.draw(c);
        trashIcon.draw(c);
    }
}
