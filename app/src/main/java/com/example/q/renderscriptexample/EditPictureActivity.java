package com.example.q.renderscriptexample;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.q.renderscriptexample.utils.RenderScriptImageEdit;

import java.io.IOException;

public class EditPictureActivity extends AppCompatActivity {
    public final static String BITMAP_URI_EXTRA = "BITMAP_URI_EXTRA";
    private Bitmap image = null;
    private Bitmap editedImage = null;

    private FloatingActionButton mFloatingActionButton;
    private TextView mTextView;
    private ImageView mImageView;

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
        mImageView = (ImageView) findViewById(R.id.computed_image);
        mTextView = (TextView) findViewById(R.id.time_textview);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        iv_original_image.setImageBitmap(image);
        new HistogramEqualizationTask().execute();

    }

    private void setFAB(Boolean blur) {
        if (blur) {
            mFloatingActionButton.setVisibility(View.GONE);
        }
        else {
            mFloatingActionButton.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new BlurTask().execute();
                        }
                    }
            );
            mFloatingActionButton.setVisibility(View.VISIBLE);
        }
    }

    private void setEditedImageView(long time) {
        String text = getString(R.string.compute_time) + time + "ms for "+image.getWidth()+"x"+image.getHeight()+" pixels";
        mTextView.setText(text);
        mImageView.setImageBitmap(editedImage);
        setFAB(false);
    }

    private void setBlurredImage(long time) {
        String text = mTextView.getText().toString();
        text += " " + getString(R.string.blur_compute_time) + time + "ms";
        mTextView.setText(text);
        setFAB(true);
    }

    private class HistogramEqualizationTask extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... params) {
            long begin = System.currentTimeMillis();
            editedImage = RenderScriptImageEdit.histogramEqualization(image, EditPictureActivity.this);
            long end = System.currentTimeMillis();
            long time = end-begin;
            return time;
        }

        protected void onPostExecute(Long result) {
            setEditedImageView(result);
        }
    }

    private class BlurTask extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... params) {
            long begin = System.currentTimeMillis();
            editedImage = RenderScriptImageEdit.blurBitmap(editedImage, 25.0f, EditPictureActivity.this);
            long end = System.currentTimeMillis();
            long time = end-begin;
            return time;
        }

        protected void onPostExecute(Long result) {
            setBlurredImage(result);
        }
    }
}
