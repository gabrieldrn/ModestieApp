package com.modestie.modestieapp.activities.events.list;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.events.form.NewEventActivity;
import com.modestie.modestieapp.adapters.EventListAdapter;
import com.modestie.modestieapp.model.event.Event;
import com.modestie.modestieapp.sqlite.FreeCompanyDbHelper;
import com.modestie.modestieapp.utils.network.RequestHelper;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class EventListActivity extends AppCompatActivity implements EventDetailsDialogFragment.OnParticipationChanged
{
    private TextView noEventPlaceholder;
    private ArrayList<Event> events;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<EventListAdapter.EventViewHolder> adapter;
    private ShimmerFrameLayout shimmerFrameLayout;
    private ExtendedFloatingActionButton FAB;

    private EventDetailsDialogFragment eventDetailsFragment;

    private boolean userLoggedIn;

    private boolean pending;

    private RequestHelper requestHelper;

    private Snackbar errorSnackbar;

    public static final int NEW_EVENT_REQUEST = 1;
    public static final int EVENT_MODIFICATION_REQUEST = 2;
    //public static final int RESULT_OK =

    private String GET_EVENT_REQUEST_TAG = "GetEventsRequest";

    public static final String TAG = "ACTVT - EVNTLST";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list);

        this.requestHelper = new RequestHelper(getApplicationContext());
        this.pending = false;

        Toolbar toolbar = findViewById(R.id.eventListToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.noEventPlaceholder = findViewById(R.id.noEventsPlaceholder);
        this.recyclerView = findViewById(R.id.eventsCardsView);
        this.shimmerFrameLayout = findViewById(R.id.shimmerLayout);
        this.FAB = findViewById(R.id.newEventFAB);
        this.FAB.shrink();

        Hawk.init(getApplicationContext()).build();
        this.userLoggedIn = Hawk.contains("UserCharacter") && Hawk.contains("UserCredentials");
        this.events = new ArrayList<>();

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (userLoggedIn)
        {
            //FAB intent
            this.FAB.show();
            this.FAB.setOnClickListener(
                    v ->
                    {
                        this.requestHelper.cancelPendingRequests(this.GET_EVENT_REQUEST_TAG);
                        startActivityForResult(new Intent(getApplicationContext(), NewEventActivity.class), NEW_EVENT_REQUEST);
                    });
            //FAB animations
            this.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
            {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
                {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0)
                        FAB.extend();
                    else
                        FAB.shrink();
                }
            });
        }
        else this.FAB.hide();

        View contextView = findViewById(R.id.context_view);
        this.errorSnackbar = Snackbar.make(contextView, "Erreur de réception", BaseTransientBottomBar.LENGTH_INDEFINITE).setAction("Réessayer", v -> updateList());

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        updateList();
    }

    @Override
    protected void onStop()
    {
        /*if (this.eventDetailsFragment != null && this.eventDetailsFragment.isVisible())
            this.eventDetailsFragment.dismiss();*/
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.event_list_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.refresh)
        {
            updateList();
            this.errorSnackbar.dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.e(TAG, "RESULT : " + requestCode + " / " + resultCode + " // " + RESULT_OK + " / " + RESULT_CANCELED);
        if (requestCode == NEW_EVENT_REQUEST)
        {
            // Make sure the request was successful
            if (resultCode == RESULT_OK)
                updateList();

            if (resultCode == RESULT_CANCELED)
            {
                if (data == null)
                    return;

                if (data.hasExtra("Error"))
                    Toast.makeText(this, "Echec du processus, nous rencontrons des difficultés techniques.", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Envoi annulé.", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == EVENT_MODIFICATION_REQUEST)
        {
            if (resultCode == RESULT_OK)
            {
                Log.e(TAG, "RESULT OK");
                if (this.eventDetailsFragment != null && this.eventDetailsFragment.isVisible())
                {
                    Log.e(TAG, "DISMISS");
                    this.eventDetailsFragment.dismiss();
                }
            }
            updateList();
        }
    }

    private void updateList()
    {
        if (this.pending) return;

        this.pending = true;

        //Clear list
        if (!this.events.isEmpty())
        {
            this.events.clear();
            this.adapter.notifyDataSetChanged();
            //this.progressBar.setVisibility(View.VISIBLE);
        }

        this.noEventPlaceholder.setVisibility(View.GONE);
        this.shimmerFrameLayout.setVisibility(View.VISIBLE);
        this.shimmerFrameLayout.startShimmer();

        FreeCompanyDbHelper dbHelper = new FreeCompanyDbHelper(getApplicationContext());
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        if (this.userLoggedIn)
            this.adapter = new EventListAdapter(this.events, database, true, Hawk.get("UserCharacter"), this);
        else
            this.adapter = new EventListAdapter(this.events, database, false, null, this);

        this.recyclerView.setAdapter(this.adapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").get()
                .addOnCompleteListener(
                        task ->
                        {
                            if (task.isSuccessful())
                            {
                                for (QueryDocumentSnapshot document : task.getResult())
                                {
                                    //Log.d(TAG, document.getId() + " => " + document.getData());
                                    this.events.add(new Event(document));

                                }
                                Collections.sort(this.events, Event.EventDateComparator);
                                if (this.events.isEmpty())
                                {
                                    //this.progressBar.setVisibility(View.INVISIBLE);
                                    this.shimmerFrameLayout.stopShimmer();
                                    this.shimmerFrameLayout.setVisibility(View.INVISIBLE);
                                    this.noEventPlaceholder.setVisibility(View.VISIBLE);
                                    if (this.userLoggedIn)
                                    {
                                        this.noEventPlaceholder.setText(getString(R.string.no_event_user));
                                        this.FAB.extend();
                                    }
                                    else this.noEventPlaceholder.setText(getString(R.string.no_event));
                                }
                                else
                                {
                                    //If user is logged in, the fab is visible and a blank space must be added
                                    //at the end of the list avoid obstructing visibility of the last card
                                    if (this.userLoggedIn) this.events.add(null);
                                    this.adapter.notifyDataSetChanged();
                                    //this.progressBar.setVisibility(View.INVISIBLE);
                                    this.shimmerFrameLayout.stopShimmer();
                                    this.shimmerFrameLayout.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                this.errorSnackbar.show();
                                Log.e(TAG, "Error getting documents: ", task.getException());
                            }
                            this.pending = false;
                        });
    }

    @Override
    public void onAttachFragment(@NotNull Fragment fragment)
    {
        if (fragment instanceof EventDetailsDialogFragment)
        {
            this.eventDetailsFragment = (EventDetailsDialogFragment) fragment;
            this.eventDetailsFragment.setOnParticipationChanged(this);
        }
    }

    @Override
    public void participationChanged()
    {
        updateList();
    }
}
