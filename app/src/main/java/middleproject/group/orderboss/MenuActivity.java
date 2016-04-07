package middleproject.group.orderboss;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity implements DialogInterface.OnClickListener, View.OnClickListener {

    private MenuDB menuDB;

    private FloatingActionButton fab;

    private EditText editText_name, editText_price;

    private ListView listView;
    private MyAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        menuDB = new MenuDB(getApplicationContext());
        myAdapter = new MyAdapter(this, menuDB.getAll());

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(myAdapter);
        listView.setOnItemLongClickListener(myAdapter);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.fab){
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.add_menu_dialog_title))
                    .setView(R.layout.add_menu_dialog)
                    .setPositiveButton(getResources().getString(R.string.shop_name_dialog_ok), this)
                    .show();
            editText_name = (EditText) dialog.findViewById(R.id.editText2);
            editText_price = (EditText) dialog.findViewById(R.id.editText3);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == Dialog.BUTTON_POSITIVE){
            try{
                String name = editText_name.getText().toString();
                int price = Integer.valueOf(editText_price.getText().toString());
                MenuDBItem item = new MenuDBItem(name, price);
                menuDB.insert(item);

                if(listView.getCount() == 0){
                    Intent intent = new Intent(this, MenuActivity.class);
                    MenuActivity.this.startActivity(intent);

                    finish();
                }
                else {
                    myAdapter.notifyDataSetInvalidated();
                }
            }catch (Exception e){
                Toast.makeText(MenuActivity.this, "請輸入正確價格", Toast.LENGTH_SHORT).show();
            }

        }
    }

    class MyAdapter extends BaseAdapter implements AdapterView.OnItemLongClickListener {
        private LayoutInflater myInflater;
        private ArrayList<MenuDBItem> items;

        public MyAdapter(Context c, ArrayList<MenuDBItem> items){
            myInflater = LayoutInflater.from(c);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            items = menuDB.getAll();
            convertView = myInflater.inflate(R.layout.menu_list, null);
            TextView textView_name = (TextView) convertView.findViewById(R.id.textView3);
            TextView textView_price = (TextView) convertView.findViewById(R.id.textView4);

            for(int i=0; i<items.size(); i++){
                textView_name.setText(items.get(position).getName()+"");
                textView_price.setText(items.get(position).getPrice()+" 元");
            }
            return convertView;
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            menuDB.delete(items.get(position).getId());
            items.remove(position);
            myAdapter.notifyDataSetInvalidated();
            return false;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }
    }
}
