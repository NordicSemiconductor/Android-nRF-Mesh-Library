package no.nordicsemi.android.nrfmeshprovisioner.widgets;

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
import no.nordicsemi.android.meshprovisioner.AllocatedGroupRange;
import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;
import no.nordicsemi.android.meshprovisioner.AllocatedUnicastRange;
import no.nordicsemi.android.meshprovisioner.Range;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.R;

public class RangeView extends View {

    private Paint mPaint;
    private int unallocatedColor;
    private int rangesColor;
    private int otherRangeColor;
    private int conflictColor;
    private int borderColor;
    private Rect mRect;
    private List<Range> ranges = new ArrayList<>();
    private List<Range> otherRanges = new ArrayList<>();

    public RangeView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        initPaint(context);
    }

    public void addRanges(@NonNull final List<? extends Range> ranges) {
        this.ranges.addAll(ranges);
        invalidate();
    }

    public void addRange(@NonNull final Range range) {
        this.ranges.add(range);
        invalidate();
    }

    public void clearRanges() {
        ranges.clear();
        invalidate();
    }

    public void addOtherRanges(@NonNull final List<? extends Range> ranges) {
        this.otherRanges.addAll(ranges);
        invalidate();
    }

    public void addOtherRange(@NonNull final Range range) {
        this.otherRanges.add(range);
        invalidate();
    }

    public void clearOtherRanges() {
        otherRanges.clear();
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        mRect = canvas.getClipBounds();
        canvas.drawColor(unallocatedColor);
        drawRanges(canvas);
        drawOtherRanges(canvas);
        drawConflictingRange(canvas);


        mPaint.setColor(borderColor);
        mPaint.setStrokeWidth(2);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(mRect, mPaint);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void initPaint(final Context context) {
        unallocatedColor = ContextCompat.getColor(context, R.color.nordicLightGray);
        rangesColor = ContextCompat.getColor(context, R.color.nordicLake);
        otherRangeColor = ContextCompat.getColor(context, R.color.nordicMediumGray);
        conflictColor = ContextCompat.getColor(context, R.color.nordicRed);
        borderColor = ContextCompat.getColor(context, R.color.nordicMediumGray);
        mPaint = new Paint();
    }

    private void drawRanges(final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(rangesColor);
        for (Range range : ranges) {
            if (range instanceof AllocatedUnicastRange) {
                final AllocatedUnicastRange range1 = (AllocatedUnicastRange) range;
                final Rect rect = getRegion(range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_UNICAST_ADDRESS, MeshAddress.END_UNICAST_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedGroupRange) {
                final AllocatedGroupRange range1 = (AllocatedGroupRange) range;
                final Rect rect = getRegion(range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedSceneRange) {
                final AllocatedSceneRange range1 = (AllocatedSceneRange) range;
                final Rect rect = getRegion(range1.getFirstScene(), range1.getLastScene(), range1.getLowerBound(), range1.getUpperBound());
                canvas.drawRect(rect, paint);
            }
        }
    }

    private void drawOtherRanges(final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(otherRangeColor);
        for (Range range : otherRanges) {
            if (range instanceof AllocatedUnicastRange) {
                final AllocatedUnicastRange range1 = (AllocatedUnicastRange) range;
                final Rect rect = getRegion(range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_UNICAST_ADDRESS, MeshAddress.END_UNICAST_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedGroupRange) {
                final AllocatedGroupRange range1 = (AllocatedGroupRange) range;
                final Rect rect = getRegion(range1.getLowAddress(), range1.getHighAddress(), MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS);
                canvas.drawRect(rect, paint);
            } else if (range instanceof AllocatedSceneRange) {
                final AllocatedSceneRange range1 = (AllocatedSceneRange) range;
                final Rect rect = getRegion(range1.getFirstScene(), range1.getLastScene(), range1.getLowerBound(), range1.getUpperBound());
                canvas.drawRect(rect, paint);
            }
        }
    }

    public boolean drawConflictingRange(final Canvas canvas) {
        final Paint paint = getRectPaint();
        paint.setColor(conflictColor);
        for (Range range : ranges) {
            for (Range other : otherRanges) {
                if (range instanceof AllocatedUnicastRange) {
                    final AllocatedUnicastRange unicastRange = (AllocatedUnicastRange) range;
                    final AllocatedUnicastRange otherRange = (AllocatedUnicastRange) other;
                    if (overlaps(unicastRange.getLowAddress(), unicastRange.getHighAddress(), otherRange.getLowAddress(), otherRange.getHighAddress())) {
                        final Rect rect = getRegion(unicastRange.getLowAddress(), unicastRange.getHighAddress(), otherRange.getLowAddress(), MeshAddress.END_UNICAST_ADDRESS);
                        canvas.drawRect(rect, paint);
                    }
                } else if (range instanceof AllocatedGroupRange) {
                    final AllocatedGroupRange groupRange = (AllocatedGroupRange) range;
                    final AllocatedGroupRange otherRange = (AllocatedGroupRange) other;
                    if (overlaps(groupRange.getLowAddress(), groupRange.getHighAddress(), otherRange.getLowAddress(), otherRange.getHighAddress())) {
                        final Rect rect = getRegion(groupRange.getLowAddress(), groupRange.getHighAddress(), MeshAddress.START_GROUP_ADDRESS, MeshAddress.END_GROUP_ADDRESS);
                        canvas.drawRect(rect, paint);
                    }
                } else {
                    final AllocatedSceneRange sceneRange = (AllocatedSceneRange) range;
                    final AllocatedSceneRange otherRange = (AllocatedSceneRange) other;
                    if (overlaps(sceneRange.getFirstScene(), sceneRange.getLastScene(), otherRange.getFirstScene(), otherRange.getLastScene())) {
                        final Rect rect = getRegion(sceneRange.getFirstScene(), sceneRange.getLastScene(), sceneRange.getLowerBound(), sceneRange.getUpperBound());
                        canvas.drawRect(rect, paint);
                    }
                }
            }
        }
        return false;
    }

    private Rect getRegion(final int lowAddress, final int highAddress, final int lowerBound, final int upperBound) {
        final float unit = (mRect.width() / (float) (upperBound - lowerBound));
        final int x = (int) ((lowAddress - lowerBound) * unit);
        final int y = (int) ((highAddress - lowerBound) * unit);
        return new Rect(x, 0, y, mRect.height());
    }

    private Paint getRectPaint() {
        final Paint p = mPaint;
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        return p;
    }

    private boolean overlaps(final int rLowAddress, final int rHighAddress, final int oLowAddress, final int oHighAddress) {
        if (rLowAddress >= oLowAddress && rLowAddress <= oHighAddress) {
            return true;
        } else {
            return rHighAddress >= oLowAddress && rHighAddress <= oHighAddress;
        }
    }
}
