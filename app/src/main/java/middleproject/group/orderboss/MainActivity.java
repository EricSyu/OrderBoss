package middleproject.group.orderboss;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
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

    private static final int REQUEST_LOCATION = 0;

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
        directory = contextWrapper.getDir("OrderBoss",MODE_APPEND);

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

        init_nav_header();

        orderMealDB = new OrderMealDB(getApplicationContext());

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

        startService(new Intent(this, ServerService.class));

        requestPermission();
    }

    public void requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    finish();
                }
                break;
        }
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
        else if (id == R.id.nav_sales){
            Intent intent = new Intent(this, SalesActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_income){
            Intent intent = new Intent(this, IncomeActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    class UIReceiver extends BroadcastReceiver {

        public UIReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            mAdapter.setItems(orderMealDB.getNotSend());
            mAdapter.notifyDataSetChanged();
        }
    }

}
