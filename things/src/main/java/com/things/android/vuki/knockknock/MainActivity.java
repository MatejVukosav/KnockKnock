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
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.android.things.contrib.driver.button.Button;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.things.android.vuki.knockknock.camera.Camera;
import com.things.android.vuki.knockknock.camera.CameraActions;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends Activity implements ImageReader.OnImageAvailableListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String gpioButtonPinName = "BUS NAME";
    private Button button;
    private CameraActions cameraActions;
    //Handler for running Camera tasks in the background.
    private Handler cameraHandler;
    //An additional thread for running Camera tasks that shouldn't block the UI.
    private HandlerThread cameraThread;

    private DatabaseReference databaseReference;

    /**
     * A {@link Handler} for running Cloud tasks in the background.
     */
    private Handler cloudHandler;

    /**
     * An additional thread for running Cloud tasks that shouldn't block the UI.
     */
    private HandlerThread cloudThread;

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
        cameraHandler = new Handler( cameraThread.getLooper() );
        cameraActions = Camera.getInstance();
        cameraActions.initializeCamera( this, cameraHandler, this );

        setupButton();

        ValueEventListener scanListener = new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot dataSnapshot ) {
                GuestEntity model = dataSnapshot.getValue( GuestEntity.class );
                if ( model != null ) {
                    ImageView result = findViewById( R.id.result );
                    if ( model.isAccepted ) {
                        result.setImageDrawable( ContextCompat.getDrawable( getApplicationContext(), android.R.drawable.btn_plus ) );
                    } else {
                        result.setImageDrawable( ContextCompat.getDrawable( getApplicationContext(), android.R.drawable.btn_minus ) );
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraActions.shutDown();
        cameraThread.quitSafely();
        destroyButton();
    }

    @Override
    public boolean onKeyUp( int keyCode, KeyEvent event ) {
        if ( keyCode == KeyEvent.KEYCODE_ENTER ) {
            Log.d( TAG, "button pressed" );
            cameraActions.takePicture();
            return true;
        }
        return super.onKeyUp( keyCode, event );
    }

    private void setupButton() {
        try {
            button = new Button( gpioButtonPinName,
                    // high signal indicates the button is pressed
                    // use with a pull-down resistor
                    Button.LogicState.PRESSED_WHEN_HIGH
            );
            button.setOnButtonEventListener( new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent( Button button, boolean pressed ) {
                    // do something awesome
                }
            } );
        } catch ( IOException e ) {
            // couldn't configure the button...
        }
    }

    private void destroyButton() {
        if ( button != null ) {
            Log.i( TAG, "Closing button" );
            try {
                button.close();
            } catch ( IOException e ) {
                Log.e( TAG, "Error closing button", e );
            } finally {
                button = null;
            }
        }
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
            long timestamp = System.currentTimeMillis() / 1000;
            GuestEntity guestEntity = new GuestEntity( timestamp, Base64.encodeToString( imageBytes, Base64.NO_WRAP | Base64.URL_SAFE ) );
            databaseReference.setValue( guestEntity );
        }

    }
}
