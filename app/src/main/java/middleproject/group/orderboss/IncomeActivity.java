package middleproject.group.orderboss;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cunoraz.gifview.library.GifView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class IncomeActivity extends AppCompatActivity {

    private OrderMealDB orderMealDB;

    private BarChart mChart;
    private String chartDataSetLegend = "每月營業額";

    private ListView listView;
    private ArrayList<String> allMonth;
    private IncomeListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_income);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        orderMealDB = new OrderMealDB(getApplicationContext());

        mChart = (BarChart) findViewById(R.id.bar_chart1);
        listView = (ListView) findViewById(R.id.listView3);

        setBarChart();
        HashMap<String, Integer> chartDataMap = calEachMonth();
        setChartData(chartDataMap);

        allMonth = new ArrayList<>();
        allMonth.add("每月營業額");
        for (String s : chartDataMap.keySet()){
            allMonth.add(s);
        }
        allMonth = sort(allMonth);
        adapter = new IncomeListAdapter(this, allMonth);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setBarChart(){
        mChart.setDrawBarShadow(false);
        mChart.setDrawValueAboveBar(true);
        mChart.setDescription("");
        mChart.setMaxVisibleValueCount(60);
        mChart.setPinchZoom(false);
        mChart.setDrawGridBackground(false);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setSpaceBetweenLabels(2);

        YAxisValueFormatter custom = new MyYAxisValueFormatter();

        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setLabelCount(8, false);
        leftAxis.setValueFormatter(custom);
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinValue(0f); // this replaces setStartAtZero(true)

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setDrawGridLines(false);
        rightAxis.setLabelCount(8, false);
        rightAxis.setValueFormatter(custom);
        rightAxis.setSpaceTop(15f);
        rightAxis.setAxisMinValue(0f);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
        l.setForm(Legend.LegendForm.SQUARE);
        l.setFormSize(9f);
        l.setTextSize(11f);
        l.setXEntrySpace(4f);
    }

    private void setChartData(HashMap<String, Integer> map){
        ArrayList<String> xVals = new ArrayList<String>();
        for (String s : map.keySet()){
            xVals.add(s);
        }
        xVals = sort(xVals);

        ArrayList<BarEntry> yVals1 = new ArrayList<BarEntry>();
        for (int i=0; i<xVals.size(); i++){
            float val = (float) map.get(xVals.get(i));
            yVals1.add(new BarEntry(val, i));
        }

        BarDataSet set1;
        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (BarDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setYVals(yVals1);
            set1.setLabel(chartDataSetLegend);
            mChart.getData().setXVals(xVals);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        }
        else {
            set1 = new BarDataSet(yVals1, chartDataSetLegend);
            set1.setBarSpacePercent(35f);
            set1.setColors(ColorTemplate.MATERIAL_COLORS);

            ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
            dataSets.add(set1);

            BarData data = new BarData(xVals, dataSets);
            data.setValueTextSize(10f);

            mChart.setData(data);
        }
    }

    private HashMap calEachMonth(){
        ArrayList<OrderMealItem> itemList = orderMealDB.getAllRecord();
        HashSet<String> dateSet = new HashSet<String>();
        for (OrderMealItem i : itemList){
            dateSet.add(i.getDate());
        }

        HashMap<String, Integer> monthMap = new HashMap<String, Integer>();
        for (String s : dateSet){
            String month = s.substring(0, 7);
            int totalMonthMoney = 0;

            for (int i=0; i<itemList.size(); i++){
                if (itemList.get(i).getDate().matches(month + ".*")){
                    totalMonthMoney += itemList.get(i).getPrice();
                }
            }
            monthMap.put(month, totalMonthMoney);
        }
        return monthMap;
    }

    private HashMap calEachDate(String month){
        ArrayList<OrderMealItem> itemList = orderMealDB.getAllRecord();
        HashSet<String> dateSet = new HashSet<String>();
        for (OrderMealItem i : itemList){
            if (i.getDate().matches(month+".*"))
                dateSet.add(i.getDate());
        }

        HashMap<String, Integer> dateMap = new HashMap<String, Integer>();
        for (String s : dateSet){
            int daytotalMoney = 0;
            for (int i=0; i<itemList.size(); i++){
                if (itemList.get(i).getDate().equals(s)){
                    daytotalMoney += itemList.get(i).getPrice();
                }
            }
            dateMap.put(s, daytotalMoney);
        }
        return dateMap;
    }

    private ArrayList sort(ArrayList<String> list){
        int start = 0;

        if (list.isEmpty()) return list;
        if (list.get(0).equals("每月營業額")) start = 1;

        for (int i=start; i<list.size(); i++){
            int n1 = transformSortNum(list.get(i));
            for (int j=i+1; j<list.size(); j++){
                int n2 = transformSortNum(list.get(j));

                if (n1 > n2){
                    String s = list.get(i);
                    list.set(i, list.get(j));
                    list.set(j, s);
                }
            }
        }
        return list;
    }

    private int transformSortNum(String str){
        String[] s = str.split("-");
        int n = Integer.valueOf(s[s.length-1]);
        return n;
    }

    class IncomeListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
        private ArrayList<String> arrayList;
        private LayoutInflater myInflater;

        public IncomeListAdapter(Context c, ArrayList<String> arrayList) {
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            convertView = myInflater.inflate(R.layout.income_month_list, null);

            TextView textView = (TextView) convertView.findViewById(R.id.textView13);
            textView.setText(arrayList.get(position)+"");

            return convertView;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            chartDataSetLegend = arrayList.get(position);
            if (position == 0) setChartData(calEachMonth());
            else setChartData(calEachDate(chartDataSetLegend));
            mChart.invalidate();
        }
    }
}
