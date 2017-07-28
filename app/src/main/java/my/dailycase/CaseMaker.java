package my.dailycase;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class CaseMaker extends DialogFragment {

    DatabaseHelper sqlHelper;
    SQLiteDatabase db;
    private String m_Title = "";
    private String m_Description = "";
    private String m_Date = "";
    private Boolean emptyDialog = true;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View viewInflated = inflater.inflate(R.layout.add_case, null);

        final EditText inputTitle = (EditText)viewInflated.findViewById(R.id.title);
        final EditText inputDescription = (EditText)viewInflated.findViewById(R.id.description);
        final EditText inputDate = (EditText)viewInflated.findViewById(R.id.date);


        final Bundle inputBundle = getArguments();
//
        if (inputBundle != null){
            emptyDialog = false;
            inputTitle.setText(inputBundle.getString("title"));
            inputDescription.setText(inputBundle.getString("description"));
            inputDate.setText(inputBundle.getString("date"));
        }

        builder.setView(viewInflated)
                .setPositiveButton(R.string.Add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        sqlHelper = new DatabaseHelper(getActivity());
                        db = sqlHelper.getWritableDatabase();
                        ContentValues cv = new ContentValues();

                        m_Title = inputTitle.getText().toString();
                        m_Description = inputDescription.getText().toString();
                        m_Date = inputDate.getText().toString();

                        cv.put(DatabaseHelper.COLUMN_TITLE, m_Title);
                        cv.put(DatabaseHelper.COLUMN_DESCRIPTION, m_Description);
                        cv.put(DatabaseHelper.COLUMN_DATE, m_Date);

                        if (!emptyDialog) {
                            cv.put(DatabaseHelper.COLUMN_ID, inputBundle.getInt("id"));
                            int upd = db.update(DatabaseHelper.TABLE, cv, "_id = ?",  new String[]{String.valueOf(inputBundle.getInt("id"))});
                        }
                        else {
                            db.insert(DatabaseHelper.TABLE, null, cv);
                            Toast toast = Toast.makeText(getActivity(),"Added",Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        db.close();

                        MainActivity act = (MainActivity)getActivity();
                        act.updateUI();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        Dialog root = builder.create();

        return root;
    }
}
