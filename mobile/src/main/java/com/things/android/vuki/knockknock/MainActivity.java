package com.things.android.vuki.knockknock;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.things.android.vuki.knockknock.models.GuestEntity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getCanonicalName();
    Button deny;
    Button approve;
    ImageView image;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        FirebaseDatabase instance = FirebaseDatabase.getInstance();
        databaseReference = instance.getReference( "message" );

        deny = findViewById( R.id.deny );
        approve = findViewById( R.id.approve );
        image = findViewById( R.id.image );

        approve.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                sendResponse( true );
            }
        } );

        deny.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                sendResponse( false );
            }
        } );

        ValueEventListener scanListener = new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {
                GuestEntity model = dataSnapshot.getValue( GuestEntity.class );
                if ( model != null && model.getImage() != null ) {
                    byte[] imageBytes = Base64.decode( model.getImage(), Base64.NO_WRAP | Base64.URL_SAFE );
                    Bitmap bitmap = BitmapFactory.decodeByteArray( imageBytes, 0, imageBytes.length );
                    image.setImageBitmap( bitmap );
                } else {
                    image.setImageDrawable( ContextCompat.getDrawable( MainActivity.this, android.R.mipmap.sym_def_app_icon ) );
                }
            }

            @Override
            public void onCancelled( DatabaseError databaseError ) {
                Log.w( TAG, "Failed to read value.", databaseError.toException() );
            }
        };

        databaseReference.addValueEventListener( scanListener );

    }

    private void sendResponse( boolean isApproved ) {
        Map<String, Object> update = new HashMap<>();
        update.put( "isAccepted", isApproved );
        databaseReference.updateChildren( update );

    }
}
