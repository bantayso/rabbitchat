package com.bantayso.rabbitchat.rabbitchat;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MessageConsumer mConsumer;
    private TextView mOutput;
    private String QUEUE_NAME = "myqueue";
    private String EXCHANGE_NAME = "amq.fanout";//test
    private String message = "";
    private String name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final EditText etv1 = (EditText) findViewById(R.id.out3);
        etv1.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN)
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    name = etv1.getText().toString();
                    etv1.setText("");
                    etv1.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });

        final EditText etv = (EditText) findViewById(R.id.out2);
        etv.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                // If the event is a key-down event on the "enter" button
                if ((arg2.getAction() == KeyEvent.ACTION_DOWN)
                        && (arg1 == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    message = name + ": " + etv.getText().toString();
                    new send().execute(message);
                    etv.setText("");
                    return true;
                }
                return false;
            }
        });

        // The output TextView we'll use to display messages
        mOutput = (TextView) findViewById(R.id.output);

        // Create the consumer
        mConsumer = new MessageConsumer("192.168.1.4", EXCHANGE_NAME, "fanout");
        new consumerconnect().execute();
        // register for messages
        mConsumer.setOnReceiveMessageHandler(new MessageConsumer.OnReceiveMessageHandler() {

            public void onReceiveMessage(byte[] message) {
                String text = "";
                try {
                    text = new String(message, "UTF8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                mOutput.append("\n" + text);
            }
        });

    }

    private class send extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... Message) {
        try {

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("192.168.1.4");

            // my internet connection is a bit restrictive so I have use an
            // external server
            // which has RabbitMQ installed on it. So I use "setUsername"
            // and "setPassword"
            factory.setUsername("cong");
            factory.setPassword("123456");
            //factory.setVirtualHost("karthi");
            factory.setPort(5672);
            System.out.println(""+factory.getHost()+factory.getPort()+factory.getRequestedHeartbeat()+factory.getUsername());
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout", true);
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String tempstr = "";
            for (int i = 0; i < Message.length; i++)
                tempstr += Message[i];

            channel.basicPublish(EXCHANGE_NAME, QUEUE_NAME, null,
                    tempstr.getBytes());

            channel.close();

            connection.close();

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        // TODO Auto-generated method stub
        return null;
    }

    }

    private class consumerconnect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {



                // Connect to broker
                mConsumer.connectToRabbitMQ();



            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        new consumerconnect().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mConsumer.dispose();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
