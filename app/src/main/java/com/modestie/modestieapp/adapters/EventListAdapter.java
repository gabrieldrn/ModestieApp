package com.modestie.modestieapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.events.list.EventDetailsDialogFragment;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.sqlite.FreeCompanyReaderContract;
import com.orhanobut.hawk.Hawk;
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
    private boolean userIsLoggedIn;
    private LightCharacter userCharacter;
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
        ImageView participationCheck;
        TextView participationText;

        boolean expanded;
        boolean promoterParticipation;
        boolean userParticipation;
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

            this.expanded = false;

            this.promoterParticipation = false;
            this.userParticipation = false;
            this.userIsPromoter = false;
        }
    }

    @SuppressLint("UseSparseArrays")
    public EventListAdapter(ArrayList<Event> events, SQLiteDatabase database, boolean userIsLoggedIn, LightCharacter character, AppCompatActivity context)
    {
        this.events = events;
        this.context = context;
        this.userIsLoggedIn = userIsLoggedIn;
        this.userCharacter = character;

        Hawk.init(context).build();

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
        FreeCompanyMember promoter = this.members.get(event.getPromoterID());
        assert promoter != null;

        if (this.userCharacter != null)
            holder.userIsPromoter = event.getPromoterID() == this.userCharacter.getID();
        else
            holder.userIsPromoter = event.getPromoterID() == 0;

        //Get promoterParticipation status
        holder.promoterParticipation = event.isPromoterParticipant();

        //Get user participation
        holder.userParticipation = event.getParticipantsIDs().contains(this.userCharacter.getID());

        //Initialize views

        //Header (avatar + title + promoter)
        holder.title.setText(event.getName());
        holder.promoter.setText(String.format(Locale.FRANCE, "Organisé par %s", promoter.getName()));
        Picasso.get()
                .load(promoter.getAvatarURL())
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

        //Participation text feedback
        if (holder.userIsPromoter) //Is app user promoter ?
        {
            holder.participationText.setText(R.string.event_self_promoter_feedback);
            holder.participationText.setVisibility(View.VISIBLE);
            holder.participationCheck.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.participationText.setText(R.string.event_participation_feedback);
            //Check user participation
            if (holder.userParticipation)
            {
                updateParticipationButton(holder);
            }
        }

        //Participants count
        updateParticipantsViews(holder, event);

        holder.v.setOnClickListener(
                v ->
                {
                    if(Hawk.put("SelectedEvent", event) && Hawk.put("SelectedEventPromoter", promoter))
                        EventDetailsDialogFragment.display(((AppCompatActivity) context).getSupportFragmentManager());
                });
    }

    private void updateParticipantsViews(EventListCardViewHolder holder, Event event)
    {
        int participants = event.getParticipantsIDs().size();
        if (holder.promoterParticipation) participants++;

        if (event.getMaxParticipants() == -1)
            holder.participantCount.setText(String.format(Locale.FRANCE, "%d/∞", participants));
        else
            holder.participantCount.setText(String.format(Locale.FRANCE, "%d/%d", participants, event.getMaxParticipants()));
    }

    private void updateParticipationButton(EventListCardViewHolder holder)
    {
        if (holder.userParticipation)
        {
            holder.participationCheck.setVisibility(View.VISIBLE);
            holder.participationText.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.participationCheck.setVisibility(View.INVISIBLE);
            holder.participationText.setVisibility(View.INVISIBLE);
        }
    }

    // Return the size of the dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {
        return events.size();
    }
}
