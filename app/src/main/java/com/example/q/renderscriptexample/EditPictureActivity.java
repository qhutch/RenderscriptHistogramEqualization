package com.example.q.renderscriptexample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.q.renderscriptexample.utils.RenderScriptImageEdit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

public class EditPictureActivity extends AppCompatActivity {
    public final static String BITMAP_URI_EXTRA = "BITMAP_URI_EXTRA";
    private Bitmap image = null;
    private Bitmap editedImage = null;

    private FloatingActionButton mFloatingActionButton;
    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_picture);

        if (getIntent().hasExtra(BITMAP_URI_EXTRA)) {
            Uri imageUri = Uri.parse(getIntent().getStringExtra(BITMAP_URI_EXTRA));
            try {
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
                finish();
            }
        }
        else {
            finish();
        }
        final ImageView iv_original_image = (ImageView) findViewById(R.id.original_image);
        final ImageView mImageView = (ImageView) findViewById(R.id.computed_image);
        mTextView = (TextView) findViewById(R.id.time_textview);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        iv_original_image.setImageBitmap(image);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (image.getWidth()<1000 && image.getHeight()<1000) {
                    image = RenderScriptImageEdit.scaleBitmap(image, 2.0f, EditPictureActivity.this);
                    iv_original_image.setImageBitmap(image);
                }
                Date begin = new Date();
                editedImage = RenderScriptImageEdit.histogramEqualization(image, EditPictureActivity.this);
                Date end = new Date();
                long time = end.getTime()-begin.getTime();
                String text = getString(R.string.compute_time) + time + "ms for "+image.getWidth()+"x"+image.getHeight()+" pixels";
                mTextView.setText(text);
                mImageView.setImageBitmap(editedImage);
                setFAB();
            }
        };
        runnable.run();
    }

    private void setFAB() {
        mFloatingActionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareImage();
                    }
                }
        );
        mFloatingActionButton.setVisibility(View.VISIBLE);
    }

    private void shareImage() {
        // Save this bitmap to a file.
        File cache = getApplicationContext().getExternalCacheDir();
        File sharefile = new File(cache, "renderScriptShare.png");
        try {
            FileOutputStream out = new FileOutputStream(sharefile);
            editedImage.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (IOException e) {

        }

        // Now send it out to share
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + sharefile));
        try {
            startActivity(Intent.createChooser(share, "Share photo"));
        } catch (Exception e) {

        }
    }
}
