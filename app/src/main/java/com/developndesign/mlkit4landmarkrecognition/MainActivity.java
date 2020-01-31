package com.developndesign.mlkit4landmarkrecognition;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmark;
import com.google.firebase.ml.vision.cloud.landmark.FirebaseVisionCloudLandmarkDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionLatLng;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView textView; // Displaying the label for the input image
    Button button; // To select the image from device
    ImageView imageView; //To display the selected image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.text);
        button = findViewById(R.id.selectImage);
        imageView = findViewById(R.id.image);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().start(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    Uri uri = result.getUri(); //path of image in phone
                    imageView.setImageURI(uri); //set image in imageview
                    textView.setText(""); //so that previous text don't get append with new one
                    landmarkRecognitionFromImage(uri);
                }
            }
        }
    }

    private void landmarkRecognitionFromImage(Uri uri) {

        try {
            FirebaseVisionImage image = FirebaseVisionImage.fromFilePath(MainActivity.this, uri);
            FirebaseVisionCloudDetectorOptions options =
                    new FirebaseVisionCloudDetectorOptions.Builder()
                            .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                            .setMaxResults(15)
                            .build();
//            FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
//                    .getVisionCloudLandmarkDetector();
// Or, to change the default settings:
            FirebaseVisionCloudLandmarkDetector detector = FirebaseVision.getInstance()
                    .getVisionCloudLandmarkDetector(options);
            detector.detectInImage(image)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionCloudLandmark>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionCloudLandmark> firebaseVisionCloudLandmarks) {
                            for (FirebaseVisionCloudLandmark landmark : firebaseVisionCloudLandmarks) {

                                Rect bounds = landmark.getBoundingBox();
                                String landmarkName = landmark.getLandmark();
                                String entityId = landmark.getEntityId();
                                float confidence = landmark.getConfidence();
                                textView.append("bounds " + bounds + "\n" + "landmarkName " + landmarkName + "\n" + "entityId " + entityId + "\n" + "confidence " + confidence);
                                // Multiple locations are possible, e.g., the location of the depicted
                                // landmark and the location the picture was taken.
                                for (FirebaseVisionLatLng loc : landmark.getLocations()) {
                                    double latitude = loc.getLatitude();
                                    double longitude = loc.getLongitude();
                                    textView.append("latitude " + latitude + "\n");
                                    textView.append("longtiude " + longitude + "\n");
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}