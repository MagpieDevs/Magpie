package com.example.magpie.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import static android.widget.Toast.LENGTH_SHORT;
import static com.example.magpie.app.R.layout.temp_vpn_start;

/**
 * Created by mattpatera on 3/3/17.
 */

public class MagpieVPN extends AppCompatActivity {

    private static Context context;
    public static String fileStatic_str = "";

    private static final String TAG = MagpieVPN.class.getSimpleName();

    // Hexadecimal representation of a byte -> 00001111
    private static final int VPN_REQUEST_CODE = 0x0F;

    private boolean isWaitingForVPN;

    // Receives broadcast from MagpieVPNService (line )
    private BroadcastReceiver vpnStateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // if the received broadcast is from
            if (MagpieVPNService.BROADCAST_VPN_STATE.equals(intent.getAction())) {
                if (intent.getBooleanExtra("running", false))
                    isWaitingForVPN = false;
                Log.i(TAG, "Broadcast Recieved");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MagpieVPN.context = getApplicationContext();

        setContentView(temp_vpn_start);

        // Grab switch element from view and cast it to a switch.
        final Switch vpn_toggle = (Switch)findViewById(R.id.vpn_toggle);



        // attach switch listener
        if (vpn_toggle != null) {
            vpn_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        startVPN();
                        Toast.makeText(MagpieVPN.this, "Beginning packet capture", LENGTH_SHORT).show();
                    } else {
                        stopVPN();
                        Toast.makeText(MagpieVPN.this, "Halting packet capture", LENGTH_SHORT).show();

                        //Added intent to go to this activity when off(switch)
                        Intent intent1 = new Intent(MagpieVPN.this, DisplayFileContentsActivity.class);
                        startActivity(intent1);

                    }
                }
            });
        }
    }

    //create string for file contents can be accessible
    public String GetString(){
          //fileStatic_str = "dlkfasklfalkfalkfasldkjfdlakf";
        return fileStatic_str;
    }


    private void startVPN() {
        Log.i(TAG, "Starting MagpieVPN (MAGPIEVPN: 72)");
        Intent magpieVpnIntent = VpnService.prepare(this);
        if (magpieVpnIntent != null) {
            startActivityForResult(magpieVpnIntent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, RESULT_OK, null);
        }
    }

    // This must be an override because startActivityForResult calls it upon receiving the go-ahead
    // from the phone's "allow vpn connection" box.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VPN_REQUEST_CODE && resultCode == RESULT_OK)
        {
            isWaitingForVPN = true;
            startService(new Intent(this, MagpieVPNService.class));
            Log.i(TAG, "VPNIntent creation successful (MAGPIEVPN: 89)");
        }
    }
//Need to change string to text view
    private void stopVPN() {
        try {
            File outfile = new File(context.getFilesDir(), "outfile.txt");
            BufferedReader in = new BufferedReader(new FileReader(outfile));
            String fileContents = readFile(in);

            //add static string for textview
            //fileStatic_str = "Dummy String";
            fileStatic_str = fileContents;
            Log.i(TAG, fileContents);



        } catch (FileNotFoundException e) {
            return;
        }


    }

    public String readFile(BufferedReader br)
    {
        StringBuilder sb = new StringBuilder();
        String tmp;

        try
        {
            while((tmp = br.readLine()) != null)
                sb.append(tmp).append("\n");
        }
        catch(IOException e)
        {
            return null;
        }



        return sb.toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if(id == R.id.action_about_us){
            Intent intent = new Intent(MagpieVPN.this, AboutUsActivity.class);
            startActivity(intent);

        }

        else if(id == R.id.action_helpInfo){
            Intent intent = new Intent(MagpieVPN.this, HelpInfoActivity.class);
            startActivity(intent);

        }

        else if(id == R.id.action_Privacypolicy){
            Intent intent = new Intent(MagpieVPN.this, PrivacyPolicyActivity.class);
            startActivity(intent);

        }

        //invokes email clients for email services
        else if(id == R.id.action_startemail_client){
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            //Intent emailIntent = getPackageManager().getLaunchIntentForPackage("com.android.email");
            //emailIntent.setData(Uri.parse("mailto:" + to));

            //SetType for email is message/rfc822
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"youremail@xyz"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Magpie Application");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "File from Magpie Application");
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));

        }



        return super.onOptionsItemSelected(item);
    }
}
