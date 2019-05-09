package com.modestie.modestieapp;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.modestie.modestieapp.model.freeCompany.FreeCompany;
import com.modestie.modestieapp.sql.FreeCompanyDbHelper;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private FreeCompanyDbHelper dbHelper;
    private SQLiteDatabase database;

    private FreeCompany freeCompany;

    private TextView fcNameView;
    private TextView fcSigleView;
    private TextView fcFormedView;
    private TextView fcSloganView;
    private TextView fcMembersCountView;
    private TextView fcRankView;
    private TextView gcNameView;
    private TextView gcRankView;
    private TextView gcProgressTextView;
    private TextView fcMonthlyRankingView;
    private TextView fcWeeklyRankingView;

    private ImageView crestBackgroundView;
    private ImageView crestFrameView;
    private ImageView crestLogoView;
    private ImageView gcFlagView;

    private ProgressBar gcMaelProgressBarView;
    private ProgressBar gcAdderProgressBarView;
    private ProgressBar gcFlamesProgressBarView;

    public static final String TAG = "ACTIVITY - HOME";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        this.fcNameView = findViewById(R.id.freeCompanyName);
        this.fcSigleView = findViewById(R.id.freeCompanySigle);
        this.fcFormedView = findViewById(R.id.formationDate);
        this.fcSloganView = findViewById(R.id.slogan);
        this.fcMembersCountView = findViewById(R.id.memberCount);
        this.fcRankView = findViewById(R.id.rank);
        this.gcNameView = findViewById(R.id.grandcompanyName);
        this.gcRankView = findViewById(R.id.grandcompanyRank);
        this.gcProgressTextView = findViewById(R.id.grandcompanyProgressText);
        this.fcMonthlyRankingView = findViewById(R.id.rankingMonthly);
        this.fcWeeklyRankingView = findViewById(R.id.rankingWeekly);
        this.crestBackgroundView = findViewById(R.id.crestBackground);
        this.crestFrameView = findViewById(R.id.crestFrame);
        this.crestLogoView = findViewById(R.id.crestLogo);
        this.gcFlagView = findViewById(R.id.grandcompanyFlag);
        this.gcMaelProgressBarView = findViewById(R.id.grandcompanyMaelProgressbar);
        this.gcAdderProgressBarView = findViewById(R.id.grandcompanyAdderProgressbar);
        this.gcFlamesProgressBarView = findViewById(R.id.grandcompanyFlamesProgressbar);

        this.dbHelper = new FreeCompanyDbHelper(getApplicationContext());
        this.database = this.dbHelper.getReadableDatabase();

        this.freeCompany = new FreeCompany(this.database, 0);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        hideGcProgressBars();

        this.fcNameView.setText(this.freeCompany.getName());
        this.fcSigleView.setText(String.format(Locale.FRANCE, "«%s»", this.freeCompany.getTag()));
        Date date = new Date(this.freeCompany.getFormed() * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE);
        this.fcFormedView.setText(String.format(Locale.FRANCE,"Formée le %s", sdf.format(date)));
        this.fcSloganView.setText(this.freeCompany.getSlogan());
        this.fcMembersCountView.setText(String.format(Locale.FRANCE, "%d", this.freeCompany.getMembers().size()));
        this.fcRankView.setText(String.format(Locale.FRANCE, "%d", this.freeCompany.getRank()));
        switch(this.freeCompany.getGrandCompanyName())
        {
            case "Maelstrom":
                this.gcNameView.setText(R.string.gc_maelstrom_name);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    this.gcNameView.setTextColor(getColor(R.color.maelstromColor));
                    this.gcMaelProgressBarView.setVisibility(View.VISIBLE);
                }
                else
                {
                    this.gcNameView.setTextColor(Color.parseColor("#E53935"));
                }
                Picasso.get().load(R.drawable.gc_maelstrom_flag).into(this.gcFlagView);
                break;

            case "Order of the Twin Adder":
                this.gcNameView.setText(R.string.gc_adders_name);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    this.gcNameView.setTextColor(getColor(R.color.twinAdderColor));
                    this.gcAdderProgressBarView.setVisibility(View.VISIBLE);
                }
                else
                {
                    this.gcNameView.setTextColor(Color.parseColor("#FFC107"));
                }
                Picasso.get().load(R.drawable.gc_twinadder_flag).into(this.gcFlagView);
                break;

            case "Immortal Flames":
                this.gcNameView.setText(R.string.gc_flames_name);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    this.gcNameView.setTextColor(getColor(R.color.immortalFlamesColor));
                    this.gcFlamesProgressBarView.setVisibility(View.VISIBLE);
                }
                else
                {
                    this.gcNameView.setTextColor(Color.parseColor("#1976D2"));
                }
                Picasso.get().load(R.drawable.gc_flames_flag).into(this.gcFlagView);
                break;
        }
        this.gcRankView.setText(this.freeCompany.getGrandCompanyRank());
        this.gcProgressTextView.setText(String.format(Locale.FRANCE, "%d%%", this.freeCompany.getGrandCompanyProgress()));
        this.fcMonthlyRankingView.setText(String.format(Locale.FRANCE, "%de", this.freeCompany.getMonthlyRanking()));
        this.fcWeeklyRankingView.setText(String.format(Locale.FRANCE, "%de", this.freeCompany.getWeeklyRanking()));

        Picasso.get().load(this.freeCompany.getCrestBackground()).into(this.crestBackgroundView);
        Picasso.get().load(this.freeCompany.getCrestFrame()).into(this.crestFrameView);
        Picasso.get().load(this.freeCompany.getCrestLogo()).into(this.crestLogoView);
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the HomeActivity/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_members)
        {

        }
        else if (id == R.id.nav_events)
        {

        }
        else if (id == R.id.nav_tools)
        {

        }
        else if (id == R.id.nav_share)
        {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void hideGcProgressBars()
    {
        this.gcMaelProgressBarView.setVisibility(View.INVISIBLE);
        this.gcAdderProgressBarView.setVisibility(View.INVISIBLE);
        this.gcFlamesProgressBarView.setVisibility(View.INVISIBLE);
    }
}
