package com.example.myapp;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Feature;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    // Cloud Vision APIキー
    private static final String CLOUD_VISION_API_KEY = "AIzaSyDfSwo7_s9EHovAnpTPBj9CKtrWdA7s9bY";
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView); // ImageViewを初期化
        dispatchTakePictureIntent(); // カメラを起動する
    }

    // カメラアプリを起動するためのインテントを作成し、起動する
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    // カメラアプリから戻ってきたときに呼ばれるメソッド
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // ここでスーパークラスのメソッドを呼び出す

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap); // 撮影した画像をImageViewに表示する
            callCloudVision(imageBitmap); // Cloud Vision APIを呼び出す
        }
    }

    // Cloud Vision APIを呼び出すメソッド
    private void callCloudVision(Bitmap bitmap) {
        try {
            // Vision APIのクライアントを初期化する
            Vision.Builder visionBuilder = new Vision.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    null
            );
            Vision vision = visionBuilder.build();

            // 画像をBase64エンコードする
            Image base64EncodedImage = new Image();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            base64EncodedImage.encodeContent(imageBytes);

            // 画像注釈リクエストを作成する
            AnnotateImageRequest request = new AnnotateImageRequest()
                    .setImage(base64EncodedImage)
                    .setFeatures(Arrays.asList(new Feature().setType("LABEL_DETECTION").setMaxResults(10)));

            // バッチリクエストを作成する
            BatchAnnotateImagesRequest batchRequest = new BatchAnnotateImagesRequest().setRequests(Arrays.asList(request));
            Vision.Images.Annotate annotateRequest = vision.images().annotate(batchRequest);
            annotateRequest.setDisableGZipContent(true);

            // リクエストを実行してレスポンスを取得する
            AnnotateImageResponse response = annotateRequest.execute().getResponses().get(0);
            Log.d("CloudVision", response.toString()); // レスポンスをログに出力する
        } catch (Exception e) {
            Log.d("CloudVision", "Error: " + e.getMessage()); // エラーメッセージをログに出力する
        }
    }
}
