package com.example.aashya.thereadingroom;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by aashya on 27/03/15.
 */
public class tata1 extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tata1);
        Button a = (Button) findViewById(R.id.about3);

        a.setOnClickListener(new View.OnClickListener()


                             {

                                 public void onClick(View v) {

                                     Intent intent1 = new Intent(tata1.this, tata2.class);
                                     startActivity(intent1);
                                 }
                             }

        );

        Button b = (Button) findViewById(R.id.contact3);


        b.setOnClickListener(new View.OnClickListener()

                             {
                                 public void onClick(View v) {

                                     Intent intent1 = new Intent(tata1.this, tata3.class);
                                     startActivity(intent1);
                                 }
                             }

        );


    }

}


