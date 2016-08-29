package middleproject.group.orderboss;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import me.nereo.multi_image_selector.MultiImageSelector;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class MenuActivity extends AppCompatActivity implements DialogInterface.OnClickListener, View.OnClickListener {

    private final static int REQUEST_IMAGE = 11;
    public final static int CONTEXT_MENU_DELETE_ID = 111;
    public static String PICTURE_DIR = "/sdcard/OrderBoss/";

    private MenuDB menuDB;

    private FloatingActionMenu fab;
    private FloatingActionButton fab_addmeal, fab_addspecies;

    private EditText editText_name, editText_price, editText_species;
    private Button btn_addpic;
    private ImageView iv_pic;

    private Spinner spinner;

    private ExpandableListView listView;
    private MyExpandableListAdapter myAdapter;

    private File directory, file;
    private ArrayList<String> speciesList;

    private HashMap<String, ArrayList<MenuDBItem>> menuDataMap;

    private String orgPicPath = "", picName = "", oldPicName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        menuDB = new MenuDB(getApplicationContext());

        fab = (FloatingActionMenu) findViewById(R.id.fab);
        fab_addmeal = (FloatingActionButton) findViewById(R.id.fab_addmeal);
        fab_addspecies = (FloatingActionButton) findViewById(R.id.fab_addspecies);
        fab_addmeal.setOnClickListener(this);
        fab_addspecies.setOnClickListener(this);

        speciesList = new ArrayList<>();
        retrieveSpecies();

        collectMenuData();
        myAdapter = new MyExpandableListAdapter(this, speciesList, menuDataMap);
        listView = (ExpandableListView) findViewById(R.id.listView);
        listView.setAdapter(myAdapter);
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        fab.close(true);
                        fab.hideMenuButton(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        fab.showMenuButton(true);
                        break;
                }
                return false;
            }
        });
        registerForContextMenu(listView);
        makePicDir();
    }

    private void makePicDir(){
        File dir = new File(PICTURE_DIR);
        if (!dir.exists()) dir.mkdir();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listView){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_contextmenu, menu);

            ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            int type = ExpandableListView.getPackedPositionType(info.packedPosition);
            if (type == 1){
                menu.add(0, CONTEXT_MENU_DELETE_ID, 1, "刪除");
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        final int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        switch (item.getItemId()){
            case R.id.modify_menu:
                if (type == 0){
                    final String oldSpecies =  speciesList.get(group);
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("修改餐點種類")
                            .setView(R.layout.add_species_dialog)
                            .setPositiveButton(getResources().getString(R.string.shop_name_dialog_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        String newSpecies =  editText_species.getText().toString();

                                        if (oldSpecies.equals(newSpecies)) return;

                                        speciesList.set(group, newSpecies);

                                        ArrayList<MenuDBItem> items = menuDB.getAll();
                                        for (int i=0; i<items.size(); i++){
                                            if (items.get(i).getSpecies().equals(oldSpecies)){
                                                items.get(i).setSpecies(newSpecies);
                                                menuDB.update(items.get(i));
                                            }
                                        }

                                        file.delete();
                                        file.createNewFile();
                                        FileWriter writer = new FileWriter(file, true);
                                        for (int i=0; i<speciesList.size(); i++){
                                            writer.write(speciesList.get(i) + "\n");
                                        }
                                        writer.close();
                                    } catch (Exception e) {
                                        Toast.makeText(MenuActivity.this, "寫入檔案失敗", Toast.LENGTH_SHORT).show();
                                    }

                                    collectMenuData();
                                    myAdapter.setSpeciesList(speciesList);
                                    myAdapter.setMenuDataMap(menuDataMap);
                                    myAdapter.notifyDataSetChanged();

                                    Toast.makeText(MenuActivity.this, "餐點種類已修改完成", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                    editText_species = (EditText) dialog.findViewById(R.id.editText_species);
                    editText_species.setText(speciesList.get(group));
                }
                else if (type == 1){
                    MenuDBItem m = menuDataMap.get(speciesList.get(group)).get(child);
                    openModifyMealDialog(m);
                }
                break;
            case CONTEXT_MENU_DELETE_ID:
                if (type == 1){
                    MenuDBItem m = menuDataMap.get(speciesList.get(group)).get(child);
                    menuDB.delete(m.getId());

                    File deleteFile = new File(PICTURE_DIR + m.getPictureName());
                    deleteFile.delete();

                    collectMenuData();
                    myAdapter.setMenuDataMap(menuDataMap);
                    myAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "餐點已刪除", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                return super.onContextItemSelected(item);
        }
        return super.onContextItemSelected(item);
    }

    private void collectMenuData(){
        menuDataMap = new HashMap<String, ArrayList<MenuDBItem>>();

        for (String s : speciesList){
            ArrayList<MenuDBItem> list = new ArrayList<>();
            for (MenuDBItem meal : menuDB.getAll()){
                if (meal.getSpecies().equals(s)){
                    list.add(meal);
                }
            }
            menuDataMap.put(s, list);
        }
    }

    private void retrieveSpecies(){
        ContextWrapper contextWrapper = new ContextWrapper(getApplicationContext());
        directory = contextWrapper.getDir("OrderBoss",MODE_APPEND);

        if(fileIsExist()){
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String s = "";
                while ((s = reader.readLine()) != null){
                    speciesList.add(s);
                }
                reader.close();
            }catch (Exception e){
                Toast.makeText(this, "讀取檔案錯誤", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean fileIsExist(){
        try{
            file=new File(directory,"SpeciesFile.txt");
            if(!file.exists()){
                return false;
            }

        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void openModifyMealDialog(final MenuDBItem item){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("修改餐點")
                .setView(R.layout.add_menu_dialog)
                .setPositiveButton(getResources().getString(R.string.shop_name_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try{
                            int price = Integer.valueOf(editText_price.getText().toString());
                            String species = spinner.getSelectedItem().toString();

                            item.setPrice(price);
                            item.setSpecies(species);
                            if (!picName.equals("") && !orgPicPath.equals(""))
                                item.setPictureName(picName);

                            menuDB.update(item);

                            collectMenuData();
                            myAdapter.setMenuDataMap(menuDataMap);
                            myAdapter.notifyDataSetChanged();

                            Toast.makeText(MenuActivity.this, "餐點已修改完成", Toast.LENGTH_SHORT).show();
                            if (picName.equals("") && orgPicPath.equals("")) return;

                            new File(PICTURE_DIR + oldPicName).delete();

                            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(orgPicPath));
                            FileOutputStream out = new FileOutputStream(PICTURE_DIR + picName);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
                        }catch (Exception e){
                            Toast.makeText(MenuActivity.this, "餐點修改錯誤", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        editText_name = (EditText) dialog.findViewById(R.id.editText2);
        editText_price = (EditText) dialog.findViewById(R.id.editText3);
        spinner = (Spinner) dialog.findViewById(R.id.spinner);
        btn_addpic = (Button) dialog.findViewById(R.id.button);
        iv_pic = (ImageView) dialog.findViewById(R.id.imageView);
        ArrayAdapter<String> speciesListAdapter = new ArrayAdapter<String>(MenuActivity.this, android.R.layout.simple_spinner_dropdown_item, speciesList);
        spinner.setAdapter(speciesListAdapter);
        btn_addpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiImageSelector.create(MenuActivity.this)
                        .showCamera(true)
                        .single()
                        .start(MenuActivity.this, REQUEST_IMAGE);
            }
        });
        iv_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiImageSelector.create(MenuActivity.this)
                        .showCamera(true)
                        .single()
                        .start(MenuActivity.this, REQUEST_IMAGE);
            }
        });

        editText_name.setText(item.getName() + "");
        editText_name.setEnabled(false);
        editText_price.setText(item.getPrice()+"");
        for (int i=0; i<speciesList.size(); i++){
            if (item.getSpecies().equals(speciesList.get(i))){
                spinner.setSelection(i, true);
                break;
            }
        }
        if (!item.getPictureName().isEmpty()){
            btn_addpic.setVisibility(View.GONE);
            iv_pic.setVisibility(View.VISIBLE);
            Picasso.with(this).load("file://" + PICTURE_DIR + item.getPictureName()).resize(500, 600).into(iv_pic);
            oldPicName = item.getPictureName();
        }

        picName = "";
        orgPicPath = "";
    }

    private void openAddMealDialog(){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.add_menu_dialog_title))
                .setView(R.layout.add_menu_dialog)
                .setPositiveButton(getResources().getString(R.string.shop_name_dialog_ok), this)
                .setNegativeButton("取消", null)
                .show();
        editText_name = (EditText) dialog.findViewById(R.id.editText2);
        editText_price = (EditText) dialog.findViewById(R.id.editText3);
        spinner = (Spinner) dialog.findViewById(R.id.spinner);
        btn_addpic = (Button) dialog.findViewById(R.id.button);
        iv_pic = (ImageView) dialog.findViewById(R.id.imageView);
        ArrayAdapter<String> speciesListAdapter = new ArrayAdapter<String>(MenuActivity.this, android.R.layout.simple_spinner_dropdown_item, speciesList);
        spinner.setAdapter(speciesListAdapter);
        btn_addpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiImageSelector.create(MenuActivity.this)
                        .showCamera(true)
                        .single()
                        .start(MenuActivity.this, REQUEST_IMAGE);
            }
        });
        iv_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiImageSelector.create(MenuActivity.this)
                        .showCamera(true)
                        .single()
                        .start(MenuActivity.this, REQUEST_IMAGE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.fab_addmeal) {
            fab.close(true);
            if (speciesList.size() == 0) {
                Toast.makeText(this, "請先新增餐點種類", Toast.LENGTH_SHORT).show();
                return;
            }
            openAddMealDialog();
        }
        else if (v.getId() == R.id.fab_addspecies) {
            fab.close(true);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("新增餐點種類")
                    .setView(R.layout.add_species_dialog)
                    .setPositiveButton(getResources().getString(R.string.shop_name_dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                FileWriter writer = new FileWriter(file, true);
                                writer.write(editText_species.getText().toString() + "\n");
                                speciesList.add(editText_species.getText().toString());
                                writer.close();
                            } catch (Exception e) {
                                Toast.makeText(MenuActivity.this, "寫入檔案失敗", Toast.LENGTH_SHORT).show();
                            }

                            collectMenuData();
                            myAdapter.setSpeciesList(speciesList);
                            myAdapter.setMenuDataMap(menuDataMap);
                            myAdapter.notifyDataSetChanged();
                        }
                    })
                    .show();
            editText_species = (EditText) dialog.findViewById(R.id.editText_species);
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
                for (MenuDBItem m : menuDB.getAll()){
                    if (m.getName().equals(name)){
                        Toast.makeText(MenuActivity.this, "餐點名稱重複", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                int price = Integer.valueOf(editText_price.getText().toString());
                String species = spinner.getSelectedItem().toString();
                MenuDBItem item = new MenuDBItem(name, price, species, 0, picName);
                menuDB.insert(item);

                if (!picName.equals("")){
                    Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(orgPicPath));
                    FileOutputStream out = new FileOutputStream(PICTURE_DIR + picName);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, out);
                }

                collectMenuData();
                myAdapter.setMenuDataMap(menuDataMap);
                myAdapter.notifyDataSetChanged();
            }catch (Exception e){
                Toast.makeText(MenuActivity.this, "請輸入正確價格", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE){
                List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);

                orgPicPath = path.get(0);
                String[] temp = orgPicPath.split("/");
                picName = temp[temp.length-1];

                btn_addpic.setVisibility(View.GONE);
                Picasso.with(this).load("file://" + orgPicPath).resize(500, 600).into(iv_pic);
                iv_pic.setVisibility(View.VISIBLE);
            }
        }
    }

    class MyExpandableListAdapter extends BaseExpandableListAdapter {
        private LayoutInflater myInflater;
        private ArrayList<String> speciesList;
        private HashMap<String, ArrayList<MenuDBItem>> menuDataMap;

        public MyExpandableListAdapter(Context c, ArrayList<String> speciesList, HashMap<String, ArrayList<MenuDBItem>> data){
            myInflater = LayoutInflater.from(c);
            this.speciesList = speciesList;
            this.menuDataMap = data;
        }

        @Override
        public int getGroupCount() {
            return speciesList.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return menuDataMap.get(speciesList.get(groupPosition)).size();
        }

        @Override
        public ArrayList getGroup(int groupPosition) {
            return speciesList;
        }

        @Override
        public MenuDBItem getChild(int groupPosition, int childPosition) {
            return menuDataMap.get(speciesList.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            convertView = myInflater.inflate(R.layout.menu_list_species, null);
            TextView textView_species = (TextView) convertView.findViewById(R.id.textView8);

            textView_species.setText(speciesList.get(groupPosition)+"");
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            convertView = myInflater.inflate(R.layout.menu_list, null);
            TextView textView_name = (TextView) convertView.findViewById(R.id.textView3);
            TextView textView_price = (TextView) convertView.findViewById(R.id.textView4);

            ArrayList<MenuDBItem> items = menuDataMap.get(speciesList.get(groupPosition));
            textView_name.setText(items.get(childPosition).getName()+"");
            textView_price.setText(items.get(childPosition).getPrice() + " 元");

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public void setSpeciesList(ArrayList<String> speciesList) {
            this.speciesList = speciesList;
        }

        public void setMenuDataMap(HashMap<String, ArrayList<MenuDBItem>> menuDataMap) {
            this.menuDataMap = menuDataMap;
        }

    }
}
