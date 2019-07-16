package com.modestie.modestieapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.modestie.modestieapp.R;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.sqlite.FreeCompanyReaderContract;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.EventViewHolder>
{
    public Context context;

    private ArrayList<Event> events;
    private Map<Integer, FreeCompanyMember> members;

    private static final int VIEW_CARD = 0;
    private static final int VIEW_SPACE = 1;

    public static final String TAG = "ADPTR.EVENTLIST";

    //Declared to be used with the blank space
    public static class EventViewHolder extends RecyclerView.ViewHolder
    {
        EventViewHolder(View v) {super(v);}
    }

    // This class provide a reference to the views for each data item
    public static class EventListCardViewHolder extends EventViewHolder
    {
        View v;
        TextView title;
        TextView promoter;
        ImageView promoterAvatar;
        ImageView image;
        TextView date;
        TextView description;
        TextView participantCount;
        //ImageView expand;
        Button action;
        ImageView participationCheck;
        TextView participationText;

        boolean expanded;
        int userID;
        boolean promoterParticipation;
        boolean userIsPromoter;

        EventListCardViewHolder(View v, Context context)
        {
            super(v);
            this.v = v;
            this.title = v.findViewById(R.id.eventTitle);
            this.promoter = v.findViewById(R.id.characterPromoter);
            this.promoterAvatar = v.findViewById(R.id.promoterAvatar);
            this.image = v.findViewById(R.id.eventImage);
            this.date = v.findViewById(R.id.eventDate);
            this.description = v.findViewById(R.id.eventDescription);
            this.participantCount = v.findViewById(R.id.participantsCount);
            this.participationCheck = v.findViewById(R.id.participationCheck);
            this.participationText = v.findViewById(R.id.participationText);

            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            {
                this.participationCheck.setColorFilter(context.getColor(R.color.colorValidateLight));
                this.participationText.setTextColor(context.getColor(R.color.colorValidateLight));
            }

            //this.expand = v.findViewById(R.id.expand);
            this.action = v.findViewById(R.id.actionBtn);
            this.expanded = false;

            this.userID = 11148489;
            this.promoterParticipation = false;
            this.userIsPromoter = false;
        }
    }

    @SuppressLint("UseSparseArrays")
    public EventListAdapter(ArrayList<Event> events, SQLiteDatabase database, Context context)
    {
        this.events = events;
        this.context = context;

        Cursor cursor = database.rawQuery("SELECT * FROM " + FreeCompanyReaderContract.MemberEntry.TABLE_NAME, null);
        this.members = new HashMap<>();
        if (cursor.moveToFirst())
        {
            do
            {
                FreeCompanyMember member = new FreeCompanyMember(cursor);
                this.members.put(member.getID(), member);
            }
            while (cursor.moveToNext());
        }
        else
        {
            Log.e(TAG, "No entries found in " + FreeCompanyReaderContract.MemberEntry.TABLE_NAME + " table");
        }

        cursor.close();
    }

    @Override
    public int getItemViewType(int position)
    {
        if (this.events.get(position) == null)
        {
            return VIEW_SPACE;
        }
        else
        {
            return VIEW_CARD;
        }
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public EventListAdapter.EventViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType)
    {
        // create a new view
        if (viewType == VIEW_CARD)
            return new EventListCardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.eventlist_card, parent, false), this.context);
        else
            return new EventViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.eventlist_blankspace, parent, false));
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull EventViewHolder vHolder, int position)
    {
        //Skip if this is a blank space
        if (getItemViewType(position) == VIEW_SPACE) return;

        EventListCardViewHolder holder = (EventListCardViewHolder) vHolder;

        Event event = this.events.get(position);
        FreeCompanyMember member = this.members.get(event.getPromoterID());
        assert member != null;

        //TODO REPLACE THIS WITH FUTURE USER AUTHENTICATION
        //boolean userIsPromoter = member.getID() == [PREFERENCES -> APP USER ID];
        //boolean userIsPromoter = true;
        holder.userIsPromoter = event.getPromoterID() == 11148489;

        //Get promoterParticipation status
        holder.promoterParticipation = event.isPromoterParticipant();

        //Initialize views

        //Header (avatar + title + promoter)
        holder.title.setText(event.getName());
        holder.promoter.setText(String.format(Locale.FRANCE, "Organisé par %s", member.getName()));
        Picasso.get()
                .load(member.getAvatarURL())
                .into(holder.promoterAvatar);

        //Event image
        if (event.getImageURL() != null && !event.getImageURL().equals(""))
        {
            Picasso.get()
                    .load(event.getImageURL())
                    .fit()
                    .centerCrop()
                    .into(holder.image);
        }

        //Expand More/Less icon
        /*AnimatedVectorDrawable animatedExpandMore = (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ic_expand_more_animatable, null);
        if(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
            animatedExpandMore.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        else
            animatedExpandMore.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
        vholder.expand.setImageDrawable(animatedExpandMore);
        vholder.expand.setOnClickListener(v -> expandOrCollapseDescription(vholder));*/

        //Date
        Date eventTime = new Date(event.getEventEpochTime() * 1000);
        SimpleDateFormat eventDateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);
        SimpleDateFormat eventTimeFormat = new SimpleDateFormat("HH:mm", Locale.FRANCE);
        holder.date.setText(String.format(Locale.FRANCE, "Le %s à %s", eventDateFormat.format(eventTime), eventTimeFormat.format(eventTime)));

        //Description
        if (event.getDescription() == null || event.getDescription().isEmpty())
            holder.description.setText(R.string.event_description_null);
        else
            holder.description.setText(event.getDescription());
        //vholder.description.setOnClickListener(v -> expandOrCollapseDescription(vholder));

        //Participation text feedback
        if (holder.userIsPromoter) //Is app user promoter ?
        {
            holder.action.setVisibility(View.INVISIBLE);
            holder.participationText.setText(R.string.event_self_promoter_feedback);
            holder.participationText.setVisibility(View.VISIBLE);
            holder.participationCheck.setVisibility(View.VISIBLE);

            ConstraintSet set = new ConstraintSet();
            ConstraintLayout parentLayout = holder.v.findViewById(R.id.eventCardContent);
            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, context.getResources().getDisplayMetrics());
            set.clone(parentLayout);
            set.connect(R.id.participationCheck, ConstraintSet.START, R.id.eventCardContent, ConstraintSet.START, margin);
            set.applyTo(parentLayout);
        }
        else
        {
            holder.participationText.setText(R.string.event_pariticpation_feedback);
            updateParticipationButton(holder);
        }

        //Participants count
        updateParticipantsViews(holder, event);

        //Action button
        holder.action.setOnClickListener(v ->
                                         {
                                             if (!holder.promoterParticipation) //Participation request -> check if a place is available
                                             {
                                                 if (event.getMaxParticipants() == -1)
                                                 {
                                                     holder.promoterParticipation = true;
                                                     updateParticipation(holder, event);
                                                 }
                                                 else if (event.getParticipantsIDs().size() < event.getMaxParticipants())
                                                 {
                                                     holder.promoterParticipation = true;
                                                     updateParticipation(holder, event);
                                                 }
                                                 else
                                                 {
                                                     Toast.makeText(context, "Désolé, c'est complet !", Toast.LENGTH_SHORT).show();
                                                 }
                                             }
                                             else //Cancel promoterParticipation
                                             {
                                                 //Promoters can't cancel promoterParticipation to their own events
                                                 if (event.getPromoterID() != holder.userID)
                                                 {
                                                     holder.promoterParticipation = false;
                                                     updateParticipation(holder, event);
                                                 }
                                             }

                                             updateParticipationButton(holder);
                                             updateParticipantsViews(holder, event);
                                         });
    }

    private void updateParticipation(EventListCardViewHolder holder, Event event)
    {
        if (holder.promoterParticipation)
            event.getParticipantsIDs().add(holder.userID);
        else
            event.removeParticipant(holder.userID);
    }

    private void updateParticipantsViews(EventListCardViewHolder holder, Event event)
    {
        int participants = event.getParticipantsIDs().size();
        if (holder.userIsPromoter) participants++;

        if (event.getMaxParticipants() == -1)
            holder.participantCount.setText(String.format(Locale.FRANCE, "%d/∞", participants));
        else
            holder.participantCount.setText(String.format(Locale.FRANCE, "%d/%d", participants, event.getMaxParticipants()));
    }

    private void updateParticipationButton(EventListCardViewHolder holder)
    {
        if (holder.promoterParticipation)
        {
            holder.action.setText(R.string.button_cancel_participation);
            holder.participationCheck.setVisibility(View.VISIBLE);
            holder.participationText.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.action.setText(R.string.button_participate);
            holder.participationCheck.setVisibility(View.INVISIBLE);
            holder.participationText.setVisibility(View.INVISIBLE);
        }
    }

    private void expandOrCollapseDescription(EventListCardViewHolder holder)
    {
        if (holder.expanded)
        {
            AnimatedVectorDrawable animatedExpandLess = (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ic_expand_less_animatable, null);
            //holder.expand.setImageDrawable(animatedExpandLess);
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                animatedExpandLess.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            else
                animatedExpandLess.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            animatedExpandLess.start();
            holder.description.setMaxLines(2);
        }
        else
        {
            AnimatedVectorDrawable animatedExpandMore = (AnimatedVectorDrawable) context.getResources().getDrawable(R.drawable.ic_expand_more_animatable, null);
            //holder.expand.setImageDrawable(animatedExpandMore);
            if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES)
                animatedExpandMore.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            else
                animatedExpandMore.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            animatedExpandMore.start();
            holder.description.setMaxLines(Integer.MAX_VALUE);
        }

        holder.expanded = !holder.expanded;
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return events.size();
    }
}
