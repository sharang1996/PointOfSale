package com.example.sharang.pointofsale;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private TextView mNameText;
    private TextView mQuantityText;
    private TextView mDeliveryDateText;
    private Item mCurrentItem;
    Date date;
    private ArrayList<Item> mItems = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNameText=(TextView)findViewById(R.id.name_text);
        mQuantityText=(TextView)findViewById(R.id.quantity_text);
        mDeliveryDateText=(TextView)findViewById(R.id.date_text);


        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               addEditItem(false);
            }
        });

        registerForContextMenu(mNameText );
    }

    private void addEditItem(final boolean isEditing) {
        DialogFragment df = new DialogFragment(){

            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add,null);
                builder.setView(view);
               //capture
                final EditText nameEditText=(EditText)view.findViewById(R.id.edit_name);
                final EditText quantityEditText=(EditText)view.findViewById(R.id.edit_quantity);
                final CalendarView deliveryDateView=(CalendarView) view.findViewById(R.id.calender_view);
                deliveryDateView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                    @Override
                    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.clear();
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        date = calendar.getTime();
                    }
                });
                if(isEditing)
                {
                    nameEditText.setText(mCurrentItem.getName());
                    quantityEditText.setText(String.valueOf(mCurrentItem.getQuantity()));
                    deliveryDateView.setDate(mCurrentItem.getDeliveryDateTime());
                }
                //buttons
                builder.setNegativeButton(android.R.string.cancel,null);

                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = nameEditText.getText().toString();
                        int quantity = Integer.parseInt(quantityEditText.getText().toString());

                        if(isEditing){
                            mCurrentItem.setName(name);
                            mCurrentItem.setQuantity(quantity);
                            mCurrentItem.setDeliveryDate(date);
                        }
                        else{
                            mCurrentItem=new Item(name,quantity,date);
                            mItems.add(mCurrentItem);
                        }
                        showCurrentItem();
                    }
                });
                return builder.create();
            }
        };
        df.show(getSupportFragmentManager(),"add");
    }

    public void showCurrentItem(){
        mNameText.setText(mCurrentItem.getName());
        mQuantityText.setText(getString(R.string.quantity_format,mCurrentItem.getQuantity()));
        mDeliveryDateText.setText(getString(R.string.date_format,mCurrentItem.getDeliveryDateString()));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_main,menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.menu_context_edit:
                addEditItem(true);
                return true;
            case R.id.menu_context_remove:
                mItems.remove(mCurrentItem);
                mCurrentItem=new Item();
                showCurrentItem();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_reset:
                if(mCurrentItem==null) mCurrentItem=Item.getDefaultItem();
                final Item temp = mCurrentItem;
                mCurrentItem=new Item();
                showCurrentItem();
                Snackbar.make(findViewById(R.id.coordinator_layout ), "Item Cleared", Snackbar.LENGTH_LONG)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentItem=temp;
                                Snackbar.make(findViewById(R.id.coordinator_layout),"Item restored",Snackbar.LENGTH_LONG).show();
                                showCurrentItem();
                            }
                        }).show();
                return true;

            case R.id.action_search:
                showSearchDialog();
                return true;

            case R.id.action_clear:
                clearAll();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                return true;

            default: return super.onOptionsItemSelected(item);
        }


    }

    private void clearAll() {
        DialogFragment df = new DialogFragment(){
            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Remove");
                builder.setMessage("Are you sure you want to remove all items?");
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mItems.clear();
                        mCurrentItem=new Item();
                        showCurrentItem();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,null);
                return builder.create();
            }
        };
        df.show(getSupportFragmentManager(),"clear");
    }

    private void showSearchDialog() {
        DialogFragment df = new DialogFragment(){

            @NonNull
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.search_dialog_title));
                builder.setItems(getNames(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCurrentItem=mItems.get(which);
                        showCurrentItem();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel,null);
                return builder.create();
            }
        };

        df.show(getSupportFragmentManager(),"search");
    }

    private String[] getNames() {
        String []names = new String[mItems.size()];
        for(int i=0;i<mItems.size();i++){
            names[i]=mItems.get(i).getName();
        }
        return names;
    }
}
