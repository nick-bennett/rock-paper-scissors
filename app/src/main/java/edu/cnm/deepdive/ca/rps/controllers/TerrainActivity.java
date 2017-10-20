package edu.cnm.deepdive.ca.rps.controllers;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import edu.cnm.deepdive.ca.rps.R;
import edu.cnm.deepdive.ca.rps.models.Terrain;
import edu.cnm.deepdive.ca.rps.views.TerrainView;

/**
 * This is a controller class for a simple Android app that runs a Rock-Paper-Scissors ecosystem
 * model, presenting the changing system state as an animated image.
 *
 * @author Nicholas Bennett &amp; Deep Dive Coding Java+Android+SalesForce Bootcamp Cohort 2
 * @version 1.0, 2017-10-20
 */
public class TerrainActivity extends AppCompatActivity {

  private static final int RUNNER_THREAD_REST = 40;
  private static final int RUNNER_THREAD_SLEEP = 50;

  private boolean running = false;
  private boolean inForeground = false;
  private Terrain terrain;
  private Runner runner = null;
  private TerrainView terrainView;
  private View terrainLayout;

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
    terrainView = (TerrainView) findViewById(R.id.terrainView);
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
      terrainLayout.post(new Runnable() {
        @Override
        public void run() {
          terrainView.setSource(terrain.getSnapshot());
        }
      });
    } else {
      this.inForeground = false;
      runner = null;
    }
  }

  private class Runner extends Thread {

    @Override
    public void run() {
      while (isInForeground()) {
        while (isRunning() && isInForeground()) {
          terrain.step();
          terrainView.setSource(terrain.getSnapshot());
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

}
