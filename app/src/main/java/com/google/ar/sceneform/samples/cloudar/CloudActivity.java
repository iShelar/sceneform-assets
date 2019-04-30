/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.cloudar;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.ColorSpace;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputContentInfo;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.RenderableDefinition;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.FutureTask;

import static android.app.DownloadManager.COLUMN_LOCAL_FILENAME;
import static android.app.DownloadManager.COLUMN_LOCAL_URI;
import static android.app.DownloadManager.COLUMN_STATUS;
import static android.os.Environment.*;
import static android.support.v4.view.accessibility.AccessibilityRecordCompat.setSource;
import static java.security.AccessController.getContext;


/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class CloudActivity extends AppCompatActivity {
    private static final String TAG = CloudActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private ArFragment arFragment;
    private ModelRenderable andyRenderable;
    Button button;
    DownloadManager downloadManager;



    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        setContentView(R.layout.activity_ux);
        addListenerOnButton();
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);


/*
*/
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri urq = Uri.parse("https://github.com/googlecodelabs/sceneform-intro/raw/master/Completed/app/src/main/assets/andy.sfb");
        // andy android https://drive.google.com/uc?authuser=0&id=1dwxnZ82GsuWwLgBAtqIsLjHV2PcxHUqZ&export=download
        // "https://holonextstr.blob.core.windows.net/food-blob-obj/box/box.gltf");
        DownloadManager.Request request = new DownloadManager.Request(urq);
        request.setTitle("model");
        request.setDescription("downloading...");
        request.allowScanningByMediaScanner();// if you want to be available from media players

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        Long reference = downloadManager.enqueue(request);
       // File filepath = new File(getFilesDir(), "andy.sfb");
        File filepath = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "andy.sfb");



        Toast.makeText(getApplicationContext(), "FilePath : " + filepath, Toast.LENGTH_LONG).show();




        File file = new File("storage/emulated/0/Download/andy.sfb");
     //   file.exists();


        if (file.exists())
            System.out.println("Exists");
        else
            System.out.println("Does not Exists");

            Callable callable = new Callable() {
            @Override
            public InputStream call() throws Exception {
                InputStream inputStream = new FileInputStream(file);
                return inputStream;
            }
        };


        ModelRenderable.builder()
                .setSource(this, callable )
                .build()
                .thenAccept(renderable -> andyRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });



        arFragment.setOnTapArPlaneListener((HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
            if (andyRenderable == null) {
                return;
            }

            // Create the Anchor.
            Anchor anchor = hitResult.createAnchor();
            AnchorNode anchorNode = new AnchorNode(anchor);
            anchorNode.setParent(arFragment.getArSceneView().getScene());

            // Create the transformable andy and add it to the anchor.
            TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
            andy.setParent(anchorNode);
            andy.setRenderable(andyRenderable);
            andy.select();
        });
    }




    public void addListenerOnButton() {

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View view) {


                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse("https://github.com/googlecodelabs/sceneform-intro/raw/master/Completed/app/src/main/assets/andy.sfb");
                     // "https://holonextstr.blob.core.windows.net/food-blob-obj/box/box.gltf");
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setTitle("model");
                request.allowScanningByMediaScanner();// if you want to be available from media players

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                Long reference = downloadManager.enqueue(request);
                File filepath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "andy.sfb");


                Toast.makeText(getApplicationContext(), "FilePath : " + filepath, Toast.LENGTH_LONG).show();
              // this file is saved at : /storage/emulated/0/Download/andy.png

                //Toast.makeText(getApplicationContext(), "path" + absolutePath, Toast.LENGTH_LONG).show();
            }

        });
    }




    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
}







































