package com.example.zametki;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.zametki.Adapter.NotesListAdapter;
import com.example.zametki.DataBase.RoomDB;
import com.example.zametki.Models.Notes;
import com.example.zametki.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener {
    RecyclerView recyclerView;
    FloatingActionButton fab_add;
    NotesListAdapter notesListAdapter;
    RoomDB database;
    List<Notes> notes = new ArrayList<>();
    Notes selectedNote;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());

        recyclerView = findViewById(R.id.recycler_home);
        fab_add = findViewById(R.id.fab_add);


        database = RoomDB.getInstance(this);
        notes = database.mainDao().getAll();

        updateRecycler(notes);


        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
                startActivityForResult(intent, 101);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101){
            if(resultCode == Activity.RESULT_OK){
                assert data != null;
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                if (new_notes != null) {
                    database.mainDao().insert(new_notes);
                    notes.clear();
                    notes.addAll(database.mainDao().getAll());
                    notesListAdapter.notifyDataSetChanged();
                }

            }
        }

        if(requestCode == 102){
            if(resultCode == Activity.RESULT_OK){
                Notes new_notes = (Notes) data.getSerializableExtra("note");
                if (new_notes != null) {
                    database.mainDao().update(new_notes.getID(), new_notes.getTitle(), new_notes.getNotes());
                    notes.clear();
                    notes.addAll(database.mainDao().getAll());
                    notesListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void updateRecycler(List<Notes> notes) {

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    private final NotesClickListener notesClickListener = new NotesClickListener() {
        @Override
        public void onClick(Notes notes) {
            Intent intent = new Intent(MainActivity.this, NotesTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, 102);

        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {
            selectedNote = new Notes();
            selectedNote = notes;
            showPopup(cardView);


        }
    };

    private void showPopup(CardView cardView) {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }


    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if(item.getItemId() == R.id.delete) {
            database.mainDao().delete(selectedNote);
            notes.remove(selectedNote);
            notesListAdapter.notifyDataSetChanged();
            Toast.makeText(MainActivity.this, "Удалено", Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return false;
        }
    }
}