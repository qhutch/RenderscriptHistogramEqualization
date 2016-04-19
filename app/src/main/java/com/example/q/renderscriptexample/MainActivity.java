package com.example.q.renderscriptexample;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {
    private static int PICKER_REQUEST_CODE = 1;
    private static int EDIT_RESULT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        openPicker();
    }

    private void openPicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICKER_REQUEST_CODE);
    }

    private void openEdit(Uri uri) {
        Intent intent = new Intent(this, EditPictureActivity.class);
        intent.putExtra(EditPictureActivity.BITMAP_URI_EXTRA, uri.toString());
        startActivityForResult(intent, EDIT_RESULT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKER_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK) {
                Uri chosenImageUri = data.getData();
                openEdit(chosenImageUri);
            }
            else {
                finish();
            }
        } else {
            openPicker();
        }
    }

}
