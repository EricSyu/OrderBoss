package middleproject.group.orderboss;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, DialogInterface.OnClickListener {

    public static final String MENU_UPDATE_ACTION = "main.receiver.UPDATE_MENU";

    private DBHelper dbhelper = null;
    private String database_name = "";

    private NavigationView navigationView;
    private EditText editText_shopName;
    private TextView textView_nav_title;

    private File directory;

    private OrderMealDB orderMealDB;

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemTouchHelper mItemTouchHelper;

    private UIReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        directory = contextWrapper.getDir("MyData",MODE_APPEND);

        if(fileIsExist()){
            try{
                File file = new File(directory, "shop_name.txt");
                FileInputStream fis = new FileInputStream(file);
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br = new BufferedReader(new InputStreamReader(dis));
                database_name = br.readLine();
                dis.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.shop_name_dialog_title))
                    .setView(R.layout.shop_name_dialog)
                    .setNegativeButton(getResources().getString(R.string.shop_name_dialog_exit), this)
                    .setPositiveButton(getResources().getString(R.string.shop_name_dialog_ok), this)
                    .setCancelable(false)
                    .show();
            editText_shopName = (EditText) dialog.findViewById(R.id.editText);
        }

        openDatabase();
        init_nav_header();

        orderMealDB = new OrderMealDB(getApplicationContext());orderMealDB.sample();

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(orderMealDB.getNotSend(), orderMealDB);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);

        registerBroadcastReceiver();
    }

    public void registerBroadcastReceiver(){
        receiver = new UIReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MENU_UPDATE_ACTION);
        registerReceiver(receiver, filter);
    }

    public void init_nav_header(){
        View header = navigationView.getHeaderView(0);
        textView_nav_title = (TextView) header.findViewById(R.id.nav_title);
        textView_nav_title.setText("" + database_name);
    }

    public boolean fileIsExist(){
        try{
            File file=new File(directory,"shop_name.txt");
            if(!file.exists()){
                return false;
            }

        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which)
        {
            case Dialog.BUTTON_NEGATIVE:
                finish();
                break;
            case Dialog.BUTTON_POSITIVE:
                try {
                    File file = new File(directory, "shop_name.txt");
                    FileOutputStream fos = new FileOutputStream(file,true);
                    database_name = editText_shopName.getText().toString();
                    fos.write(database_name.getBytes());
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        init_nav_header();
    }

    /*function to call for opening database*/
    private void openDatabase(){
        dbhelper = new DBHelper(getApplicationContext());
    }

    /*function to call for closing database*/
    private void closeDatabase(){
        dbhelper.close();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_checkout) {
            Intent intent = new Intent(this, CheckoutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDatabase();
        unregisterReceiver(receiver);
    }

    class UIReceiver extends BroadcastReceiver {

        public UIReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            mAdapter.notifyDataSetChanged();
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

}
