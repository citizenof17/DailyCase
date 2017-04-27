package my.dailycase;

import android.app.DialogFragment;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static android.database.DatabaseUtils.queryNumEntries;

public class MainActivity extends AppCompatActivity {

    ScrollView scrollView;
//    LinearLayout cases;
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

        String[] headers = new String[] {DatabaseHelper.COLUMN_TITLE,
                DatabaseHelper.COLUMN_DESCRIPTION};

        userAdapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item,
                userCursor, headers, new int[]{android.R.id.text1, android.R.id.text2}, 0);

        TextView test = new TextView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundResource(R.color.colorCaseBackground1);
        while (userCursor.moveToNext()){
            TextView title = new TextView(this);
            title.setText("Title: " + userCursor.getString(1));
            layout.addView(title, new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView desc = new TextView(this);
            desc.setText("Description: " + userCursor.getString(2));
            layout.addView(desc, new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 10, 0, 0);
        scrollView.addView(layout, layoutParams);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseHelper.close();
        db.close();
        userCursor.close();
    }

    public void clearDataBase(View view){
//        databaseHelper.close();
        Context cnt = getApplicationContext();
        cnt.deleteDatabase(db.getPath());
        updateUI();
    }

    public void newDialog(View view){
//        int cnt = userCursor.getCount();

        DialogFragment newFragment = new CaseMaker();
        newFragment.show(getFragmentManager(), "MYTAG");

//        updateUI();
    }
}
