package middleproject.group.orderboss;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.gifview.library.GifView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class SalesActivity extends AppCompatActivity {

    private PieChart mChart;
    private ListView listView;

    private MenuDB menuDB;
    private SalesNumAdapter adapter;

    ArrayList<MenuDBItem> itemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        menuDB = new MenuDB(getApplicationContext());

        mChart = (PieChart) findViewById(R.id.chart1);
        listView = (ListView) findViewById(R.id.listView2);
        adapter = new SalesNumAdapter(this, menuDB.getAll());
        listView.setAdapter(adapter);

        setPieChart();
        setPieChartData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setPieChart() {
        mChart.setUsePercentValues(true);
        mChart.setDescription("");
        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.setDragDecelerationFrictionCoef(0.95f);
        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);
        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);
        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);
        mChart.setDrawCenterText(true);
        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);
        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
    }

    private void setPieChartData(){
        itemsList = menuDB.getAll();
        ArrayList<Entry> yVals = new ArrayList<Entry>();
        ArrayList<String> xVals = new ArrayList<String>();

        for (int i=0; i<itemsList.size(); i++){
            if (itemsList.get(i).getSalesNum() == 0) continue;
            yVals.add(new Entry((float) itemsList.get(i).getSalesNum(), i));
        }
        for (int i=0; i<itemsList.size(); i++){
            if (itemsList.get(i).getSalesNum() == 0) continue;
            xVals.add(itemsList.get(i).getName());
        }

        PieDataSet dataSet = new PieDataSet(yVals, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.BLACK);

        mChart.setData(data);
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    class SalesNumAdapter extends BaseAdapter {
        private ArrayList<MenuDBItem> arrayList;
        private LayoutInflater myInflater;

        public SalesNumAdapter(Context c, ArrayList<MenuDBItem> arrayList) {
            myInflater = LayoutInflater.from(c);
            this.arrayList = arrayList;
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = myInflater.inflate(R.layout.salse_list, null);

            GifView gifView = (GifView) convertView.findViewById(R.id.gif1);
            TextView tv_name = (TextView) convertView.findViewById(R.id.textView11);
            TextView tv_num = (TextView) convertView.findViewById(R.id.textView12);

            if (arrayList.get(position).getSalesNum() == getBestSale()){
                gifView.setVisibility(View.VISIBLE);
            }
            else gifView.setVisibility(View.GONE);

            tv_name.setText(arrayList.get(position).getName() + "");
            tv_num.setText(arrayList.get(position).getSalesNum() + "");

            return convertView;
        }

        private int getBestSale(){
            int most = 0;
            for (MenuDBItem i : arrayList){
                if(i.getSalesNum() > most)
                    most = i.getSalesNum();
            }
            return most;
        }

    }
}
