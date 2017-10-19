package edu.cnm.deepdive.ca.rps.activities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ToggleButton;

import edu.cnm.deepdive.ca.rps.R;
import edu.cnm.deepdive.ca.rps.models.Breed;
import edu.cnm.deepdive.ca.rps.models.Terrain;

public class TerrainActivity extends AppCompatActivity {

  private static final int RUNNER_THREAD_REST = 25;
  private static final int RUNNER_THREAD_SLEEP = 50;
  private static final int PAINTER_THREAD_REST = 50;
  private static final int PAINTER_THREAD_SLEEP = 50;

  private boolean running = false;
  private boolean inForeground = false;
  private Terrain terrain;
  private Runner runner = null;
  private Painter painter = null;
  private ImageView terrainView;
  private View terrainLayout;
  private Menu options;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_terrain);
    initializeModel();
    initializeUserInterface();
  }

  @Override
  protected void onStart() {
    super.onStart();
    setInForeground(true);
  }

  @Override
  protected void onStop() {
    setInForeground(false);
    super.onStop();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.options, menu);
    options = menu;
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    boolean running = isRunning();
    menu.findItem(R.id.run_item).setVisible(!running);
    menu.findItem(R.id.pause_item).setVisible(running);
    menu.findItem(R.id.reset_item).setEnabled(!running);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.run_item:
        setRunning(true);
        invalidateOptionsMenu();
        return true;
      case R.id.pause_item:
        setRunning(false);
        invalidateOptionsMenu();
        return true;
      case R.id.reset_item:
        setInForeground(false);
        initializeModel();
        setInForeground(true);
        draw(terrain.getSnapshot());
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void initializeModel() {
    terrain = new Terrain();
    terrain.initialize();
  }

  private void initializeUserInterface() {
    terrainLayout = findViewById(R.id.terrainLayout);
    terrainView = (ImageView) findViewById(R.id.terrainView);
  }

  private synchronized boolean isRunning() {
    return running;
  }

  private synchronized void setRunning(boolean running) {
    this.running = running;
  }

  private synchronized boolean isInForeground() {
    return inForeground;
  }

  private synchronized void setInForeground(boolean inForeground) {
    if (inForeground) {
      this.inForeground = true;
      if (runner == null) {
        runner = new Runner();
        runner.start();
      }
      if (painter == null) {
        painter = new Painter();
        painter.start();
      }
      terrainLayout.post(new Runnable() {
        @Override
        public void run() {
          draw(terrain.getSnapshot());
        }
      });
    } else {
      this.inForeground = false;
      runner = null;
      painter = null;
    }
  }

  private void draw(Breed[][] cells) {
    final float cellSize
        = 1.0f * Math.max(terrainLayout.getHeight(), terrainLayout.getWidth()) / terrain.getSize();
    final Bitmap bitmap = Bitmap.createBitmap((int) (cells[0].length * cellSize),
        (int) (cells.length * cellSize), Bitmap.Config.RGB_565);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    for (int i = 0; i < cells.length; i++) {
      for (int j = 0; j < cells[i].length; j++) {
        switch (cells[i][j]) {
          case ROCK:
            paint.setColor(Color.RED);
            break;
          case PAPER:
            paint.setColor(Color.GREEN);
            break;
          case SCISSORS:
            paint.setColor(Color.BLUE);
            break;
        }
        canvas.drawOval(j * cellSize, i * cellSize, (j + 1) * cellSize, (i + 1) * cellSize, paint);
      }
    }
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        terrainView.setImageBitmap(bitmap);
        terrainView.setLayoutParams(new FrameLayout.LayoutParams(
            (int) (terrain.getSize() * cellSize), (int) (terrain.getSize() * cellSize)));
      }
    });
  }

  private class Runner extends Thread {

    @Override
    public void run() {
      while (isInForeground()) {
        while (isRunning() && isInForeground()) {
          terrain.step();
          try {
            Thread.sleep(RUNNER_THREAD_REST);
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
        try {
          Thread.sleep(RUNNER_THREAD_SLEEP);
        } catch (InterruptedException e) {
          // Do nothing
        }
      }
    }

  }

  private class Painter extends Thread {

    private int steps = 0;

    @Override
    public void run() {
      while (isInForeground()) {
        for (int tempSteps = terrain.getSteps(); tempSteps > steps; steps = tempSteps) {
          Breed[][] snapshot = terrain.getSnapshot();
          draw(snapshot);
          try {
            Thread.sleep(PAINTER_THREAD_REST);
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
        try {
          Thread.sleep(PAINTER_THREAD_SLEEP);
        } catch (InterruptedException e) {
          // Do nothing
        }
      }
    }

  }

}
