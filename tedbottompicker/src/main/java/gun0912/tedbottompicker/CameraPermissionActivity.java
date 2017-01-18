package gun0912.tedbottompicker;

import com.commonsware.cwac.provider.StreamProvider;
import com.tbruyelle.rxpermissions.RxPermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.functions.Action1;


/**
 * Created by stephan on 06/12/16.
 */

public class CameraPermissionActivity extends FragmentActivity {

    public static final String ERROR_MESSAGE = "error_message";
    public static final String CAMERA_URI = "camera_uri";
    private static final int CAMERA_CODE = 10;
    private Uri cameraImageUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            String imagePath = savedInstanceState.getString(CAMERA_URI);

            Intent returnIntent = new Intent();

            if (imagePath != null && new File(imagePath).exists()) {
                returnIntent.putExtra(CAMERA_URI, cameraImageUri);
                setResult(Activity.RESULT_OK, returnIntent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
            return;
        }

        RxPermissions.getInstance(this).request(Manifest.permission.CAMERA).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean _boolean) {
                if (_boolean) {
                    startCameraIntent();
                } else {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable _throwable) {
                finishActivityWithError(_throwable.getMessage());
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (cameraImageUri != null) {
            outState.putString(CAMERA_URI, cameraImageUri.getPath());
        }
        super.onSaveInstanceState(outState);
    }

    private void finishActivityWithError(String _error) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(ERROR_MESSAGE, _error);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void startCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File imageFile = getImageFile();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri photoURI = StreamProvider.getUriForFile(this.getApplicationContext().getPackageName() + ".provider", imageFile);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        } else {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        }

        startActivityForResult(cameraIntent, CAMERA_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_CODE && resultCode == RESULT_OK) {
            Intent returnIntent = new Intent();

            if (new File(cameraImageUri.getPath()).exists()) {
                returnIntent.putExtra(CAMERA_URI, cameraImageUri);
            }
            setResult(Activity.RESULT_OK, returnIntent);

            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private File getImageFile() {
        // Create an image file name
        File imageFile = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            storageDir.mkdirs();

            imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            cameraImageUri = Uri.fromFile(imageFile);
        } catch (IOException e) {
            e.printStackTrace();
            finishActivityWithError("Could not create imageFile for camera");
        }

        return imageFile;
    }
}
