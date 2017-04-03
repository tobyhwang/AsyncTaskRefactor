package cs478.project5.toby.androidasynctask;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private EditText time;
    private TextView finalResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        time = (EditText) findViewById(R.id.in_time);
        button = (Button) findViewById(R.id.btn_run);
        finalResult = (TextView) findViewById(R.id.tv_result);

        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String sleepTime = time.getText().toString();
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(sleepTime);
            }
        });
    }


    //create a private async task funner
    private class AsyncTaskRunner extends AsyncTask<String, String, String>{

        private String resp;
        ProgressDialog progressDialog;

        /* This method contains the code which is executed before the background processing starts*/
        @Override
        protected void onPreExecute(){
            progressDialog = ProgressDialog.show(MainActivity.this, "ProgressDialog", "Wait for " + time.getText().toString() + " seconds");

        }

        /*This method contains the code which needs to be executed in background.  In
        this method we can send results multiple times to the UI thread by publishProgress
        method.  To notify that the background processing has been completed */
        @Override
        protected String doInBackground(String... params){
            publishProgress("Sleeping..."); //Calls onProgressUpdated
            try{
                int time = Integer.parseInt(params[0])*1000;

                Thread.sleep(time);
                resp = "Slept for " + params + " seconds";

            } catch(InterruptedException e){
                e.printStackTrace();
                resp = e.getMessage();

            }catch(Exception e){
                e.printStackTrace();
                resp = e.getMessage();
            }

            return resp;
        }


        /*This method receives progress updates from doInBackground method, which is published via publishProgress method, and this method
        * can use this progress update to update the UI thread*/
        @Override
        protected void onProgressUpdate(String... text){
            finalResult.setText(text[0]);

        }

        /*This method is called after doInBackground method completes processing.  Result from doInBackground is passed to this method*/
        @Override
        protected void onPostExecute(String result){
            progressDialog.dismiss();
            finalResult.setText(result);
        }



    }



}
