package edu.cnm.deepdive.ca.rps.views;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import edu.cnm.deepdive.ca.rps.models.Breed;

/**
 * View class for the Rock-Paper-Scissors cellular automaton (CA). This class implements the {@link
 * #onDraw(Canvas)} method to render an image of the RPS terrain, on a toroidal space, on which a
 * lattice is overlayed with agents of one of the 3 types at each of the lattice points.
 * <p>
 * As currently implemented, all of the drawing is done on the UI thread; delegating this
 * work to other threads is one of the planned refinements for the future.
 */
public class TerrainView extends View {

  private Paint paint = new Paint();
  private Breed[][] source = null;
  private boolean drawing = false;

  {
    setWillNotDraw(false);
  }

  /**
   * Invokes the superclass constructor ({@link View#View(Context)}) to perform primary
   * initialization.
   *
   * @param context reference to access Android resources and services
   */
  public TerrainView(Context context) {
    super(context);
  }

  /**
   * Invokes the corresponding superclass constructor ({@link
   * View#View(Context, AttributeSet)}) to perform primary initialization.
   *
   * @param context reference to access Android resources and services
   * @param attrs   XML attributes
   */
  public TerrainView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  /**
   * Invokes the corresponding superclass constructor ({@link
   * View#View(Context, AttributeSet, int)}) to perform primary initialization.
   *
   * @param context      reference to access Android resources and services
   * @param attrs        XML attributes
   * @param defStyleAttr default style based on theme
   */
  public TerrainView(Context context, AttributeSet attrs,
      int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * Invokes the corresponding superclass constructor ({@link
   * View#View(Context, AttributeSet, int)}) to perform primary initialization.
   *
   * @param context      reference to access Android resources and services
   * @param attrs        XML attributes
   * @param defStyleAttr default style based on theme
   * @param defStyleRes  style resource ID
   */
  public TerrainView(Context context, AttributeSet attrs,
      int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int contentSize = Math.max(getSuggestedMinimumWidth(), getSuggestedMinimumHeight());
    int width = resolveSizeAndState(getPaddingLeft() + getPaddingRight() + contentSize,
        widthMeasureSpec, 0);
    int height = resolveSizeAndState(getPaddingTop() + getPaddingBottom() + contentSize,
        heightMeasureSpec, 0);
    contentSize = Math.max(width - getPaddingLeft() - getPaddingRight(),
        height - getPaddingTop() - getPaddingBottom());
    width = resolveSizeAndState(getPaddingLeft() + getPaddingRight() + contentSize,
        widthMeasureSpec, 0);
    height = resolveSizeAndState(getPaddingTop() + getPaddingBottom() + contentSize,
        heightMeasureSpec, 0);
    setMeasuredDimension(width, height);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (!drawing && source != null) {
      super.onDraw(canvas);
      setDrawing(true);
      Breed[][] cells = source;
      float cellSize = Math.min(1.0f * canvas.getHeight() / cells.length,
          1.0f * canvas.getWidth() / cells[0].length);
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
          canvas.drawOval(
              j * cellSize, i * cellSize, (j + 1) * cellSize, (i + 1) * cellSize, paint);
        }
      }
      setDrawing(false);
    }
  }

  /**
   *
   * @param source 2-dimensional array of RPS population
   */
  public synchronized void setSource(Breed[][] source) {
    this.source = source;
    postInvalidate();
  }

  /**
   *
   * @param drawing semphore indicating whether drawing is being performed at this moment
   */
  private synchronized void setDrawing(boolean drawing) {
    this.drawing = drawing;
  }

}
