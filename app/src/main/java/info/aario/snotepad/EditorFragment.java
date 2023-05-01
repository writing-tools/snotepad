package info.aario.snotepad;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;

// Elias: for pasting date button:
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import android.content.ClipboardManager;

/**
 * Created by aario on 3/16/17.
 */

public class EditorFragment extends Fragment {
    private MainActivity activity;
    private String name;
    private EditText etEditor;
    private EditText etTitle;
    private String path;
    ArrayList<String> textUndoHistory = new ArrayList<String>();
    ArrayList<Integer> selectionStartUndoHistory = new ArrayList<Integer>();
    ArrayList<Integer> selectionEndUndoHistory = new ArrayList<Integer>();
    private Button btUndo;
    private Button btRedo;
    private Button btSave;
    private Button btShare;
    private Button btDate;
    private int undoHistoryCursor = 0;
    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            saveUndoRedo();
        }

        @Override
        public void afterTextChanged(Editable editable) {
            activity.editor_modified = true;
        }
    };

    private void saveUndoRedo() {
        if (textUndoHistory.size() > 0 && undoHistoryCursor < textUndoHistory.size() - 1) {
            for (int c = textUndoHistory.size() - 1; c >= undoHistoryCursor; c--) {
                textUndoHistory.remove(c);
                selectionStartUndoHistory.remove(c);
                selectionEndUndoHistory.remove(c);
            }
        }
        textUndoHistory.add(etEditor.getText().toString());
        selectionStartUndoHistory.add(etEditor.getSelectionStart());
        selectionEndUndoHistory.add(etEditor.getSelectionEnd());
        undoHistoryCursor = textUndoHistory.size() - 1;
        updateUndoRedoButtons();
    }

    private void retrieveUndoRedo() {
        etEditor.removeTextChangedListener(textWatcher);
        etEditor.setText(textUndoHistory.get(undoHistoryCursor).toString());
        etEditor.setSelection(
                selectionStartUndoHistory.get(undoHistoryCursor),
                selectionStartUndoHistory.get(undoHistoryCursor)
        );
        etEditor.addTextChangedListener(textWatcher);
        updateUndoRedoButtons();
    }

    private void updateUndoRedoButtons() {
        btUndo.setEnabled(textUndoHistory.size() > 0 && undoHistoryCursor > 0);
        btRedo.setEnabled(textUndoHistory.size() > 0 && undoHistoryCursor < textUndoHistory.size() - 1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        activity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.editor_fragment, container, false);
        FloatingActionButton fab = (FloatingActionButton) activity.findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        etTitle = (EditText) view.findViewById(R.id.etTitle);
        path = activity.getOpenedFilePath();
        name = activity.filer.getFileNameWithoutExtension(path);
        etTitle.setText(name);
        etEditor = (EditText) view.findViewById(R.id.etEditor);
        if (activity.filer.exists(path))
            etEditor.setText(activity.filer.getStringFromFile(path));
        etEditor.addTextChangedListener(textWatcher);
        textUndoHistory.clear();
        selectionStartUndoHistory.clear();
        selectionEndUndoHistory.clear();
        btUndo = (Button) view.findViewById(R.id.btUndo);
        btUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undoHistoryCursor--;
                retrieveUndoRedo();
            }
        });
        btRedo = (Button) view.findViewById(R.id.btRedo);
        btRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                undoHistoryCursor++;
                retrieveUndoRedo();
            }
        });
        saveUndoRedo();
        btSave = (Button) view.findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        btShare = (Button) view.findViewById(R.id.btShare);
        btShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
            }
        });
/*
        btDate = (Button) view.findViewById(R.id.btDate); // Elias
        btDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteDate();
            }
        });
*/
        return view;
    }

    private String getEditorText() {
        return etEditor.getText().toString();
    }

    public void save() {
        String newName = etTitle.getText().toString();
        String oldPath = path;
        boolean rename = (!newName.equals(name));
        if (rename) {
            path = activity.listFragment.proposeNewFilePath(newName);
            name = activity.filer.getFileNameWithoutExtension(path);
        }

        if (activity.filer.writeToFile(path, getEditorText())) {
            activity.makeSnackBar("Changes saved to " + path); // Elias: notification.

            if (rename)
                activity.filer.delete(oldPath);
            activity.editor_modified = false;
        }
    }

    private void pasteDate() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = myDateObj.format(myFormatObj);
        // date source: https://www.w3schools.com/java/java_date.asp


        // TODO: how to paste this text? see:
        // https://developer.android.com/guide/topics/text/copy-paste
        // https://safe.duckduckgo.com/?q=stackoverflow+paste+string+to+clipboard+java+and+roid&kp=1&atb=v259-7__&ia=web
        // https://safe.duckduckgo.com/?q=stackoverflow+copy+string+to+clipboard+java+and+roid&kp=1&atb=v259-7__&ia=web

        //ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        //clipboard.setText(formattedDate);

        /*
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(formattedDate);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(formattedDate, formattedDate);
            clipboard.setPrimaryClip(clip);
        }*/

        //ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        //ClipData clip = ClipData.newPlainText("your_text_to_be_copied");
        //clipboard.setPrimaryClip(clip);

    }

    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getEditorText());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

}
