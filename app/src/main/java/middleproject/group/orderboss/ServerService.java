package middleproject.group.orderboss;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ServerService extends Service {

    private MenuDB menuDB;

    private OrderMealDB orderMealDB;

    private ServerThread serverThread;

    public ServerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        menuDB = new MenuDB(getApplicationContext());
        orderMealDB = new OrderMealDB(getApplicationContext());

        serverThread = new ServerThread();
        serverThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        serverThread.closeServer();
        menuDB.close();
        orderMealDB.close();
    }

    class ServerThread extends Thread {

        public static final int PORT = 1212;

        private ServerSocket serverSocket;

        private boolean serverFlag = true;

        public void closeServer(){
            try {
                serverFlag = false;
                serverSocket.close();
            }catch (Exception e){
                e.printStackTrace();
                Log.e("Service Close", "close error");
            }
        }


        @Override
        public void run() {
            super.run();
            try{
                serverSocket = new ServerSocket(PORT);
                while(true){
                    if(!serverFlag) return;

                    Socket socket = serverSocket.accept();
                    new SocketThread(socket).start();
                }

            }catch (Exception e){
                e.printStackTrace();
                Log.e("Service Socket", "error");
            }

        }
    }

    class SocketThread extends Thread {

        private Socket socket;

        public SocketThread(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try{
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                int action = dis.readInt();
                switch (action){
                    case 1:
                        ArrayList<MenuDBItem> items = menuDB.getAll();
                        int amount = items.size();

                        dos.writeInt(amount);
                        dos.flush();

                        for(int i=0; i<amount; ++i){
                            bw.write(items.get(i).getName() + " " + items.get(i).getSpecies() + " " + items.get(i).getPrice() + "\n");
                            bw.flush();
                        }

                        if (items.size() == 0) break;

                        String maxMeal = "";
                        for (int n=0; n<3; n++){
                            if (items.size() == 0) break;
                            int max = items.get(0).getSalesNum();
                            for (int i=1; i<items.size(); i++){
                                if (max < items.get(i).getSalesNum())
                                    max = items.get(i).getSalesNum();
                            }
                            for (int i=0; i<items.size(); i++){
                                if (max == items.get(i).getSalesNum()){
                                    maxMeal += items.get(i).getName() + " ";
                                    items.remove(i);
                                }
                            }
                        }
                        maxMeal.trim();
                        bw.write(maxMeal + "\n");
                        bw.flush();
                        break;
                    case 2:         // 餐點改用逗號(,)分開
                        OrderMealItem item = new OrderMealItem();
                        ArrayList<OrderMealItem> allOrderOutside = orderMealDB.getTodayOutside();

                        int table = dis.readInt();
                        int outsideNum = -1*(allOrderOutside.size()+1);
                        if (table == -1){
                            item.setTable(outsideNum);
                        }
                        else item.setTable(table);

                        int p = dis.readInt();
                        item.setPrice(p);

                        String[] data = br.readLine().split(" ");
                        String s = "";
                        for(int i=0; i<data.length; i++){
                            s += (i!=0 ? ",":"") + data[i];
                        }
                        item.setOrderItem(s);
                        item.setSend(0);

                        if (table == -1){
                            dos.writeInt(outsideNum*-1);
                            dos.flush();
                        }

                        SimpleDateFormat dateStringFormat = new SimpleDateFormat( "yyyy-MM-dd" );
                        Date date=new Date();
                        String currentDate = dateStringFormat.format(date);
                        item.setDate(currentDate);

                        item = orderMealDB.insert(item);

                        Intent intent = new Intent();
                        intent.setAction(MainActivity.MENU_UPDATE_ACTION);
                        intent.putExtra("ItemId", item.getId());
                        sendBroadcast(intent);
                        break;
                    case 3:
                        int tableNum = dis.readInt();

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                        long[] vibrate_effect = {1000, 500, 1000, 400, 1000, 300, 1000, 200, 1000, 100};
                        builder.setSmallIcon(R.drawable.clock)
                                .setWhen(System.currentTimeMillis())
                                .setContentTitle(getResources().getString(R.string.notify_title))
                                .setContentText(tableNum + " " + getResources().getString(R.string.notify_content))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setVibrate(vibrate_effect)
                                .setPriority(Notification.PRIORITY_DEFAULT)
                                .setAutoCancel(true);

                        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        nm.notify(0, builder.build());
                        break;
                    case 4:
                        String mealName = br.readLine();Log.d("sendimg", mealName);
                        for (MenuDBItem m : menuDB.getAll()){
                            if (m.getName().equals(mealName)){
                                Bitmap bitmap = BitmapFactory.decodeFile(MenuActivity.PICTURE_DIR + m.getPictureName());
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                byte[] bytes = baos.toByteArray();

                                dos.writeInt(bytes.length);
                                dos.flush();

                                dos.write(bytes, 0, bytes.length);
                                dos.flush();
                                break;
                            }
                        }
                        break;
                }

                socket.close();
            } catch (Exception e){
                e.printStackTrace();
                Log.e("Service Socket", "inner error");
            }
        }
    }
}
