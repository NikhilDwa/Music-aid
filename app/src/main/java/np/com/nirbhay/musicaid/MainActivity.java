package np.com.nirbhay.musicaid;

import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;

import java.util.ArrayList;
import java.util.List;

import np.com.nirbhay.musicaid.active_android.HappySongModel;
import np.com.nirbhay.musicaid.active_android.SadSongModel;
import np.com.nirbhay.musicaid.adapter.MainActivityRecyclerViewAdapter;
import np.com.nirbhay.musicaid.data_set.MusicDescription;

import static np.com.nirbhay.musicaid.adapter.MainActivityRecyclerViewAdapter.mediaPlayer;
import static np.com.nirbhay.musicaid.adapter.MainActivityRecyclerViewAdapter.releasePlayer;

public class MainActivity extends AppCompatActivity {
    private ImageView playPause;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EmotionCheckActivity.fromActivityResult = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBackgroundThread();
        ActiveAndroid.initialize(this);
        int FLAG = getIntent().getIntExtra("FLAG",0);
        System.err.println("flag --> "+FLAG);
        switch (FLAG){
            case 1:
                ArrayList<MusicDescription> sadData = changePathSadSong(new SadSongModel().getAllList());
                System.err.println(sadData.size());
                addToRecyclerSadSong(sadData);
                break;
            case 2:
                ArrayList<MusicDescription> happyData = changePathHappySong(new HappySongModel().getAllList());
                System.err.println(happyData.size());
                addToRecyclerHappySong(happyData);
        }
        ImageView nextSong = findViewById(R.id.imageViewNextButton);
        ImageView previousSong = findViewById(R.id.imageViewPreviousButton);
        playPause = findViewById(R.id.imageViewPlayPauseButton);
        nextSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        previousSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        if (mediaPlayer != null) {
                            mediaPlayer.start();
                        }
                    }
                } catch (NullPointerException ignored) {
                }
            }
        });
    }

    void startBackgroundThread() {
        thread.start();
    }

    void stopBackgroundThread() {
        thread.interrupt();
    }

    private Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (mediaPlayer.isPlaying()) {
                                    playPause.setImageResource(R.drawable.ic_pause);
                                } else {
                                    playPause.setImageResource(R.drawable.ic_play);
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopBackgroundThread();
        try {
            mediaPlayer.release();
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch(itemId){
            case R.id.sadSong:
                Intent sadIntent = new Intent(MainActivity.this,DatabaseActivity.class);
                sadIntent.putExtra("FLAG",1);
                startActivity(sadIntent);
                break;
            case R.id.happySong:
                Intent happyIntent = new Intent(MainActivity.this,DatabaseActivity.class);
                happyIntent.putExtra("FLAG",2);
                startActivity(happyIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    private ArrayList<MusicDescription> changePathHappySong(List<HappySongModel> data) {
        ArrayList<MusicDescription> musicDescription = new ArrayList<>();
        for (HappySongModel song : data) {
            System.err.println();
            MusicDescription music = new MusicDescription();
            music.setArtistName(song.artistName);
            music.setTitleName(song.title_name);
            music.setMusicData(song.path);
            music.setDisplayName(song.display_name);
            music.setDuration(song.duration);
            music.setAlbumArt(albumArt(song.albumId));
            musicDescription.add(music);
        }
        return musicDescription;
    }

    private ArrayList<MusicDescription> changePathSadSong(List<SadSongModel> data) {
        ArrayList<MusicDescription> musicDescription = new ArrayList<>();
        for (SadSongModel song : data) {
            System.err.println();
            MusicDescription music = new MusicDescription();
            music.setArtistName(song.artistName);
            music.setTitleName(song.title_name);
            music.setMusicData(song.path);
            music.setDisplayName(song.display_name);
            music.setDuration(song.duration);
            music.setAlbumArt(albumArt(song.albumId));
            musicDescription.add(music);
        }
        return musicDescription;
    }

    private Bitmap albumArt(int albumId) {
        Uri sArtworkUri = Uri
                .parse("content://media/external/audio/albumart");
        Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), albumArtUri);
            bitmap = Bitmap.createScaledBitmap(bitmap, 60, 60, true);
        } catch (Exception exception) {
            bitmap = null;
        }
        return bitmap;
    }

    private void addToRecyclerSadSong(ArrayList<MusicDescription> data) {
        RecyclerView mRecyclerView = findViewById(R.id.recyclerViewMain);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        final MainActivityRecyclerViewAdapter adapter = new MainActivityRecyclerViewAdapter(this, data, 1);
        android.support.design.widget.FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setImageResource(R.drawable.sad_emoji);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePlayer();
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    private void addToRecyclerHappySong(ArrayList<MusicDescription> data) {
        RecyclerView mRecyclerView = findViewById(R.id.recyclerViewMain);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        final MainActivityRecyclerViewAdapter adapter = new MainActivityRecyclerViewAdapter(this, data, 2);
        android.support.design.widget.FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setImageResource(R.drawable.laugh_emoji);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releasePlayer();
            }
        });
        mRecyclerView.setAdapter(adapter);
    }
}
