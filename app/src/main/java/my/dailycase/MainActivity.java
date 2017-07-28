package my.dailycase;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    ScrollView scrollView;
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;
    Cursor userCursor;
    SimpleCursorAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scrollView = (ScrollView)findViewById(R.id.main_scroll_view);
        databaseHelper = new DatabaseHelper(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        db = databaseHelper.getReadableDatabase();
        userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);
        updateUI();
    }

    public void updateUI(){
        scrollView.removeAllViews();
        scrollView.refreshDrawableState();

        databaseHelper = new DatabaseHelper(getApplicationContext());
        db = databaseHelper.getReadableDatabase();
        userCursor = db.rawQuery("select * from " + DatabaseHelper.TABLE, null);

        if (userCursor.getCount() == 0){
            Context cnt = getApplicationContext();
            cnt.deleteDatabase(db.getPath());
            return;
        }

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        float density = getApplicationContext().getResources().getDisplayMetrics().density;

        while (userCursor.moveToNext()){
            RelativeLayout relativeLayout = new RelativeLayout(this);
            relativeLayout.setBackgroundResource(R.color.colorCaseBackground1);
            //Title params
            TextView title = new TextView(this);
            title.setId(100000000);
            relativeLayout.setId(userCursor.getInt(0));
            title.setText("Что: " + userCursor.getString(1));
            title.setMaxEms(10);
            title.setMaxLines(1);

            RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            relativeLayout.addView(title, titleParams);

            //date param
            TextView date = new TextView(this);
            date.setText("Когда: " + userCursor.getString(3));

            RelativeLayout.LayoutParams dateParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            date.setMaxEms(10);
            date.setMaxLines(1);
            dateParams.addRule(RelativeLayout.BELOW, title.getId());
            relativeLayout.addView(date, dateParams);

            Button btnDel = new Button(this);
            btnDel.setText("X");
            RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            btnParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            btnParams.width = (int) (30 * density);
            btnDel.setBackgroundResource(R.color.colorCaseBackground1);
            btnDel.setId(userCursor.getInt(0));

            btnDel.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    db.delete(DatabaseHelper.TABLE, "_id = ?", new String[]{String.valueOf(v.getId())});
                    updateUI();
                }
            });

            relativeLayout.addView(btnDel, btnParams);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.setMargins(0, 15, 0, 0);

            relativeLayout.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    Bundle tempBundle = new Bundle();
                    Cursor cursor = db.query(DatabaseHelper.TABLE, null,"_id = ?", new String[]{String.valueOf(v.getId())},
                            null, null, null);

                    cursor.moveToFirst();
                    tempBundle.putInt("id", v.getId());
                    tempBundle.putString("title", cursor.getString(1));
                    tempBundle.putString("description", cursor.getString(2));
                    tempBundle.putString("date", cursor.getString(3));
                    cursor.close();

                    DialogFragment newFragment = new CaseMaker();
                    newFragment.setArguments(tempBundle);
                    newFragment.show(getFragmentManager(), "MYTAG");
                }
            });

            mainLayout.addView(relativeLayout, layoutParams);
        }

        scrollView.addView(mainLayout);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
        db.close();
        userCursor.close();
    }

    public void clearDataBase(View view){
        Context cnt = getApplicationContext();
        cnt.deleteDatabase(db.getPath());
        updateUI();
    }

    public void newDialog(View view){
        DialogFragment newFragment = new CaseMaker();
        newFragment.show(getFragmentManager(), "MYTAG");
    }

    public void newPicture(View view){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                Uri selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);



                //TODO Adding bitmap instead of file's path
//                Bitmap photo = getPreview(selectedImagePath);
//
//                Toast toast = Toast.makeText(this,(photo == null ? "BAD" : "GOOD"),Toast.LENGTH_LONG);
//                toast.show();
//
//                ImageView picture = new ImageView(this);
//                picture.setImageBitmap(photo);
//                LinearLayout layout = (LinearLayout) findViewById(R.id.images_layout);
//                layout.addView(picture);
            }
        }
    }

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        // this is our fallback here
        return uri.getPath();
    }

    public Bitmap getPreview(String fileName) {
        File image = new File(fileName);
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getPath(), bounds);

        if ((bounds.outWidth == -1) || (bounds.outHeight == -1)) {
            return null;
        }
        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / 64;
        Bitmap img = BitmapFactory.decodeFile(image.getPath(), opts);

        return img;
    }
}
