package middleproject.group.orderboss;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

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
                Log.e("Service Close", e.getMessage());
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
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    int action = dis.readInt();
                    switch (action){
                        case 1:
                            ArrayList<MenuDBItem> items = menuDB.getAll();
                            int amount = items.size();

                            dos.writeInt(amount);
                            dos.flush();

                            for(int i=0; i<amount; ++i){
                                dos.write(items.get(i).getName().getBytes());
                                dos.writeInt(items.get(i).getPrice());
                                dos.flush();
                            }
                            break;
                        case 2:
                            OrderMealItem item = new OrderMealItem();
                            item.setTable(dis.readInt());

                            byte[] bytes = null;
                            dis.read(bytes);
                            item.setOrderItem(bytes.toString());

                            item.setPrice(dis.readInt());
                            item.setSend(0);

                            orderMealDB.insert(item);

                            Intent intent = new Intent();
                            intent.setAction(MainActivity.MENU_UPDATE_ACTION);
                            sendBroadcast(intent);
                            break;
                        case 3:
                            int tableNum = dis.readInt();

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
                            long[] vibrate_effect = {1000, 500, 1000, 400, 1000, 300, 1000, 200, 1000, 100};
                            builder.setSmallIcon(R.drawable.clock)
                                    .setWhen(System.currentTimeMillis())
                                    .setContentTitle(tableNum + " " + getResources().getString(R.string.notify_title))
                                    .setContentText(tableNum + " " + getResources().getString(R.string.notify_title))
                                    .setDefaults(Notification.DEFAULT_ALL)
                                    .setVibrate(vibrate_effect)
                                    .setPriority(Notification.PRIORITY_DEFAULT)
                                    .setAutoCancel(true);

                            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            nm.notify(0, builder.build());
                            break;
                    }

                    socket.close();
                }

            }catch (Exception e){
                Log.e("Service Socket", e.getMessage());
            }

        }
    }
}
