package com.modestie.modestieapp.activities.login;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import com.makeramen.roundedimageview.RoundedImageView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.modestie.modestieapp.R;
import com.modestie.modestieapp.activities.HomeActivity;
import com.modestie.modestieapp.adapters.CharacterListAdapter;
import com.modestie.modestieapp.model.character.LightCharacter;
import com.modestie.modestieapp.model.freeCompany.FreeCompanyMember;
import com.modestie.modestieapp.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static com.android.volley.Request.Method.GET;
import static com.modestie.modestieapp.activities.login.LoginActivity.*;

/**
 * This fragment subclass instantiates the character verification fragments in the LoginFragmentPager
 */
public class CharacterRegistrationFragment extends Fragment
{
    private static final String TAG = "CHRCTR-REGISTR-FRG";

    private Button yesBtn;
    private Button noBtn;
    private View loadingView;
    private ImageView clipboardAction;
    private TextView hashTextView;
    private RoundedImageView avatarView;
    private ImageView rankIcon;
    private TextView memberNameView;
    private TextView memberRankView;

    private FreeCompanyMember member;
    private LightCharacter character;
    private int characterID;
    private String characterAvatar;

    //ExtendedCharacter selection
    private View CharacterSelection_FCMember;
    private View CharacterSelection_BasicUser;

    private FrameLayout characterSelectionLayout;
    private ImageView searchIcon;
    private LinearLayout searchLayout;
    private ConstraintLayout pagerLayout;
    private FrameLayout noContentPlaceholder;
    private ProgressBar noContentProgressBar;
    private TextView noContentLabel;
    private boolean FCMember;
    private ArrayList<LightCharacter> dataset;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter<CharacterListAdapter.CharacterViewHolder> adapter;
    private RecyclerView.LayoutManager layoutManager;

    private OnFragmentInteractionListener mListener;

    private static final String ARG_PAGE = "page";
    private int page;

    private boolean pending; //Used to disable some elements during a process

    private RequestQueue mRequestQueue;

    public CharacterRegistrationFragment()
    {
        // Required empty public constructor
    }

    /**
     * @return A new instance of fragment CharacterRegistrationFragment.
     */
    static CharacterRegistrationFragment newInstance(int page)
    {
        CharacterRegistrationFragment fragment = new CharacterRegistrationFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.pending = false;
        if (getArguments() != null)
        {
            this.page = getArguments().getInt(ARG_PAGE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View rootView;
        switch (page)
        {
            case CHARACTER_REGISTRATION_FIRST_PAGE:
                rootView = inflater.inflate(R.layout.fragment_character_regist_1, container, false);
                createFirstPage(rootView);
                break;

            case CHARACTER_REGISTRATION_CHARACTER_SELECTION_PAGE:
                rootView = inflater.inflate(R.layout.fragment_character_regist_2, container, false);
                createSecondPage(rootView);
                break;

            case CHARACTER_REGISTRATION_VERIFICATION_AND_REGISTRATION_PAGE:
                rootView = inflater.inflate(R.layout.fragment_character_regist_3, container, false);
                createThirdPage(rootView);
                break;

            case CHARACTER_REGISTRATION_DONE_PAGE:
                rootView = inflater.inflate(R.layout.fragment_character_regist_4, container, false);
                createFourthPage(rootView);
                break;

            default:
                return null;
        }

        return rootView;
    }

    /**
     * Setup views in the first fragment
     *
     * @param rootView Parent view
     */
    private void createFirstPage(View rootView)
    {
        this.yesBtn = rootView.findViewById(R.id.yesBtn);
        this.noBtn = rootView.findViewById(R.id.noBtn);
        this.yesBtn.setOnClickListener(v -> userTypeSelection(true));
        this.noBtn.setOnClickListener(v -> userTypeSelection(false));
    }

    /**
     * Setup views in the second fragment
     *
     * @param rootView Parent view
     */
    private void createSecondPage(View rootView)
    {
        this.characterSelectionLayout = rootView.findViewById(R.id.characterSelection);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        this.CharacterSelection_FCMember = inflater.inflate(R.layout.fragment_character_regist_2_fc_member, null);
        this.CharacterSelection_BasicUser = inflater.inflate(R.layout.fragment_character_regist_2_basic_character, null);

        this.loadingView = this.CharacterSelection_FCMember.findViewById(R.id.loadingView);

        //Recycler view setup
        this.dataset = new ArrayList<>();
        this.searchIcon = this.CharacterSelection_BasicUser.findViewById(R.id.searchIcon);
        this.noContentPlaceholder = this.CharacterSelection_BasicUser.findViewById(R.id.noContentPlaceholder);
        this.noContentProgressBar = this.CharacterSelection_BasicUser.findViewById(R.id.noContentProgress);
        this.noContentLabel = this.CharacterSelection_BasicUser.findViewById(R.id.noContentLabel);
        this.searchLayout = this.CharacterSelection_BasicUser.findViewById(R.id.searchLayout);
        this.pagerLayout = this.CharacterSelection_BasicUser.findViewById(R.id.searchPagerLayout);
        this.recyclerView = this.CharacterSelection_BasicUser.findViewById(R.id.characterRecyclerView);
        this.layoutManager = new LinearLayoutManager(getContext());
        this.recyclerView.setLayoutManager(this.layoutManager);
        //Set adapter and call callback listener to return selected item
        this.adapter = new CharacterListAdapter(this.dataset, this::characterChoosed);
        this.recyclerView.setAdapter(this.adapter);

        TextInputLayout searchField = this.CharacterSelection_BasicUser.findViewById(R.id.fieldCharacterSearch);

        searchField.getEditText().setOnEditorActionListener(
                (v, actionId, event) ->
                {
                    if (actionId == EditorInfo.IME_ACTION_DONE)
                    {
                        executeSearch(searchField);
                    }
                    return false;
                });

        this.searchIcon.setOnClickListener(v -> executeSearch(searchField));
    }

    private void executeSearch(TextInputLayout field)
    {
        String request = "https://xivapi.com/character/search?server=_dc_chaos&name=";
        request += Objects.requireNonNull(field.getEditText()).getText();

        field.getEditText().clearFocus();
        field.setEnabled(false);

        this.recyclerView.setVisibility(View.INVISIBLE);

        this.noContentPlaceholder.setAlpha(1f);
        this.noContentLabel.setVisibility(View.GONE);
        this.noContentProgressBar.setVisibility(View.VISIBLE);

        this.recyclerView.setVisibility(View.INVISIBLE);

        addToRequestQueue(new JsonObjectRequest(
                GET, request, null,
                response ->
                {
                    buildDatasetFromResponse(response);
                    this.adapter.notifyDataSetChanged();
                    this.recyclerView.setVisibility(View.VISIBLE);
                    this.noContentPlaceholder.animate().alpha(0f);
                    field.setEnabled(true);

                }, error ->
                {
                    this.noContentLabel.setVisibility(View.VISIBLE);
                    this.noContentProgressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Erreur réseau, veuillez réessayer", Toast.LENGTH_SHORT).show();
                    field.setEnabled(true);
                }));
    }

    public void buildDatasetFromResponse(JSONObject obj)
    {
        try
        {
            JSONArray results = obj.getJSONArray("Results");

            Log.e(TAG, results.toString());

            this.dataset.clear();

            for (int i = 0; i < results.length(); i++)
            {
                Log.e(TAG, results.getJSONObject(i).toString());
                this.dataset.add(new LightCharacter(results.getJSONObject(i)));
            }
        }
        catch (JSONException e)
        {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }

    /**
     * Setup views in the third fragment
     *
     * @param rootView Parent view
     */
    private void createThirdPage(View rootView)
    {
        this.yesBtn = rootView.findViewById(R.id.yesBtn);
        this.member = null;

        this.avatarView = rootView.findViewById(R.id.promoterAvatar);
        this.rankIcon = rootView.findViewById(R.id.rankIcon);
        this.memberNameView = rootView.findViewById(R.id.memberName);
        this.memberRankView = rootView.findViewById(R.id.memberRank);
        this.hashTextView = rootView.findViewById(R.id.hash);
        this.clipboardAction = rootView.findViewById(R.id.clipboardAction);
        Button openLodestoneBtn = rootView.findViewById(R.id.lodestoneOpenBtn);
        Button beginRegistrationBtn = rootView.findViewById(R.id.verifyBtn);

        this.loadingView = rootView.findViewById(R.id.loadingView);

        //Intent to the character lodestone page
        openLodestoneBtn.setOnClickListener(
                v ->
                {
                    if (!this.pending)
                    {
                        String url = "https://fr.finalfantasyxiv.com/lodestone/character/" + this.member.getID();
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    }
                });

        //Call registration request
        beginRegistrationBtn.setOnClickListener(
                v ->
                {
                    this.pending = true;
                    beginRegistration(this.hashTextView.getText() + "");
                });
    }

    /**
     * Setup views in the fourth fragment
     *
     * @param rootView Parent view
     */
    private void createFourthPage(View rootView)
    {
        Button finishBtn = rootView.findViewById(R.id.finish);
        finishBtn.setOnClickListener(v -> startActivity(new Intent(getContext(), HomeActivity.class)));
    }

    /**
     * Add one of the inflated character selection views (non-fc-member or fc-member) depending on if
     * the user a member of the FC or not.
     *
     * @param FCMember Is the user a member of the FC?
     */
    void setCharacterSelectionView(boolean FCMember)
    {
        this.FCMember = FCMember;
        if (this.FCMember)
            this.characterSelectionLayout.addView(this.CharacterSelection_FCMember);
        else
            this.characterSelectionLayout.addView(this.CharacterSelection_BasicUser);
    }

    /**
     * Edit character information preview in third fragment
     *
     * @param character The character chosen
     */
    void setCharacter(Object character)
    {
        this.member = null;
        this.character = null;
        this.characterID = 0;
        this.characterAvatar = "";

        if (character instanceof FreeCompanyMember)
        {
            this.member = (FreeCompanyMember) character;
            this.characterID = this.member.getID();
            this.characterAvatar = this.member.getAvatarURL();
            this.memberNameView.setText(this.member.getName());
            this.memberRankView.setText(this.member.getRank());

            Picasso.get()
                    .load(this.member.getRankIconURL())
                    .into(this.rankIcon);

            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(Color.TRANSPARENT)
                    .borderWidthDp(0)
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();

            Picasso.get()
                    .load(this.member.getAvatarURL())
                    .fit()
                    .transform(transformation)
                    .into(this.avatarView);
        }

        if (character instanceof LightCharacter)
        {
            this.character = (LightCharacter) character;
            this.characterID = this.character.getID();
            this.characterAvatar = this.character.getAvatarURL();
            this.memberNameView.setText(this.character.getName());
            this.memberRankView.setText(this.character.getServer());

            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(Color.TRANSPARENT)
                    .borderWidthDp(0)
                    .cornerRadiusDp(30)
                    .oval(false)
                    .build();

            Picasso.get()
                    .load(this.character.getAvatarURL())
                    .fit()
                    .transform(transformation)
                    .into(this.avatarView);
        }

        this.hashTextView.setText(Utils.bin2hex(Utils.getSHA256Hash(this.characterID + this.memberNameView.getText().toString())));
        copyHashToClipboard(); //Immediately copy hash
        this.clipboardAction.setOnClickListener(v -> copyHashToClipboard());
    }

    /**
     * Copy text present into hashTextView into clipboard
     */
    private void copyHashToClipboard()
    {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("lodestone hash", this.hashTextView.getText());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Copié dans le presse-papier", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    /**
     * Function callback
     *
     * @param FCMember Is user a FC member?
     */
    private void userTypeSelection(boolean FCMember)
    {
        if (mListener != null)
        {
            mListener.onUserTypeSelection(FCMember);
        }
    }

    /**
     * Function called when clicking an item inside non-members characters recycler view.
     *
     * @param character The picked character
     */
    private void characterChoosed(LightCharacter character)
    {
        if (mListener != null)
        {
            mListener.onCharacterSelection(character);
        }
    }

    /**
     * Begin character verification
     *
     * @param hash Hash used for verification
     */
    private void beginRegistration(String hash)
    {
        if (mListener != null && this.characterID != 0)
        {
            mListener.onBeginRegistrationInteraction(this.characterID, this.characterAvatar, hash);
        }
    }

    void pendingEnded()
    {
        this.pending = false;
    }

    /**
     * Toggles the visibility of loadingView (Present in some layouts)
     */
    void toggleMembersListVisibility()
    {
        this.loadingView.setVisibility(this.loadingView.getVisibility() == View.VISIBLE ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    public void onAttach(@NotNull Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener
    {
        void onUserTypeSelection(boolean FCMember);

        void onCharacterSelection(Object character);

        void onBeginRegistrationInteraction(int characterID, String characterAvatar, String hash);
    }

    /**
     * Lazy initialize the request queue, the queue instance will be created when it is accessed
     * for the first time
     *
     * @return Request Queue
     */
    public RequestQueue getRequestQueue()
    {
        if (this.mRequestQueue == null)
            this.mRequestQueue = Volley.newRequestQueue(getContext());

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag)
    {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req)
    {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag)
    {
        if (mRequestQueue != null)
            mRequestQueue.cancelAll(tag);
    }
}
