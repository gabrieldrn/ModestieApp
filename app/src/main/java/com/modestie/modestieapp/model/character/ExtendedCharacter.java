package com.modestie.modestieapp.model.character;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.modestie.modestieapp.sqlite.CharacterDbHelper;
import com.modestie.modestieapp.sqlite.CharacterReaderContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExtendedCharacter extends Character
{
    private static final String TAG = "XIVAPI.CHA";

    public static final String[] GEAR_ITEM_KEYS =
            {
                    "MainHand",
                    "Head",
                    "Body",
                    "Hands",
                    "Waist",
                    "Legs",
                    "Feet",
                    "OffHand",
                    "Earrings",
                    "Necklace",
                    "Bracelets",
                    "Ring1",
                    "Ring2",
                    "SoulCrystal"
            };

    private boolean loaded;

    private static final String apiID_ARM = "10_10";
    private static final String apiID_ORF = "11_11";
    private static final String apiID_TAN = "12_12";
    private static final String apiID_COU = "13_13";
    private static final String apiID_ALC = "14_14";
    private static final String apiID_CUI = "15_15";
    private static final String apiID_MIN = "16_16";
    private static final String apiID_BTN = "17_17";
    private static final String apiID_PEC = "18_18";
    private static final String apiID_PLD = "1_19";
    private static final String apiID_INV = "26_27";
    private static final String apiID_ERU = "26_28";
    private static final String apiID_NIN = "29_30";
    private static final String apiID_MOI = "2_20";
    private static final String apiID_MCH = "31_31";
    private static final String apiID_CHN = "32_32";
    private static final String apiID_AST = "33_33";
    private static final String apiID_SAM = "34_34";
    private static final String apiID_MRG = "35_35";
    private static final String apiID_MBU = "36_36";
    private static final String apiID_GUE = "3_21";
    private static final String apiID_DRG = "4_22";
    private static final String apiID_BRD = "5_23";
    private static final String apiID_MBL = "6_24";
    private static final String apiID_MNO = "7_25";
    private static final String apiID_MEN = "8_8";
    private static final String apiID_FRG = "9_9";

    private ClassJob activeClassJob;
    private ArrayList<ClassJob> classJobs;
    private Map<String, GearItem> gearItems;

    private int ilvl;

    private ArrayList<CharacterAttribute> attributes;

    private CharacterGrandCompany grandCompany;

    private CharacterGuardianDeity guardianDeity;

    private SQLiteDatabase database;

    public ExtendedCharacter(JSONObject obj, @org.jetbrains.annotations.NotNull CharacterDbHelper dbHelper)
    {
        super(obj);

        this.loaded = false;

        this.database = dbHelper.getWritableDatabase();
        dbHelper.resetDatabase(this.database);

        try
        {
            this.activeClassJob = new ClassJob(obj.getJSONObject("ActiveClassJob"));

            this.classJobs = new ArrayList<>();
            JSONObject apiClassJobs = obj.getJSONObject("ClassJobs");
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_PLD)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_GUE)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_CHN)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_MBL)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_ERU)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_AST)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_MOI)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_DRG)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_NIN)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_SAM)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_BRD)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_MCH)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_MNO)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_INV)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_MRG)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_MBU)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_MEN)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_FRG)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_ARM)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_ORF)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_TAN)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_COU)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_ALC)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_CUI)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_MIN)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_BTN)));
            this.classJobs.add(new ClassJob(apiClassJobs.getJSONObject(apiID_PEC)));

            JSONObject apiGearSet = obj.getJSONObject("GearSet");
            JSONObject apiGear = apiGearSet.getJSONObject("Gear");

            this.gearItems = new HashMap<>();
            for (String gearItemKey : GEAR_ITEM_KEYS)
            {
                if (!apiGear.isNull(gearItemKey))
                    this.gearItems.put(gearItemKey, new GearItem(apiGear.getJSONObject(gearItemKey)));
                else
                    this.gearItems.put(gearItemKey, null);
            }

            calcIlvl();

            JSONArray apiAttributes = apiGearSet.getJSONArray("Attributes");
            this.attributes = new ArrayList<>();
            for(int i = 0; i < apiAttributes.length(); i++)
            {
                this.attributes.add(new CharacterAttribute(apiAttributes.getJSONObject(i)));
            }

            this.grandCompany = new CharacterGrandCompany(obj.getJSONObject("GrandCompany"));

            this.guardianDeity = new CharacterGuardianDeity(obj.getJSONObject("GuardianDeity"));

            this.loaded = true;

            ContentValues characterValues = new ContentValues();
            characterValues.put(CharacterReaderContract.CharacterUpdateEntry.COLUMN_NAME_CHARACTER_ID, this.ID);
            characterValues.put(CharacterReaderContract.CharacterUpdateEntry.COLUMN_NAME_LAST_UPDATE, System.currentTimeMillis() / 1000);
            this.database.insert(CharacterReaderContract.CharacterUpdateEntry.TABLE_NAME, null, characterValues);
        }
        catch (Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    private void calcIlvl()
    {
        int sum = 0;

        if(gearItems.get("OffHand") == null)
            sum += gearItems.get("MainHand").getItemLevel() * 2;
        else
            sum += gearItems.get("MainHand").getItemLevel() +
                    gearItems.get("OffHand").getItemLevel();

        for (String gearItemKey : GEAR_ITEM_KEYS)
        {
            if (!gearItemKey.equals("MainHand") && !gearItemKey.equals("OffHand") && !gearItemKey.equals("SoulCrystal"))
            {
                if(gearItems.get(gearItemKey) != null)
                    sum += gearItems.get(gearItemKey).getItemLevel();
            }
            else
            {
                Log.e(TAG, "Ignoring " + gearItemKey);
            }
        }

        this.ilvl = sum / 13;

        Log.e(TAG, this.ilvl + "");
    }

    public boolean isLoaded()
    {
        return loaded;
    }

    public static String[] getGearItemKeys()
    {
        return GEAR_ITEM_KEYS;
    }

    public ClassJob getActiveClassJob()
    {
        return activeClassJob;
    }

    public void setActiveClassJob(ClassJob activeClassJob)
    {
        this.activeClassJob = activeClassJob;
    }

    public ArrayList<ClassJob> getClassJobs()
    {
        return classJobs;
    }

    public void setClassJobs(ArrayList<ClassJob> classJobs)
    {
        this.classJobs = classJobs;
    }

    public Map<String, GearItem> getGearItems()
    {
        return gearItems;
    }

    public void setGearItems(Map<String, GearItem> gearItems)
    {
        this.gearItems = gearItems;
    }

    public int getIlvl()
    {
        return ilvl;
    }

    public void setIlvl(int ilvl)
    {
        this.ilvl = ilvl;
    }

    public ArrayList<CharacterAttribute> getAttributes()
    {
        return attributes;
    }

    public void setAttributes(ArrayList<CharacterAttribute> attributes)
    {
        this.attributes = attributes;
    }

    public CharacterGrandCompany getGrandCompany()
    {
        return grandCompany;
    }

    public void setGrandCompany(CharacterGrandCompany grandCompany)
    {
        this.grandCompany = grandCompany;
    }

    public CharacterGuardianDeity getGuardianDeity()
    {
        return guardianDeity;
    }

    public void setGuardianDeity(CharacterGuardianDeity guardianDeity)
    {
        this.guardianDeity = guardianDeity;
    }
}