package com.things.android.vuki.knockknock;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.things.android.vuki.knockknock.camera.Camera;
import com.things.android.vuki.knockknock.camera.CameraActions;

import java.nio.ByteBuffer;
import java.util.Date;

public class MainActivity extends Activity implements ImageReader.OnImageAvailableListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CameraActions cameraActions;

    //An additional thread for running Camera tasks that shouldn't block the UI.
    private HandlerThread cameraThread;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        databaseReference = FirebaseDatabase.getInstance().getReference( "message" );

        setContentView( R.layout.main );

        android.widget.Button scan = findViewById( R.id.scan );
        scan.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                cameraActions.takePicture();
            }
        } );

        cameraThread = new HandlerThread( "CameraBackground" );
        cameraThread.start();

        //Handler for running Camera tasks in the background.
        Handler cameraHandler = new Handler( cameraThread.getLooper() );
        cameraActions = Camera.getInstance();
        cameraActions.initializeCamera( this, cameraHandler, this );

        ValueEventListener scanListener = new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {
                GuestEntity model = dataSnapshot.getValue( GuestEntity.class );
                if ( model != null ) {
                    if ( model.isAccepted != null ) {
                        if ( model.isAccepted ) {
                            setColor( android.R.color.holo_green_light );
                        } else {
                            setColor( android.R.color.holo_red_light );
                        }
                    } else {
                        setColor( android.R.color.transparent );
                    }
                }
            }

            @Override
            public void onCancelled( DatabaseError databaseError ) {
                Log.w( TAG, "Failed to read value.", databaseError.toException() );
            }
        };

        databaseReference.addValueEventListener( scanListener );
    }

    private void setColor( int color ) {
        findViewById( R.id.root ).setBackgroundColor( ContextCompat.getColor( MainActivity.this, color ) );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraActions.shutDown();
        cameraThread.quitSafely();
    }

    @Override
    public void onImageAvailable( ImageReader reader ) {
        Image image = reader.acquireLatestImage();
        // get image bytes
        ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
        final byte[] imageBytes = new byte[imageBuf.remaining()];
        imageBuf.get( imageBytes );
        image.close();

        onPictureTaken( imageBytes );
    }

    /**
     * Handle image processing in Firebase and Cloud Vision.
     */
    private void onPictureTaken( final byte[] imageBytes ) {

        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                Bitmap bmp = BitmapFactory.decodeByteArray( imageBytes, 0, imageBytes.length );
                ImageView image = findViewById( R.id.image );
                image.setImageBitmap( bmp );
            }
        } );

        if ( databaseReference != null ) {
            GuestEntity guestEntity = new GuestEntity( new Date(  ).getTime(), Base64.encodeToString( imageBytes, Base64.NO_WRAP | Base64.URL_SAFE ) );
            databaseReference.setValue( guestEntity );
        }

    }
}
