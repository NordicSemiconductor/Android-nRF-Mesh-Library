package no.nordicsemi.android.nrfmesh.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import no.nordicsemi.android.mesh.AllocatedGroupRange;
import no.nordicsemi.android.mesh.AllocatedSceneRange;
import no.nordicsemi.android.mesh.AllocatedUnicastRange;
import no.nordicsemi.android.mesh.Range;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.nrfmesh.R;

public class RangeView extends View {

    private Paint mPaint;
    private int unallocatedColor;
    private int rangesColor;
    private int otherRangeColor;
    private int conflictColor;
    private int borderColor;
    private List<Range> ranges = new ArrayList<>();
    private List<Range> otherRanges = new ArrayList<>();

    public RangeView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public void addRanges(@NonNull final List<? extends Range> ranges) {
        this.ranges.addAll(ranges);
        invalidate();
    }

    public void addRange(@NonNull final Range range) {
        ranges.add(range);
        invalidate();
    }

    public void clearRanges() {
        ranges.clear();
        invalidate();
    }

    public void addOtherRanges(@NonNull final List<? extends Range> otherRanges) {
        this.otherRanges.addAll(otherRanges);
        invalidate();
    }

    public void addOtherRange(@NonNull final Range otherRange) {
        otherRanges.add(otherRange);
        invalidate();
    }

    public void clearOtherRanges() {
        otherRanges.clear();
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(unallocatedColor);
        drawRanges(canvas);
        drawOtherRanges(canvas);
        drawConflictingRange(canvas);
        drawBorder(canvas);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initPaint(@NonNull final Context context) {
        unallocatedColor = ContextCompat.getColor(context, R.color.nordicLightGray);
        rangesColor = ContextCompat.getColor(context, R.color.nordicLake);
        otherRangeColor = ContextCompat.getColor(context, R.color.nordicMediumGray);
        conflictColor = ContextCompat.getColor(context, R.color.nordicRed);
        borderColor = ContextCompat.getColor(context, R.color.nordicMediumGray);
        mPaint = new Paint();
    }

    private void drawRanges(@NonNull final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(rangesColor);
        for (Range range : ranges) {
            if (range instanceof AllocatedUnicastRange) {
                final AllocatedUnicastRange range1 = (AllocatedUnicastRange) range;
                final Rect rect = getRegion(canvas, range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_UNICAST_ADDRESS, MeshAddress.END_UNICAST_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedGroupRange) {
                final AllocatedGroupRange range1 = (AllocatedGroupRange) range;
                final Rect rect = getRegion(canvas, range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedSceneRange) {
                final AllocatedSceneRange range1 = (AllocatedSceneRange) range;
                final Rect rect = getRegion(canvas, range1.getFirstScene(), range1.getLastScene(), range1.getLowerBound(), range1.getUpperBound());
                canvas.drawRect(rect, paint);
            }
        }
    }

    private void drawOtherRanges(@NonNull final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(otherRangeColor);
        for (Range range : otherRanges) {
            if (range instanceof AllocatedUnicastRange) {
                final AllocatedUnicastRange range1 = (AllocatedUnicastRange) range;
                final Rect rect = getRegion(canvas, range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_UNICAST_ADDRESS, MeshAddress.END_UNICAST_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedGroupRange) {
                final AllocatedGroupRange range1 = (AllocatedGroupRange) range;
                final Rect rect = getRegion(canvas, range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedSceneRange) {
                final AllocatedSceneRange range1 = (AllocatedSceneRange) range;
                final Rect rect = getRegion(canvas, range1.getFirstScene(), range1.getLastScene(), range1.getLowerBound(), range1.getUpperBound());
                canvas.drawRect(rect, paint);
            }
        }
    }

    public void drawConflictingRange(@NonNull final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(conflictColor);
        for (Range range : ranges) {
            for (Range other : otherRanges) {
                if (range instanceof AllocatedUnicastRange) {
                    final AllocatedUnicastRange unicastRange = (AllocatedUnicastRange) range;
                    final AllocatedUnicastRange otherRange = (AllocatedUnicastRange) other;
                    if (unicastRange.overlaps(otherRange)) {
                        final Rect overlapRegion = getRegion(canvas, otherRange.getLowAddress(), otherRange.getHighAddress(),
                                MeshAddress.START_UNICAST_ADDRESS, MeshAddress.END_UNICAST_ADDRESS);
                        canvas.drawRect(overlapRegion, paint);
                    }
                } else if (range instanceof AllocatedGroupRange) {
                    final AllocatedGroupRange groupRange = (AllocatedGroupRange) range;
                    final AllocatedGroupRange otherRange = (AllocatedGroupRange) other;
                    if (groupRange.overlaps(otherRange)) {
                        final Rect overlapRegion = getRegion(canvas, otherRange.getLowAddress(), otherRange.getHighAddress(),
                                MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS);
                        canvas.drawRect(overlapRegion, paint);
                    }
                } else {
                    final AllocatedSceneRange sceneRange = (AllocatedSceneRange) range;
                    final AllocatedSceneRange otherRange = (AllocatedSceneRange) other;
                    if (sceneRange.overlaps(otherRange)) {
                        final Rect overlapRegion = getRegion(canvas, otherRange.getFirstScene(), otherRange.getLastScene(),
                                otherRange.getLowerBound(), otherRange.getUpperBound());
                        canvas.drawRect(overlapRegion, paint);
                    }
                }
            }
        }
    }

    private void drawBorder(@NonNull final Canvas canvas) {
        mPaint.setColor(borderColor);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(canvas.getClipBounds(), mPaint);
    }

    @NonNull
    private Rect getRegion(@NonNull final Canvas canvas, final int lowAddress, final int highAddress, final int lowerBound, final int upperBound) {
        final Rect mRect = canvas.getClipBounds();
        final float unit = (mRect.width() / (float) (upperBound - lowerBound));
        final int x = (int) ((lowAddress - lowerBound) * unit);
        final int right = (int) ((highAddress - lowerBound) * unit);
        return new Rect(x, 0, right, mRect.height());
    }

    @NonNull
    private Paint getRectPaint() {
        final Paint p = mPaint;
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        return p;
    }
}
