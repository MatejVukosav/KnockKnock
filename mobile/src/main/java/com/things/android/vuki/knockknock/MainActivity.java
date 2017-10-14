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
import android.widget.TextView;

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
    Button reset;
    TextView time;
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
        reset = findViewById( R.id.reset );
        time = findViewById( R.id.time );
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

        reset.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                databaseReference.setValue( new GuestEntity() );
            }
        } );

        ValueEventListener scanListener = new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {
                GuestEntity model = dataSnapshot.getValue( GuestEntity.class );
                if ( model != null ) {
                    bindData( model );
                } else {
                    //reset
                    image.setImageDrawable( ContextCompat.getDrawable( MainActivity.this, android.R.mipmap.sym_def_app_icon ) );
                    setColor( android.R.color.holo_orange_light );
                }
            }

            @Override
            public void onCancelled( DatabaseError databaseError ) {
                Log.w( TAG, "Failed to read value.", databaseError.toException() );
            }
        };

        databaseReference.addValueEventListener( scanListener );
    }

    private void bindData( GuestEntity model ) {
        if ( model.getImage() != null ) {
            byte[] imageBytes = Base64.decode( model.getImage(), Base64.NO_WRAP | Base64.URL_SAFE );
            Bitmap bitmap = BitmapFactory.decodeByteArray( imageBytes, 0, imageBytes.length );
            image.setImageBitmap( bitmap );
        } else {
            image.setImageDrawable( ContextCompat.getDrawable( MainActivity.this, android.R.mipmap.sym_def_app_icon ) );
        }
        if ( model.getTimestamp() != null ) {
            time.setText( TimeHelper.getDateFromTimestamp( model.getTimestamp() ) );
        } else {
            time.setText( "" );
        }

        if ( model.getAccepted() != null ) {
            if ( model.getAccepted() ) {
                setColor( android.R.color.holo_green_light );
            } else {
                setColor( android.R.color.holo_red_light );
            }
        } else {
            setColor( android.R.color.transparent );
        }
    }

    private void setColor( int color ) {
        findViewById( R.id.root ).setBackgroundColor( ContextCompat.getColor( MainActivity.this, color ) );
    }

    private void sendResponse( boolean isApproved ) {
        Map<String, Object> update = new HashMap<>();
        update.put( "isAccepted", isApproved );
        databaseReference.updateChildren( update );
    }
}
